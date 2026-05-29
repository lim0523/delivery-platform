package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api("套餐相关操作")
@RestController
@RequestMapping("/admin/setmeal")
public class SetMealController {
    @Autowired
    SetMealService setMealService;
    @ApiOperation("新增套餐")
    @PostMapping
    public Result add(@RequestBody SetmealDTO setmealDTO){
        log.info("新增菜品信息:{}",setmealDTO);
        setMealService.add(setmealDTO);
        return Result.success();
    }
@ApiOperation("分页查询")
@GetMapping("/page")
    public Result<PageResult> page( SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("查询{}套餐",setmealPageQueryDTO);
        PageResult pageResult= setMealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
@ApiOperation("批量删除")
@DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("删除id为:{}的套餐",ids);
        setMealService.deleteBatch(ids);
        return Result.success();
    }
    @ApiOperation("查询回显")
    @GetMapping("/{id}")
    public Result<SetmealVO> find(@PathVariable Long id){
        log.info("查询id为:{}的套餐",id);
        SetmealVO setmealVO =setMealService.find(id);
        return Result.success(setmealVO);
    }
    @ApiOperation("修改套餐")
    @PutMapping
public Result update(@RequestBody SetmealDTO setmealDTO ){
   log.info("修改套餐:{}",setmealDTO);
   setMealService.update(setmealDTO);
   return Result.success();
    }
@ApiOperation("套餐启售与禁售")
@PostMapping("/status/{status}")
public Result saleOrForbidden(@PathVariable Integer status ,@RequestParam Long id){
      log.info("修改id:{}套餐状态为:{}",id,status);
      setMealService.saleOrForbidden(status,id);
      return Result.success();
    }

}
