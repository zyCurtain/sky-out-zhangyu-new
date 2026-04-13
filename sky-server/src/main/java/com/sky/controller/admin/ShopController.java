package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate; // 因为只是一个简单的状态变换所以无需通过数据库来实现

    public static final String KEY = "SHOP_STATUS"; // 作为key

    /**
     * 获取营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result getStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status = (Integer) valueOperations.get(KEY);
        log.info("当前店铺状态为：{}", status == 1 ? "营业中" : "已打烊");
        return Result.success(status);
    }
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable Integer status){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(KEY,status);
        return Result.success();
    }
}
