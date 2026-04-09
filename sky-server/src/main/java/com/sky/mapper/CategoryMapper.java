package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Page<Category> pageQuery(CategoryPageQueryDTO dto);
    @AutoFill(OperationType.INSERT)
    void addCategory(Category category);
    @AutoFill(OperationType.UPDATE)
    void editCategory(Category category);

    void statusEnable(Integer status, Integer id);

    void deleteCategory(Long id);

    List<Category> selectType(Integer type);
}
