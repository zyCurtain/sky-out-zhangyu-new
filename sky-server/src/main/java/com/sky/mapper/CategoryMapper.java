package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {
    Page<Category> pageQuery(CategoryPageQueryDTO dto);

    void addCategory(Category category);

    void editCategory(Category category);

    void statusEnable(Integer status, Integer id);

    void deleteCategory(Long id);
}
