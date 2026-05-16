package com.example.e_shop.aspect;

import com.example.e_shop.model.entity.Products;
import com.example.e_shop.mapper.ProductsMapper;
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
 * 该切面在 @Cacheable 外层环绕，无论缓存命中与否，都在返回前执行浏览量自增。
 * </p>
 * <p>
 * @Order(1) 确保比缓存拦截器（默认 LOWEST_PRECEDENCE）优先执行，
 * proceed() -> 缓存拦截器（命中直接返回，未命中调目标方法）-> 浏览量自增 -> 返回。
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

        // 仅在业务成功时增加浏览量（缓存命中或未命中都会执行到这里）
        if (result instanceof Result<?> r && r.getCode() == 0) {
            Long productId = (Long) pjp.getArgs()[0];
            if (productId != null) {
                Products p = new Products();
                p.setId(productId);
                productsMapper.incrementDetailView(p);
                log.debug("浏览量 +1，productId={}", productId);
            }
        }
        return result;
    }
}
