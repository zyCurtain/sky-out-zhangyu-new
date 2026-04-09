package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    @Select("select count(0) from dish where category_id=#{id}")
    Integer countDish(Long id);
    @AutoFill(value = OperationType.INSERT)
    void addDish(Dish dish);
}
