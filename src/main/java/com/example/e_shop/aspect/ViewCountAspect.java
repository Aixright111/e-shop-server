package com.example.e_shop.aspect;

import com.example.e_shop.mapper.ProductsMapper;
import com.example.e_shop.model.VO.ProductsDetailsVO;
import com.example.e_shop.model.entity.Products;
import com.example.e_shop.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 商品详情浏览量切面。
 * <p>
 * getProductsDetails 加了 @Cacheable，方法体只在缓存未命中时执行一次，
 * 但其中的 incrementDetailView（浏览量 +1）需要每次调用都执行。
 * 该切面在 @Cacheable 外层环绕，无论缓存命中与否，都在返回前执行浏览量自增，
 * 并更新缓存结果中的 detailView 值，确保返回给前端的始终是最新浏览量。
 * </p>
 * <p>
 * @Order(1) 确保比缓存拦截器（默认 LOWEST_PRECEDENCE）优先执行。
 * </p>
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class ViewCountAspect {

    @Autowired
    private ProductsMapper productsMapper;

    @Around("execution(* com.example.e_shop.service.impl.ProductsServiceImpl.getProductsDetails(..))")
    public Object incrementViewCount(ProceedingJoinPoint pjp) throws Throwable {
        // 先走 @Cacheable 缓存拦截器（命中直接返回，未命中执行方法体）
        Object result = pjp.proceed();

        if (result instanceof Result<?> r && r.getCode() == 0) {
            Long productId = (Long) pjp.getArgs()[0];
            if (productId != null && r.getData() instanceof ProductsDetailsVO vo) {
                // 数据库浏览量 +1
                Products p = new Products();
                p.setId(productId);
                productsMapper.incrementDetailView(p);
                // 读回最新浏览量，更新到缓存结果中（避免返回旧值）
                Products updated = productsMapper.selectById(productId);
                if (updated != null) {
                    vo.setDetailView(updated.getDetailView());
                }
                log.debug("浏览量 +1，productId={}", productId);
            }
        }
        return result;
    }
}
