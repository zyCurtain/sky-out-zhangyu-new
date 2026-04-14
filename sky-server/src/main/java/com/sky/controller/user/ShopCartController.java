package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShopCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
public class ShopCartController {
    @Autowired
    private ShopCartService shopCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result addShopCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shopCartService.addShopCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result showCart(){
        List<ShoppingCart> shoppingCartList = shopCartService.showCart();
        return Result.success(shoppingCartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result cleanCart(){
        shopCartService.cleanCart();
        return Result.success();
    }

    /**
     * 删除一个商品
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车一个商品")
    public Result deleteOne(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shopCartService.deleteOne(shoppingCartDTO);
        return Result.success();
    }
}
