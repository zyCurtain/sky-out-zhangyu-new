package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO);

    void addSetMeal(SetmealDTO setmealDTO);

    void deleteSetMeal(List<Long> ids);

    SetmealVO selectById(Long id);

    void updateSetMeal(SetmealDTO setmealDTO);

    void statusControl(Integer status, Long id);
}
