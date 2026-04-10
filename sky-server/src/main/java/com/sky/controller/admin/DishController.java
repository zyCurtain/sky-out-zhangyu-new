package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/admin/dish")
@RestController
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result addDish(@RequestBody DishDTO dishDTO){
        dishService.addDish(dishDTO);
        return Result.success();
    }

    /**
     *
     * 分页查询菜品数据
     * @param dto
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> dihPage(DishPageQueryDTO dto){
        PageResult pageResult = dishService.dishPage(dto);
        return Result.success(pageResult);
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteByIds(@RequestParam List<Long> ids){
        log.info("当前删除ids：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result findById(@PathVariable Long id){
        DishVO dishVO = dishService.selectById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息：{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 根据分类Id查询得到所有该分类下菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询当中所有菜品")
    public Result selectByCategoryId(Long categoryId){
        List<Dish> dishList = dishService.selectByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result statusDish(@PathVariable Integer status ,Long id){
        log.info("菜品修改状态：{},{}",status,id);
        dishService.statusControl(status,id);
        return Result.success();
    }
}
