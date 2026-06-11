package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * *")
    public void orderOvertime() {
        log.info("处理支付超时订单:{}", LocalDateTime.now());
        //根据状态和下单时间（是否与现在时间相差15分钟）查询
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderMapper.selectByStatusAndOrderTime(Orders.PENDING_PAYMENT, orderTime);
        if (ordersList != null && !ordersList.isEmpty()) {
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(MessageConstant.PAY_OVERTIME);
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            });
        }
    }

    /**
     * 在打烊时间处理状态仍在配送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void deliveredOrder() {
        log.info("处理仍在配送中的订单:{}", LocalDateTime.now());
        // 查询凌晨0点之前仍处于派送中的订单
        LocalDateTime orderTime = LocalDateTime.now().minusHours(-1);//0时之前包含了昨天一天
        List<Orders> ordersList = orderMapper.selectByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,orderTime );
        if (ordersList != null && !ordersList.isEmpty()) {
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(orders);
            });
        }
    }
}
