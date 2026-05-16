package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.e_shop.model.DTO.AddProductsDTO;
import com.example.e_shop.model.DTO.GetProductsDTO;
import com.example.e_shop.model.DTO.UpdateProductsDTO;
import com.example.e_shop.model.VO.ProductsDetailsVO;
import com.example.e_shop.model.VO.ProductsVO;
import com.example.e_shop.model.VO.UserVO;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.model.entity.Products;
import com.example.e_shop.model.entity.Transactionrecords;
import com.example.e_shop.model.entity.User;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.mapper.TransactionrecordsMapper;
import com.example.e_shop.mapper.UserMapper;
import com.example.e_shop.result.PageResult;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.ProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-08
 */

@Service
@Slf4j
@CacheConfig(cacheNames = "productsCache")
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {
           @Autowired
           ProductsMapper productsMapper;
           @Autowired
           UserMapper userMapper;
           @Autowired
    TransactionrecordsMapper transactionrecordsMapper;

    @CacheEvict(cacheNames = "productsCache", allEntries = true)
           public Result addProducts(AddProductsDTO addProductsDTO){
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        Products products=new Products();
        products.setName(addProductsDTO.getName());
        products.setImageUrl(addProductsDTO.getImageUrl());
        products.setPrice(addProductsDTO.getPrice());
        products.setTypeId(addProductsDTO.getTypeId());
        System.out.println(addProductsDTO.getDescription());
        products.setDescription(addProductsDTO.getDescription());
        products.setUserId(userId);
        if(productsMapper.insert(products)==0){
            return Result.error(MessageConstant.ADD+MessageConstant.FAILED);
        }

        else  return  Result.success(MessageConstant.ADD+MessageConstant.SUCCESS);
    }
    @Cacheable(key = "T(com.example.e_shop.util.CacheKeyUtil).getProductsKey(#getProductsDTO)")
        public Result<PageResult<ProductsVO>> getProducts(GetProductsDTO getProductsDTO){
        Page<Products> page=new Page<>(getProductsDTO.getPageNum(),getProductsDTO.getPageSize());
        QueryWrapper queryWrapper=new QueryWrapper<>();
        // 分页查询
        if(getProductsDTO.getUserId()!=null){
            queryWrapper.eq("user_id",getProductsDTO.getUserId());
        }
        if(getProductsDTO.getTypeId()!=null){
            queryWrapper.eq("typeId",getProductsDTO.getTypeId());
        }
        if(getProductsDTO.getName()!=null){
            queryWrapper.like("name",getProductsDTO.getName());
        }
        IPage<Products> productsIPage = productsMapper.selectPage(page, queryWrapper);
        if (productsIPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        // 转换成 ProductsVO
        List<ProductsVO> productsVOList = productsIPage.getRecords().stream()
                .map(products -> {
                            ProductsVO productsVO = new ProductsVO();
                            BeanUtils.copyProperties(products, productsVO);
                            return productsVO;

                        }
                ).toList();
        return Result.success(new PageResult<>(productsIPage.getTotal(), productsVOList));
    }
    @Cacheable(key = "#productId")
    public Result getProductsDetails(Long productId){

        Products products=productsMapper.selectById(productId);
        if(products==null){
            return Result.error(MessageConstant.FAILED);
        }

        else {
            User user=userMapper.selectById(products.getUserId());
            UserVO userVO=new UserVO();

            BeanUtils.copyProperties(user,userVO);
            userVO.setAvatarUrl(user.getUserImage());
            ProductsDetailsVO productsDetailsVO=new ProductsDetailsVO();
            BeanUtils.copyProperties(products, productsDetailsVO);
            productsDetailsVO.setUserName(user.getName());
            productsDetailsVO.setUserVO(userVO);
            return Result.success(MessageConstant.SUCCESS,productsDetailsVO);
        }

    }
    @CacheEvict(cacheNames = "productsCache", allEntries = true)
    public Result deleteProducts(Long productId){
         Products products=productsMapper.selectById(productId);
        QueryWrapper queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("productid",products.getId());
        List<Transactionrecords> transactionrecordsList=  transactionrecordsMapper.selectList(queryWrapper);
        if(transactionrecordsList!=null){

            for(Transactionrecords transactionrecords:transactionrecordsList){
                if(transactionrecords.getIsCommit()==true&&transactionrecords.getIsExpired()==false){
                    return  Result.error("处于确认订单状态的商品无法下架");
                }
            }

        }
         String productsImageUrl=products.getImageUrl();

         if(productsMapper.deleteById(productId)==0){
             return Result.error(MessageConstant.FAILED);
         }
         else return Result.success(MessageConstant.SUCCESS,productsImageUrl);
    }
    @CacheEvict(cacheNames = "productsCache", allEntries = true)
    public Result updateProducts(UpdateProductsDTO updateProductsDTO){
               Products products = productsMapper.selectById(updateProductsDTO.getId());
               QueryWrapper queryWrapper =new QueryWrapper<>();
               queryWrapper.eq("productid",products.getId());
              List<Transactionrecords> transactionrecordsList=  transactionrecordsMapper.selectList(queryWrapper);
              if(transactionrecordsList!=null){

                  for(Transactionrecords transactionrecords:transactionrecordsList){
                      if(transactionrecords.getIsCommit()==true&&transactionrecords.getIsExpired()==false){
                          return  Result.error("处于确认订单状态的商品无法修改");
                      }
                  }

              }
               if(products!=null){
                   BeanUtils.copyProperties(updateProductsDTO,products);
               }
              if( productsMapper.updateById(products)==0)
               return  Result.error();
                   else
                       return Result.success();
    }
}
