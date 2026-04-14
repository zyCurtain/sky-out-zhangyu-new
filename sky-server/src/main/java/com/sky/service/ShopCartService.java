package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShopCartService {
    void addShopCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showCart();

    void cleanCart();

    void deleteOne(ShoppingCartDTO shoppingCartDTO);
}
