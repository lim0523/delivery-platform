package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("店铺相关接口")
public class ShopController {
    public  static final String KEY="SHOP_STATUS";
    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        String statusStr = (String) redisTemplate.opsForValue().get(KEY);
        Integer status = Integer.valueOf(statusStr);
        log.info("店铺当前状态:{}",status==1?"营业中":"已打烊");
        return Result.success(status);
    }
}
