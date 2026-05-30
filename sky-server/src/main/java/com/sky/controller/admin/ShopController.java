package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api("店铺相关接口")
public class ShopController {
    public  static final String KEY="SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;
    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为:{}",status==1?"营业中":"已打烊");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(KEY, status.toString());
        return Result.success();
    }
    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        String statusStr = (String) redisTemplate.opsForValue().get(KEY);
        Integer status = Integer.valueOf(statusStr);
        log.info("店铺当前状态:{}",status==1?"营业中":"已打烊");
        return Result.success(status);
    }
}
