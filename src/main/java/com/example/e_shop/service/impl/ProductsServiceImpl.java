package com.example.e_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.e_shop.DTO.AddProductsDTO;
import com.example.e_shop.DTO.GetProductsDTO;
import com.example.e_shop.VO.ProductsDetailsVO;
import com.example.e_shop.VO.ProductsVO;
import com.example.e_shop.VO.UserVO;
import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.constant.MessageConstant;
import com.example.e_shop.entity.Products;
import com.example.e_shop.entity.User;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.mapper.UserMapper;
import com.example.e_shop.result.PageResult;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.ProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements ProductsService {
           @Autowired
           ProductsMapper productsMapper;
           @Autowired
           UserMapper userMapper;
           public Result addProducts(AddProductsDTO addProductsDTO){
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        Products products=new Products();
        products.setName(addProductsDTO.getName());
        products.setImageUrl(addProductsDTO.getImageUrl());
        products.setPrice(addProductsDTO.getPrice());
        products.setUserId(userId);
        if(productsMapper.insert(products)==0){
            return Result.error(MessageConstant.ADD+MessageConstant.FAILED);
        }

        else  return  Result.success(MessageConstant.ADD+MessageConstant.SUCCESS);
    }
    public Result<PageResult<ProductsVO>> getProducts(GetProductsDTO getProductsDTO){
        Page<Products> page=new Page<>(getProductsDTO.getPageNum(),getProductsDTO.getPageSize());
        QueryWrapper queryWrapper=new QueryWrapper<>();
        // 分页查询
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
                }).toList();
        return Result.success(new PageResult<>(productsIPage.getTotal(), productsVOList));
    }
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
}
