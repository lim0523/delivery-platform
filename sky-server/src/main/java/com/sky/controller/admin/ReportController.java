package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Api("数据统计相关接口")
@Slf4j
@RestController
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    ReportService reportService;

    /**
     * 统计营业额
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("统计营业额")
    public Result<TurnoverReportVO> getTurnover(@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate begin,@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate end){
            log.info("统计{}到{}的营业额",begin,end);
           TurnoverReportVO reportVO= reportService.getTurnover(begin,end);
            return Result.success(reportVO);
    }
    /**
     *统计用户
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> getUser(@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate begin,@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate end){
        log.info("统计{}到{}的用户数",begin,end);
        UserReportVO userReportVO=reportService.getUser(begin,end);
        return Result.success(userReportVO);
    }
    /**
     * 统计订单
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> getOrder(@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate begin,@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate end){
        log.info("统计{}到{}的订单数据",begin,end);
        OrderReportVO orderReportVO=reportService.getOrder(begin,end);
        return Result.success(orderReportVO);
    }
    /**
     * 统计销量top10
     */
    @GetMapping("/top10")
    @ApiOperation("统计销量top10")
    public  Result<SalesTop10ReportVO> getSaleTop10(@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate begin,@DateTimeFormat(pattern ="yyyy-MM-dd" ) LocalDate end){
        log.info("统计{}到{}的销量前10",begin,end);
        SalesTop10ReportVO salesTop10ReportVO=reportService.getTop10(begin,end);
        return Result.success(salesTop10ReportVO);
    }
}
