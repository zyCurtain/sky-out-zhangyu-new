package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO dto) {
        // 设置分页参数
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        // 获取page结果
        Page<Category> page = categoryMapper.pageQuery(dto);
        long total = page.getTotal();
        List<Category> categoryList = page.getResult();
        PageResult pageResult = new PageResult(total,categoryList);
        return pageResult;
    }

    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        // 由于category表当中实际列大于传输的四项，所以需要新建新对象进行属性复制
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.DISABLE);
        categoryMapper.addCategory(category);
    }

    @Override
    public void editCategory(CategoryDTO categoryDTO) {
        // 由于category表当中实际列大于传输的四项，所以需要新建新对象进行属性复制
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        // 因为使用AOP公共字段统一执行set方法所以这里删除了两个赋值
        categoryMapper.editCategory(category);
    }

    @Override
    public void statusEnable(Integer status, Integer id) {
        categoryMapper.statusEnable(status,id);
    }

    @Override
    public void deleteByID(Long id) {
        // 先检查当前分类下是否有菜品
        Integer count = dishMapper.countDish(id);
        if(count>0){ // 当前分类下包含菜品，返回自定义异常抛出异常信息
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        count = setmealMapper.countSetmeal(id);
        if (count>0){
            throw new SetmealEnableFailedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.deleteCategory(id);
    }

    @Override
    public List<Category> selectType(Integer type) {
        List<Category> categoryList = categoryMapper.selectType(type);
        return categoryList;
    }
}
