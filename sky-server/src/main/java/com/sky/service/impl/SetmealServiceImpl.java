package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        Page<SetmealVO> setmealVOPage = setmealMapper.pageQuery(pageQueryDTO);
        long total = setmealVOPage.getTotal();
        List<SetmealVO> result = setmealVOPage.getResult();
        return new PageResult(total,result);
    }

    @Override
    public void addSetMeal(SetmealDTO setmealDTO) {
        // 1、首先新增到套餐表当中部分数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal); // 复制有关信息到套餐实体
        setmealMapper.addSetMeal(setmeal);
        // 2、获取套餐当中有关的“套餐-菜品”关联
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        // 因为setmealId是上一步刚创建的，所以当前List当中是没有Id的所以需要通过xml当中主动返回id
        Long id = setmeal.getId();
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });
        setmealDishMapper.addSetMealDishs(setmealDishList);
    }
}
