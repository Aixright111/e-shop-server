package com.example.e_shop.service;

import com.example.e_shop.model.VO.FavoritesVO;
import com.example.e_shop.model.entity.Favorites;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.e_shop.result.Result;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-21
 */
public interface FavoritesService extends IService<Favorites> {
    Result addFavorite(Long productId, Long userId);
    Result<List<FavoritesVO>> getFavorites(Long userId);
    Result removeFavorite(Long productId, Long userId);
    Result<List<String>> recommend(Long userId);
}
