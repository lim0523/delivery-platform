package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiOperation("批量删除")
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("删除id为：{}的菜品",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }
@ApiOperation("查询回显")
@GetMapping("/{id}")
    public Result<DishVO> find(@PathVariable Long id){
        log.info("查询id为：{}的员工",id);
        DishVO dishVO = dishService.find(id);
        return Result.success(dishVO);
}
@ApiOperation("修改菜品信息")
@PutMapping
public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();
}
@ApiOperation("菜品启售与禁售")
@PostMapping("/status/{status}")
    public Result SaleOrForbidden(@PathVariable Integer status,@RequestParam Long id){
        log.info("修改id：{}商品状态为：{}",id,status);
        dishService.saleOrForbidden(id,status);
        return Result.success();
}



}
