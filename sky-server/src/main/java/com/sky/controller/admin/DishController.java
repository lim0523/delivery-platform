package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("菜品操作")
@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    DishService dishService;

    @ApiOperation("新增菜品")
    @PostMapping
    public Result add(@RequestBody DishDTO dishDTO){
        dishService.add(dishDTO);
        return Result.success();
    }
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public  Result<PageResult> page( DishPageQueryDTO pageQueryDTO){
        log.info("分页查询参数:{}",pageQueryDTO);
        PageResult pageResult  = dishService.page(pageQueryDTO);
         return Result.success(pageResult);
    }
}
