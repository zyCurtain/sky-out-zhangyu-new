package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {
    @Select("select count(0) from setmeal where category_id=#{id}")
    Integer countSetmeal(Long id);
}
