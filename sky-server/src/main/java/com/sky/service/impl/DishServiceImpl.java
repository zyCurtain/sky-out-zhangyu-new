package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品+口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        // 1、先插入菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.addDish(dish);
        // 2、再插入菜品风味
        List<DishFlavor> flavors = dishDTO.getFlavors(); // 因为是list类型封装的若干口味
        Long dishId = dish.getId(); // 获取当前菜品ID，因为flavor表当中需要绑定该菜品
        if (flavors != null && !flavors.isEmpty()){
            flavors.forEach(flavor ->flavor.setDishId(dishId)); // forEach绑定菜品ID
        }
        dishFlavorMapper.insertFlavors(flavors);
    }
}
