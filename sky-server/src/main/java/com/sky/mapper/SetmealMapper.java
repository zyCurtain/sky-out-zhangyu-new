package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    @Select("select count(0) from setmeal where category_id=#{id}")
    Integer countSetmeal(Long id);

    List<Long> selectByDishIds(List<Long> ids);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO pageQueryDTO);
    @AutoFill(value = OperationType.INSERT)
    void addSetMeal(Setmeal setmeal);
}
