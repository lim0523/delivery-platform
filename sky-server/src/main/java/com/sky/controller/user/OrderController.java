package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
// todo 完善派送距离判读
@Slf4j
@RestController("UserOrderController")
@RequestMapping("/user/order")
@Api("用户订单模块")
public class OrderController {
    @Autowired
    OrderService orderService;

    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO submitDTO){
            log.info("订单信息：{}",submitDTO);
            OrderSubmitVO submitVO=orderService.submit(submitDTO);
            return Result.success(submitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    /**
     * 分页查询历史订单
     */
@GetMapping("/historyOrders")
@ApiOperation("查询历史订单")
    public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO queryDTO){
        log.info("查询历史订单:{}",queryDTO);
        PageResult pageResult =orderService.getHistoryOrders(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
      log.info("查询id为:{}的订单详情",id);
      OrderVO orderVO=orderService.getInfoById(id);
      return Result.success(orderVO);
}
/**
 * 再次下单
 */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再次下单")
    public Result repeatOrder(@PathVariable Long id){
    log.info("再次下单:{}",id);
    orderService.repeatOrder(id);
    return Result.success();
}

/**
 * 取消订单
 */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消下单")
    public Result cancelOrder(@PathVariable Long id){
        log.info("取消id为:{}的订单",id);
        orderService.cancelOrder(id);
        return Result.success();
    }
    /**
     * 催单
     */
    // todo 需要基于websocket真正实现催单功能

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        log.info("订单号为:{}的用户催单",id);
        orderService.reminder(id);
        return Result.success();
    }


}
