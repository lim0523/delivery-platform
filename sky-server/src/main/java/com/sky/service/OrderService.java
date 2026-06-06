package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO submitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult getHistoryOrders(OrdersPageQueryDTO queryDTO);

    OrderVO getInfoById(Long id);

    void repeatOrder(Long id);

    void cancelOrder(Long id);

    void reminder(Long id);

    void rejectOrder(OrdersRejectionDTO dto);

    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    OrderStatisticsVO getStatus();

    void finishOrder(Long id);

    PageResult page(OrdersPageQueryDTO dto);

    void confirmOrder(OrdersConfirmDTO confirmDTO);

    void deliveryOrder(Long id);
}
