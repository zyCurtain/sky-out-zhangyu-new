package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
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

    Integer selectStatusById(Long id);

    void deleteSetMeal(List<Long> ids);

    Setmeal getSetMealById(Long id);
    @AutoFill(value = OperationType.UPDATE)
    void updateSetMeal(Setmeal setmeal);

    void statusControl(Integer status, Long id);

    List<Setmeal> selectSetMealByDishId(Long id);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

}
