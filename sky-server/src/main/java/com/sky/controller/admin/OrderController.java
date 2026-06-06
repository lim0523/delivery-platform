package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api("管理端订单接口")
@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    OrderService orderService;
@PutMapping("/cancel")
@ApiOperation("商家取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单：{}",ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> getStatus(){
        log.info("开始各个状态的订单数量统计");
        OrderStatisticsVO vo= orderService.getStatus();
        return Result.success(vo);
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result finishOrder(@PathVariable Long id){
        log.info("完成id为：{}的订单",id);
        orderService.finishOrder(id);
        return Result.success();
  }

  @GetMapping("/conditionSearch")
  @ApiOperation("订单搜索")
  public Result<PageResult> page(OrdersPageQueryDTO dto){
        log.info("订单搜索{}",dto);
        PageResult page= orderService.page(dto);
        return Result.success(page);
  }

    @PutMapping("/rejection")
    @ApiOperation("商家拒单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO dto){
        log.info("商家拒单：{}",dto);
        orderService.rejectOrder(dto);
        return Result.success();
    }

    @PutMapping("/confirm")
    @ApiOperation("商家接单")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO confirmDTO){
        log.info("商家接单：{}",confirmDTO);
        orderService.confirmOrder(confirmDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("商家派送")
    public Result deliverOrder(@PathVariable Long id){
        log.info("商家派单:{}",id);
        orderService.deliveryOrder(id);
        return Result.success();
    }

   @GetMapping("/details/{id}")
   @ApiOperation("查看订单详情")
   public Result<OrderVO> getInfo(@PathVariable Long id){
            log.info("查看id为{}的订单详情",id);
       OrderVO infoById = orderService.getInfoById(id);
       return Result.success(infoById);
   }


}
