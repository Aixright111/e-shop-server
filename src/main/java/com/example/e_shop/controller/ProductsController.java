package com.example.e_shop.controller;

import com.example.e_shop.model.DTO.AddProductsDTO;
import com.example.e_shop.model.DTO.GetProductsDTO;
import com.example.e_shop.model.DTO.UpdateProductsDTO;
import com.example.e_shop.model.VO.ProductsVO;
import com.example.e_shop.result.PageResult;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.ProductsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author e-shop
 * @since 2026-05-08
 */
@RestController
@RequestMapping("/products")
public class ProductsController {
   @Autowired
   private ProductsService productsService;
    @PostMapping("/add")
    public Result addProducts(@RequestBody AddProductsDTO addProductsDTO){
        return productsService.addProducts(addProductsDTO);
    }
   @PostMapping("/list")
    public Result<PageResult<ProductsVO>> getProducts(@RequestBody @NotNull GetProductsDTO getProductsDTO){
        return productsService.getProducts(getProductsDTO);
   }
   @GetMapping("/details/{id}")
    public Result getProductsDetails(@PathVariable("id") Long productId){
        return productsService.getProductsDetails(productId);
   }
   @DeleteMapping("/delete/{productsId}")
    public Result deleteProducts(@PathVariable("productsId") Long productsId){

        return  productsService.deleteProducts(productsId);
   }
   @PutMapping("/update")
    public  Result updateProducts(@RequestBody UpdateProductsDTO updateProductsDTO){
        return productsService.updateProducts(updateProductsDTO);
   }
}
