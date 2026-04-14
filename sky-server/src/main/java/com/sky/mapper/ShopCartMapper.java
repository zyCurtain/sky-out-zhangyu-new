package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShopCartMapper {
    ShoppingCart selectIfExist(ShoppingCart shoppingCart);

    void addNum(Long id);

    void insertCart(ShoppingCart shoppingCart);

    List<ShoppingCart> listCart();

    void clean(Long currentId);

    void deleteOne(ShoppingCartDTO shoppingCartDTO, Long currentId);
}
