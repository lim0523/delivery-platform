package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailsMapper orderDetailsMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    UserMapper userMapper;

    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO submitDTO) {
        //处理各种异常问题（地址为空，购物车为空）
        AddressBook addressBook = addressBookMapper.getById(submitDTO.getAddressBookId());
        if (addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart =new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList==null||shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //1.向order表插入一条数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(submitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setAddressBookId(addressBook.getId());
        orderMapper.insert(orders);//要设置主键回显，用于明细表的数据
        //2.向order_details表插入n条数据
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailsMapper.insertBatch(orderDetailList);
        //3.清空当前用户购物车
        shoppingCartMapper.clean(userId);
        //4.封装vo对象
        OrderSubmitVO submitVO=OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();
        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        // 1.根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(
                ordersPaymentDTO.getOrderNumber());

        if (ordersDB == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 2.如果已经支付，直接报错
        if (ordersDB.getPayStatus() != null
                && ordersDB.getPayStatus().equals(Orders.PAID)) {
            throw new OrderBusinessException("该订单已支付");
        }

        // 3.直接模拟支付成功
        paySuccess(ordersPaymentDTO.getOrderNumber());

        // 4.返回假的支付参数
        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr("test");
        vo.setPackageStr("test");
        vo.setPaySign("test");
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        vo.setSignType("RSA");

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        if (ordersDB == null) {
            return;
        }

        // 更新订单状态
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
