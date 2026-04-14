package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

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

    @Override
    @Transactional
    public void deleteSetMeal(List<Long> ids) {
        // 1、先判断是否在售
        ids.forEach(id ->{
            Integer status = setmealMapper.selectStatusById(id);
            if (status == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        // 2、先删除绑定的关系数据
        setmealDishMapper.deleteSetMealDishs(ids);
        // 3、在删除套餐本体
        setmealMapper.deleteSetMeal(ids);
    }

    @Override
    public SetmealVO selectById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        // 1、先创建新Setmeal对象单独接收基本数据
        Setmeal setmeal = setmealMapper.getSetMealById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        // 2、再获取对应的绑定关联数据
        List<SetmealDish> setmealDishList = setmealDishMapper.selectSetMealDishs(id);
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateSetMeal(SetmealDTO setmealDTO) {
        // 1、对基本SetMeal进行数据修改
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateSetMeal(setmeal);
        // 2、对SetMeal-Dishs进行删除+重新插入操作
        setmealDishMapper.deleteSetMealDishs(Collections.singletonList(setmealDTO.getId()));
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        setmealDishMapper.addSetMealDishs(setmealDishList);
    }

    @Override
    public void statusControl(Integer status, Long id) {
        // 1、先检查该套餐当中是否有已经停售的菜品(仅针对需要起售的情况）
        if (status == StatusConstant.ENABLE){
            List<SetmealDish> setmealDishList = setmealDishMapper.selectSetMealDishs(id);
            setmealDishList.forEach(setmealDish -> {
                Integer statusDish = dishMapper.selectStatusById(setmealDish.getDishId());
                if (statusDish == StatusConstant.DISABLE){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }
        // 2、检查通过可以
        setmealMapper.statusControl(status,id);
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
