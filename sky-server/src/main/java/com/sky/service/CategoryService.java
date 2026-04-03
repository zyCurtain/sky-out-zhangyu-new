package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

public interface CategoryService {
    PageResult pageQuery(CategoryPageQueryDTO dto);

    void addCategory(CategoryDTO categoryDTO);

    void editCategory(CategoryDTO categoryDTO);

    void statusEnable(Integer status, Integer id);

    void deleteByID(Long id);
}
