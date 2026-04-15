package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.ShopCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopCartServiceImpl implements ShopCartService {

    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShopCart(ShoppingCartDTO shoppingCartDTO) {
        // 首先要设置购物车表当中的user_id为当前用户id，因为用户只能对自己的购物车进行相关操作
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId()); // 当前用户id已经在拦截器当中解析JWT令牌存入了
        // 然后根据前端信息查询该配置是否已经在购物车当中存在
        ShoppingCart cart = shopCartMapper.selectIfExist(shoppingCart);
        // 1、如果已经存在那么我们直接设置该配置的数量+1即可
        if (cart != null){
            shopCartMapper.addNum(cart.getId());
        }else{
            // 2、如果未存在，那么我们需要往购物车表当中新增一条数据
            // 判断当前要新增的是套餐还是菜品
            Long dishId = shoppingCart.getDishId();
            if (dishId!=null){
                // 说明是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setName(dish.getName());
            }else {
                // 要新增套餐
                Setmeal setmeal = setmealMapper.getSetMealById(shoppingCart.getSetmealId());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            // 进行插入
            shopCartMapper.insertCart(shoppingCart);

        }

    }

    @Override
    public List<ShoppingCart> showCart() {
        List<ShoppingCart> shoppingCartList = shopCartMapper.listCart(BaseContext.getCurrentId());
        return shoppingCartList;
    }

    @Override
    public void cleanCart() {
        shopCartMapper.clean(BaseContext.getCurrentId());
    }

    @Override
    public void deleteOne(ShoppingCartDTO shoppingCartDTO) {
        // 应该插入当前用户id
        shopCartMapper.deleteOne(shoppingCartDTO,BaseContext.getCurrentId());
    }
}
