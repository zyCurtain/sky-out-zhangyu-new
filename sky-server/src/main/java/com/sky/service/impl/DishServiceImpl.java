package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sky.vo.DishVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品+口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        // 1、先插入菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.addDish(dish);
        // 2、再插入菜品风味
        List<DishFlavor> flavors = dishDTO.getFlavors(); // 因为是list类型封装的若干口味
        Long dishId = dish.getId(); // 获取当前菜品ID，因为flavor表当中需要绑定该菜品
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId)); // forEach绑定菜品ID
        }
        dishFlavorMapper.insertFlavors(flavors);
    }

    @Override
    public PageResult dishPage(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DishVO> pageDishVo = dishMapper.pageQuery(dto);
        List<DishVO> dishVOList = pageDishVo.getResult();
        long total = pageDishVo.getTotal();
        PageResult pageResult = new PageResult(total, dishVOList);
        return pageResult;
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 1、先查看当前要删除的菜品状态是不是在售
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 2、判断当前菜品是否在套餐当中
        List<Long> setMealIds = setmealMapper.selectByDishIds(ids);
        if (setMealIds!=null && !setMealIds.isEmpty()){
            // 说明有套餐绑定
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 3、均不满足就可以进行删除
        dishFlavorMapper.deleteByIds(ids);
        dishMapper.deleteByIds(ids);
    }

    @Override
    public DishVO selectById(Long id) {
        DishVO dishVO = dishMapper.getByDishId(id); // 根据id查询得到dish实体数据
        List<DishFlavor> flavorList = dishFlavorMapper.getById(id); // 根据id查询得到对应该dish的若干flavor
        dishVO.setFlavors(flavorList); // 进行组装得到最终返回VO实体
        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        // 1、先更新dish表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        // 2、根据dish_id删除绑定的相关flavor然后重新插入和该dish相关的flavor
        dishFlavorMapper.deleteByIds(Collections.singletonList(dishDTO.getId()));
        // 重新插入
        List<DishFlavor> flavorList = dishDTO.getFlavors();
        // 一定要进行判空操作并且insert操作要放在if范围内，否则如何没有新增口味，我们还正常执行insert就会导致
        // xml当中我们无法进入foreach循环会导致SQL语句报错
        if (flavorList!=null && !flavorList.isEmpty()){ //因为如果是新加入的菜品口味，传输过来时候其实是没有绑定对应菜品id的需要自己再重新绑定
            flavorList.forEach(flavor ->{
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertFlavors(flavorList);
        }
    }

    @Override
    public List<Dish> selectByCategoryId(Long id) {
        List<Dish> dishList = dishMapper.selectByCategoryId(id);
        return dishList;
    }

    @Override
    @Transactional
    public void statusControl(Integer status, Long id) {
        // 规则：如果对当前的菜品进行停售操作那么对应包含它的套餐也要停售
        if (status == StatusConstant.DISABLE){
            // 检索包含当前菜品的套餐
            List<Setmeal> setmealList = setmealMapper.selectSetMealByDishId(id);
            setmealList.forEach(setmeal -> {
                if (setmeal.getStatus() == StatusConstant.ENABLE){ // 如果该套餐是起售状态就停售
                    setmealMapper.statusControl(StatusConstant.DISABLE,setmeal.getId());
                }
            });
            dishMapper.statusControl(status,id);
        }else {
            dishMapper.statusControl(status,id);
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

}
