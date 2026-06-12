package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.ShopProperties;
import com.sky.result.PageResult;
import com.sky.server.WebSocketServer;
import com.sky.service.OrderService;
import com.sky.utils.BaiduMapUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
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
import java.util.*;
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
    @Autowired
    BaiduMapUtil baiduMapUtil;
    @Autowired
    ShopProperties shopProperties;
    @Autowired
    WebSocketServer webSocketServer;
    private void cancelOrderCommon(Long id, String cancelReason, List<Integer> allowedStatusList) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!allowedStatusList.contains(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(cancelReason);
        orders.setStatus(Orders.CANCELLED);

        orders.setPayStatus(
                orders.getPayStatus().equals(Orders.PAID)
                        ? Orders.REFUND
                        : Orders.UN_PAID
        );

        orderMapper.update(orders);
    }
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
        //插入详细地址
        String address = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();
        orders.setAddress(address);
        //1.获取门店坐标
        String shopLocation =
                baiduMapUtil.getLocation(
                        shopProperties.getAddress());

        //2.获取用户坐标
        String userLocation =
                baiduMapUtil.getLocation(
                        addressBook.getDetail());

    //3.计算距离
        Integer distance =
                baiduMapUtil.getDistance(
                        shopLocation,
                        userLocation);

    //4.校验
        if(distance > 5000){
            throw new OrderBusinessException("超出配送范围");
        }
            //要设置主键回显，用于明细表的数据
        orderMapper.insert(orders);
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
        //通过websocket向客户端推送数据：type orderId content
        Map map=new HashMap<>();
        map.put("type",1);//1 来单提醒 2 用户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号："+outTradeNo);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
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
        List<OrderDetail> orderDetailList = orderDetailsMapper.selectDetailsById(id);
        if (orderDetailList==null&&!orderDetailList.isEmpty()){
            String orderDishes = orderDetailList.stream()
                    .map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber())
                    .collect(Collectors.joining(";"));
           orderVO.setOrderDishes(orderDishes);
        }
        orderVO.setOrderDetailList(orderDetailList);
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

    /**
     * 用户取消,三张状态下被允许
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {
        cancelOrderCommon(
                id,
                MessageConstant.CANCEL_BY_USER,
                Arrays.asList(
                        Orders.PENDING_PAYMENT,
                        Orders.TO_BE_CONFIRMED,
                        Orders.CONFIRMED
                )
        );
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
        Map map=new HashMap<>();
        map.put("type",2);//1 来单提醒 2 用户催单
        map.put("orderId",id);
        map.put("content","订单号："+orders.getNumber());

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

    }
    /**
     * 商家取消订单，两种状态下被允许
     *
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        cancelOrderCommon(
                ordersCancelDTO.getId(),
                ordersCancelDTO.getCancelReason(),
                Arrays.asList(
                        Orders.TO_BE_CONFIRMED,
                        Orders.CONFIRMED
                )
        );
    }

    /**
     * 统计订单状态
     * @return
     */
    @Override
    public OrderStatisticsVO getStatus() {

        Integer confirmed =
                orderMapper.countByStatus(Orders.CONFIRMED);

        Integer deliveryInProgress =
                orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        Integer toBeConfirmed =
                orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);

        return OrderStatisticsVO.builder()
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .toBeConfirmed(toBeConfirmed)
                .build();
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void finishOrder(Long id) {
        Orders testOrder = orderMapper.selectById(id);
        if (testOrder==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders =Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 分页查询+搜索
     * @param dto
     * @return
     */
    // todo 梳理一下代码中流的使用过程
    @Override
    public PageResult page(OrdersPageQueryDTO dto) {
        List<OrderVO> orderVOList =new ArrayList<>();
        //1.基本字段查询
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
       List<Orders> orders= orderMapper.page(dto);
        Page page =(Page)  orders;
       if (orders==null||orders.isEmpty()){
           return new PageResult(page.getTotal(),new ArrayList<>());
       }
        //2.查询菜品信息，建立对应关系的map集合
        List<Long> orderIdList = orders.stream().map(Orders::getId).collect(Collectors.toList());
        List<OrderDetail> orderDetailList = orderDetailsMapper.selectAllDetails(orderIdList);
        Map<Long, List<OrderDetail>> detailMap = orderDetailList.stream().collect(Collectors.groupingBy(OrderDetail::getOrderId));
        //3.封装vo集合
        List<OrderVO> voList = orders.stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            //将菜品信息集合转换为字符串
            List<OrderDetail> detailList = detailMap.get(order.getId());
            String orderDishes = detailList.stream()
                    .map(detail -> detail.getName() + "*" + detail.getNumber())
                    .collect(Collectors.joining(";"));
            orderVO.setOrderDishes(orderDishes);
            return orderVO;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(),voList);
    }

    /**
     * 商家接单
     * @param confirmDTO
     */
    @Override
    public void confirmOrder(OrdersConfirmDTO confirmDTO) {
        Orders orders =new Orders();
        BeanUtils.copyProperties(confirmDTO,orders);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 商家派单
     * @param id
     */
    @Override
    public void deliveryOrder(Long id) {
        Orders orders=new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 商家拒单
     */
    @Override
    public void rejectOrder(OrdersRejectionDTO dto) {
        cancelOrderCommon(
                dto.getId(),
                dto.getRejectionReason(),
                Arrays.asList(
                        Orders.TO_BE_CONFIRMED
                )
        );
    }

}
