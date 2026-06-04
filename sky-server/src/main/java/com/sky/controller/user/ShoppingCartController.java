package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api("c端购物车模块")
@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
            shoppingCartService.add(shoppingCartDTO);
            return Result.success();
    }
@ApiOperation("查看购物车")
@GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
    List<ShoppingCart> shoppingCartList=  shoppingCartService.list();
    return Result.success(shoppingCartList);
    }
    @ApiOperation("删除单一商品")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }
@ApiOperation("清空购物车")
@DeleteMapping("/clean")
    public Result clean(){
shoppingCartService.clean();
return Result.success();
    }
}
