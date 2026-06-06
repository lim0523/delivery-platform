package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
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

    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO queryDTO) {
        Long userId=BaseContext.getCurrentId();
        log.info("员工的ID:{}",userId);
        PageHelper.startPage(queryDTO.getPage(),queryDTO.getPageSize());
        List<Orders> ordersList= orderMapper.getOrdersByUid(userId);
        Page page = (Page) ordersList;

        if (ordersList == null || ordersList.isEmpty()) {
            return new PageResult(page.getTotal(), new ArrayList<>());
        }
        List<Long> orderIdList = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        List<OrderDetail> detailList= orderDetailsMapper.selectAllDetails(orderIdList);

        //对应封装
        Map<Long, List<OrderDetail>> orderMap = detailList.stream().collect(Collectors.groupingBy(OrderDetail::getOrderId));

        List<OrderVO> orderVOList = ordersList.stream().map(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderMap.get(orders.getId()));
            return orderVO;
        }).collect(Collectors.toList());
        return new PageResult(page.getTotal(),orderVOList);
    }

    @Override
    public OrderVO getInfoById(Long id) {
       Orders orders= orderMapper.selectById(id);
       if (orders==null){
           throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
       }
       OrderVO orderVO=new OrderVO();
       BeanUtils.copyProperties(orders,orderVO);
       orderVO.setOrderDetailList(orderDetailsMapper.selectDetailsById(id));
       return orderVO;
    }
@Transactional
    @Override
    public void repeatOrder(Long id) {
        //1.先查询订单基本信息，判断是否存在
        Orders oldOrders = orderMapper.selectById(id);
        if (oldOrders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
         List<OrderDetail> oldDetails = orderDetailsMapper.selectDetailsById(id);
    List<ShoppingCart> shoppingCartList  = oldDetails.stream().map(orderDetail -> {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(orderDetail, shoppingCart);
        shoppingCart.setId(null);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setCreateTime(LocalDateTime.now());
        return shoppingCart;
    }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    //
//       //2.拷贝到新订单，更新下单时间,订单号,订单状态，订单支付状态
//        Orders newOrders=new Orders();
//        BeanUtils.copyProperties(oldOrders,newOrders);
//        newOrders.setOrderTime(LocalDateTime.now());
//        newOrders.setNumber(String.valueOf(System.currentTimeMillis()));
//        newOrders.setStatus(Orders.PENDING_PAYMENT);
//        newOrders.setPayStatus(Orders.UN_PAID);
//        //3. 同时清空主键(防止插入时主键冲突)
//        newOrders.setId(null);
//        // 4.清空旧订单的完成/取消/拒单等时间和原因
//        newOrders.setCheckoutTime(null);
//        newOrders.setDeliveryTime(null);
//        newOrders.setCancelTime(null);
//        newOrders.setCancelReason(null);
//        newOrders.setRejectionReason(null);
//        //5.插入
//        orderMapper.insert(newOrders);
//        //6.查询之前订单详情
//    List<OrderDetail> oldDetails = orderDetailsMapper.selectDetailsById(id);
//
//    List<OrderDetail> newDetailList = oldDetails.stream().map(orderDetail -> {
//        OrderDetail newOrderDetail = new OrderDetail();
//        //7.拷贝基本内容
//        BeanUtils.copyProperties(orderDetail, newOrderDetail);
//        //8.清空原有主键防止插入冲突,更新订单号
//        newOrderDetail.setId(null);
//        newOrderDetail.setOrderId(newOrders.getId());
//        return newOrderDetail;
//    }).collect(Collectors.toList());
//      orderDetailsMapper.insertBatch(newDetailList);
}

    @Override
    public void cancelOrder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus().equals(Orders.PENDING_PAYMENT)||orders.getStatus().equals(Orders.TO_BE_CONFIRMED)||orders.getStatus().equals(Orders.CONFIRMED)){
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason(MessageConstant.CANCEL_BY_USER);
            orders.setStatus(Orders.CANCELLED);
            orders.setPayStatus(Orders.REFUND);
            orderMapper.update(orders);
        }else {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

    }

    @Override
    public void reminder(Long id) {
        //1.判断能够进行催单的状态
        Orders orders = orderMapper.selectById(id);

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer status = orders.getStatus();

        if (status.equals(Orders.PENDING_PAYMENT)
                || status.equals(Orders.COMPLETED)
                || status.equals(Orders.CANCELLED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }
}
