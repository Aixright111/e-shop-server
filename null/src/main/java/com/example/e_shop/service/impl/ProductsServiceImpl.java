package com.example.e_shop.service.impl;

import com.example.e_shop.entity.Products;
import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.service.ProductsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
