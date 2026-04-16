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
import java.util.Map;

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

    /**
     * 动态条件查询菜品
     *
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

}
