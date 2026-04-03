package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类模块-分页查询
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result pageCategory(CategoryPageQueryDTO dto){
        log.info("分类分页查询参数：{}",dto);
        PageResult pageResult = categoryService.pageQuery(dto);
        log.info("分页查询结果：{}",pageResult);
        return Result.success(pageResult);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO){
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }
    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result editCategory(@RequestBody CategoryDTO categoryDTO){
        categoryService.editCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result statusEnable(@PathVariable Integer status,Integer id){
        categoryService.statusEnable(status,id);
        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result deleteCategory(Long id){
        log.info("删除分类id：{}",id);
        categoryService.deleteByID(id);
        return Result.success();
    }
}
