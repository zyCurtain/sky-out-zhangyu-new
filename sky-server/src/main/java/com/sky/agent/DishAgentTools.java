package com.sky.agent;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO; // 🌟 引入包含口味的 VO
import com.sky.service.DishService;
import com.sky.service.ShopCartService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DishAgentTools {

    @Autowired
    private DishService dishService;

    @Autowired
    private ShopCartService shopCartService;

    // 🌟 1. 升级查询工具，大模型会自动读取返回数据里的 flavors 列表
    @Tool("当用户询问推荐菜品或查询菜品时调用。务必关注返回结果中的 flavors 字段：name 代表口味维度（如'辣度'），value 包含了具体的可选值数组（如'[\"微辣\",\"中辣\"]'）。")
    public List<DishVO> queryDish(String keyword) {
        Dish dish = new Dish();
        dish.setName(keyword);
        dish.setStatus(1);

        // 调用你原有业务中带口味的查询方法（C端接口通常有现成的）
        return dishService.listWithFlavor(dish);
    }

    // 🌟 2. 升级购物车工具，接收大模型拼接好的口味字符串
    @Tool("加入购物车。必须传入真实的 dishId。如果该菜品有 flavors 选项且用户已确认，请将用户的选择组装成字符串（如'微辣,去冰'）传入 dishFlavor 参数；若无口味要求或无选项，传入 null。")
    public String addDishToCart(Long dishId, String dishFlavor) {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dishId);
        // 大模型会将“微辣”自动提取出来传给这个参数
        dto.setDishFlavor(dishFlavor);

        shopCartService.addShopCart(dto);

        return "操作成功，已加入购物车";
    }
}
