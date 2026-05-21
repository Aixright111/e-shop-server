package com.example.e_shop.service;

import com.example.e_shop.model.DTO.AddProductsDTO;
import com.example.e_shop.model.DTO.GetProductsDTO;
import com.example.e_shop.model.DTO.UpdateProductsDTO;
import com.example.e_shop.model.VO.ProductsVO;
import com.example.e_shop.model.entity.Products;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.e_shop.result.PageResult;
import com.example.e_shop.result.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author e-shop
 * @since 2026-05-08
 */
public interface ProductsService extends IService<Products> {
    Result addProducts(AddProductsDTO addProductsDTO);
    Result getProducts(GetProductsDTO getProductsDTO);
    Result getProductsDetails(Long productId);
    Result deleteProducts(Long productId);
    Result updateProducts(UpdateProductsDTO updateProductsDTO);
    Result aiGetProducts(GetProductsDTO getProductsDTO);
}
