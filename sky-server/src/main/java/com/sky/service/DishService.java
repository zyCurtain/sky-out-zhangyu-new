package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {
    void addDish(DishDTO dishDTO);

    PageResult dishPage(DishPageQueryDTO dto);

    void deleteBatch(List<Long> ids);

    DishVO selectById(Long id);

    void update(DishDTO dishDTO);

    List<Dish> selectByCategoryId(Long id);

    void statusControl(Integer status, Long id);
}
