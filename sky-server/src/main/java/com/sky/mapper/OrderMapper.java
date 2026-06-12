package com.sky.mapper;

import com.sky.dto.OrderReportDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersStatisticsDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);
@Select("select * from orders where user_id=#{userId} order by order_time desc")
    List<Orders> getOrdersByUid(Long userId);
@Select("select * from orders where id=#{id}")
    Orders selectById(Long id);
@Delete("delete from orders where id=#{id}")
    void deleteById(Long id);

    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    List<Orders> page(OrdersPageQueryDTO dto);

    /**
     * 查询符合支付超时条件的订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<=#{orderTime} ")
    List<Orders> selectByStatusAndOrderTime(Integer status, LocalDateTime orderTime);
/**
 * 查询指定日期内的订单金额总和，在sql就进行null处理ifnull(sum(amount),0)表示为空时传0
 */
//@Select("select ifnull(sum(amount),0) from orders where order_time between #{begin} and " +
//        "#{end} and status=5")
//BigDecimal selectTurnoverByDate(LocalDateTime begin,LocalDateTime end);

    BigDecimal selectByCondition(OrdersStatisticsDTO dto);

    /**
     * 根据时间和状态查询订单数
     * @param sumDTO
     * @return
     */
    Integer selectCountByStatusAndOrderTime(OrderReportDTO sumDTO);
}
