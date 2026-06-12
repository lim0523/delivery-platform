package com.sky.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Builder
@Data
public class OrdersStatisticsDTO {

    /**
     * 开始时间
     */
    private LocalDateTime beginDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 用户id
     */
    private Long userId;
}