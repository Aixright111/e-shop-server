package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.model.VO.FavoritesVO;
import com.example.e_shop.model.entity.Favorites;
import com.example.e_shop.model.entity.Products;
import com.example.e_shop.mapper.FavoritesMapper;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.FavoritesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {

    @Autowired
    private FavoritesMapper favoritesMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Override
    public Result addFavorite(Long productId, Long userId) {
        // 查商品是否存在
        Products product = productsMapper.selectById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 查是否已收藏
        Long count = favoritesMapper.selectCount(
                new QueryWrapper<Favorites>()
                        .eq("user_id", userId)
                        .eq("product_id", productId));
        if (count > 0) {
            return Result.error("已收藏");
        }

        Favorites favorite = new Favorites();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setTypeId(product.getTypeId());
        favorite.setCreatedAt(LocalDateTime.now());
        favoritesMapper.insert(favorite);
        return Result.success("收藏成功");
    }

    @Override
    public Result<List<FavoritesVO>> getFavorites(Long userId) {
        List<Favorites> favorites = favoritesMapper.selectList(
                new QueryWrapper<Favorites>().eq("user_id", userId));

        List<FavoritesVO> voList = favorites.stream().map(fav -> {
            FavoritesVO vo = new FavoritesVO();
            BeanUtils.copyProperties(fav, vo);
            // 回填商品信息
            Products product = productsMapper.selectById(fav.getProductId());
            if (product != null) {
                vo.setName(product.getName());
                vo.setPrice(product.getPrice());
                vo.setImageUrl(product.getImageUrl());
                vo.setDescription(product.getDescription());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result removeFavorite(Long productId, Long userId) {
        int deleted = favoritesMapper.delete(
                new QueryWrapper<Favorites>()
                        .eq("product_id", productId)
                        .eq("user_id", userId));
        if (deleted == 0) {
            return Result.error("未找到收藏记录");
        }
        return Result.success("已取消收藏");
    }

    @Override
    public Result<List<String>> recommend(Long userId) {
        // 1. 获取用户收藏的所有商品
        List<Favorites> favorites = favoritesMapper.selectList(
                new QueryWrapper<Favorites>().eq("user_id", userId));
        if (favorites.isEmpty()) {
            List<Products> all = productsMapper.selectList(
                    new QueryWrapper<Products>().select("name"));
            Collections.shuffle(all);
            List<String> randomNames = all.stream()
                    .limit(6)
                    .map(Products::getName)
                    .collect(Collectors.toList());
            return Result.success(randomNames);
        }

        // 2. 按 typeId 统计数量，按权重分配 6 个推荐位
        Map<Long, Long> typeCount = favorites.stream()
                .collect(Collectors.groupingBy(Favorites::getTypeId, Collectors.counting()));
        long totalCount = typeCount.values().stream().mapToLong(Long::longValue).sum();

        // 按收藏数量降序排列 typeId
        List<Long> sortedTypes = typeCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 如果不足三个类型，从商品表随机补充
        if (sortedTypes.size() < 3) {
            Set<Long> existing = new HashSet<>(sortedTypes);
            List<Long> candidates = productsMapper.selectList(
                    new QueryWrapper<Products>().select("DISTINCT typeid")).stream()
                    .map(Products::getTypeId)
                    .filter(t -> !existing.contains(t))
                    .distinct()
                    .collect(Collectors.toList());
            Collections.shuffle(candidates);
            sortedTypes.addAll(candidates.subList(0, Math.min(3 - sortedTypes.size(), candidates.size())));
        }

        // 按权重分配每个 typeId 应推荐的数量
        Map<Long, Integer> quota = new HashMap<>();
        int allocated = 0;
        for (int i = 0; i < sortedTypes.size(); i++) {
            Long typeId = sortedTypes.get(i);
            long count = typeCount.getOrDefault(typeId, 0L);
            int slot = (int) Math.round(6.0 * count / totalCount);
            // 最后一类兜底，确保总数 = 6
            if (i == sortedTypes.size() - 1) {
                slot = 6 - allocated;
            }
            if (slot > 0) {
                quota.put(typeId, slot);
                allocated += slot;
            }
        }

        // 3. 用 pgvector <=> 在 DB 内完成向量相似度搜索（每类一次查询）
        Set<String> result = new LinkedHashSet<>();
        Set<Long> recommendedIds = new HashSet<>();

        for (Map.Entry<Long, Integer> entry : quota.entrySet()) {
            Long typeId = entry.getKey();
            int need = entry.getValue();

            // 用户收藏的该类型商品 ID
            List<Long> favIds = favorites.stream()
                    .filter(f -> f.getTypeId().equals(typeId))
                    .map(Favorites::getProductId)
                    .collect(Collectors.toList());
            if (favIds.isEmpty()) continue;

            // 平均向量作为查询向量
            List<Products> favProducts = productsMapper.selectBatchIds(favIds);
            float[] avgVector = averageVectors(favProducts.stream()
                    .filter(p -> p.getEmbedding() != null)
                    .map(Products::getEmbedding)
                    .collect(Collectors.toList()));
            if (avgVector == null) continue;

            // 排除已推荐的商品
            List<Long> excludeIds = new ArrayList<>(favIds);
            excludeIds.addAll(recommendedIds);

            // DB 内完成向量排序，直接取 top-N 名称
            List<String> names = productsMapper.findTopSimilarNames(
                    arrayToVectorStr(avgVector), typeId, excludeIds, need);
            result.addAll(names);
        }

        return Result.success(new ArrayList<>(result));
    }

    private float[] averageVectors(List<float[]> vectors) {
        if (vectors == null || vectors.isEmpty()) return null;
        int dim = vectors.get(0).length;
        float[] avg = new float[dim];
        for (float[] v : vectors) {
            for (int i = 0; i < dim; i++) avg[i] += v[i];
        }
        for (int i = 0; i < dim; i++) avg[i] /= vectors.size();
        return avg;
    }

    private String arrayToVectorStr(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
