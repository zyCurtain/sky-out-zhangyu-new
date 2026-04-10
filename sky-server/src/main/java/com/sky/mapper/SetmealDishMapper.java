package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    void addSetMealDishs(List<SetmealDish> setmealDishList);

    void deleteSetMealDishs(List<Long> ids);

    List<SetmealDish> selectSetMealDishs(Long id);

}
