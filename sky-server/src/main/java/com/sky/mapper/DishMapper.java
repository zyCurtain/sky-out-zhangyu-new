package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {
    @Select("select count(0) from dish where category_id=#{id}")
    Integer countDish(Long id);
    @AutoFill(value = OperationType.INSERT)
    void addDish(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dto);

    Dish getById(Long id);

    void deleteByIds(List<Long> ids);

    DishVO getByDishId(Long id);
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> selectByCategoryId(Long id);

    Integer selectStatusById(Long dishId);

    void statusControl(Integer status, Long id);
}
