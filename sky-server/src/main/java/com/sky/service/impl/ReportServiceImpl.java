package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrderReportDTO;
import com.sky.dto.OrdersStatisticsDTO;
import com.sky.dto.SalesTop10DTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailsMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import java.util.List;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    OrderDetailsMapper orderDetailsMapper;
    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        //1.拼接日期
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        String dateListString = StringUtils.join(dateList, ",");

        //2.根据日期范围查金额
        List<BigDecimal> turnoverList = dateList.stream().map(date -> {
            //一天开始
            LocalDateTime beginDate = date.atStartOfDay();
            //一天的结束
        LocalDateTime endDate = LocalDateTime.of(date, LocalTime.MAX);
            // LocalDateTime endDate = date.plusDays(1).atStartOfDay();

            //查询当天的所有金额

          return orderMapper.selectByCondition( OrdersStatisticsDTO.builder()
                  .beginDate(beginDate)
                  .endDate(endDate)
                  .status(Orders.COMPLETED)
                  .build());
        }).collect(Collectors.toList());

        //拼接
        String amountListString=StringUtils.join(turnoverList,",");

        return TurnoverReportVO.builder()
                .dateList(dateListString)
                .turnoverList(amountListString)
                .build();
    }

    @Override
    public UserReportVO getUser(LocalDate begin, LocalDate end) {
        //1.拼接日期字符串
        List<LocalDate> dateList=new ArrayList<>();
        List<Integer> sumUserCount=new ArrayList<>();

        LocalDate current=begin;
        while (!current.isAfter(end)){
            dateList.add(current);
            //2.查询当前日期前的总人数,当前第二天零点前
            LocalDateTime endDate=current.plusDays(1).atStartOfDay();
            Integer sum = userMapper.getByCreateTime(null, endDate);
            sumUserCount.add(sum);

            current=current.plusDays(1);
        }
        String dateListString=StringUtils.join(dateList,",");

        //3.查询特定日期前的总人数
        List<Integer> newUserCount=dateList.stream().map(date->{

            LocalDateTime beginSpecificDate=LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endSpecificDate=LocalDateTime.of(date,LocalTime.MAX);

            return userMapper.getByCreateTime(beginSpecificDate,endSpecificDate);
        }).collect(Collectors.toList());



        return UserReportVO.builder()
                .dateList(dateListString)
                .newUserList(StringUtils.join(newUserCount,","))
                .totalUserList(StringUtils.join(sumUserCount,","))
                .build();
    }

    @Override
    public OrderReportVO getOrder(LocalDate begin, LocalDate end) {
        //1.拼接日期字符串
        List<LocalDate> dateList=new ArrayList<>();
        List<Integer> orderList=new ArrayList<>();
        List<Integer> solidOrderList=new ArrayList<>();

        LocalDate current=begin;
        while (!current.isAfter(end)){
            //2.拼接查询总订单数的dto
            OrderReportDTO sumDTO = OrderReportDTO.builder()
                    .beginDate(LocalDateTime.of(current, LocalTime.MIN))
                    .endDate(LocalDateTime.of(current.plusDays(1), LocalTime.MIN)).build();
            //3.查询总订单数
            Integer orderCount=orderMapper.selectCountByStatusAndOrderTime(sumDTO);
            orderList.add(orderCount);
            //4.查询有效订单数
            OrderReportDTO solidDTO=new OrderReportDTO();
            BeanUtils.copyProperties(sumDTO,solidDTO);
            solidDTO.setStatus(Orders.COMPLETED);
            Integer solidOrderCount =orderMapper.selectCountByStatusAndOrderTime(solidDTO);
            solidOrderList.add(solidOrderCount);

            dateList.add(current);
            current=current.plusDays(1);
        }
        Integer totalCount = orderList.stream().reduce(0, Integer::sum);
        Integer validCount = solidOrderList.stream().reduce(0, Integer::sum);
        //计算百分比时一定要注意排出除0异常
        double orderCompletionRate =
                totalCount == 0
                        ? 0.0
                        : validCount * 1.0 / totalCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderList,","))
                .validOrderCountList(StringUtils.join(solidOrderList,","))
                .totalOrderCount(totalCount)
                .validOrderCount(validCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginDate=LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endDate=LocalDateTime.of(end.plusDays(1),LocalTime.MIN);

        SalesTop10DTO salesTop10DTO = SalesTop10DTO.builder()
                .status(Orders.COMPLETED)
                .beginDate(beginDate)
                .endDate(endDate).build();
        List<GoodsSalesDTO>  list =orderDetailsMapper.selectTopByDate(salesTop10DTO);
// todo 回顾一下流的收集
        String nameListString=
                StringUtils.join(
                        list.stream()
                                .map(GoodsSalesDTO::getName)
                                .collect(Collectors.toList()),
                        ","
                );

        String numberListString =
                StringUtils.join(
                        list.stream()
                                .map(GoodsSalesDTO::getNumber)
                                .collect(Collectors.toList()),
                        ","
                );

        return SalesTop10ReportVO.builder()
                .numberList(numberListString)
                .nameList(nameListString)
                .build();

    }
}
