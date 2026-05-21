package com.example.e_shop.controller;

import com.example.e_shop.constant.JwtClaimsConstant;
import com.example.e_shop.model.VO.FavoritesVO;
import com.example.e_shop.result.Result;
import com.example.e_shop.service.FavoritesService;
import com.example.e_shop.util.ThreadLocalUtil;
import com.example.e_shop.util.TypeConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

    @PostMapping("/add/{productId}")
    public Result addFavorite(@PathVariable Long productId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        return favoritesService.addFavorite(productId, userId);
    }

    @GetMapping("/list")
    public Result<List<FavoritesVO>> getFavorites() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        return favoritesService.getFavorites(userId);
    }

    @DeleteMapping("/{productId}")
    public Result removeFavorite(@PathVariable Long productId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        return favoritesService.removeFavorite(productId, userId);
    }

    @GetMapping("/recommend")
    public Result<List<String>> recommend() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID));
        return favoritesService.recommend(userId);
    }
}
