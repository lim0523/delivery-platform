package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailsMapper {
    void insertBatch(List<OrderDetail> orderDetailList);

    List<OrderDetail> selectAllDetails(List<Long> orderIdList);

@Select("select * from order_detail where order_id=#{id}")
    List<OrderDetail> selectDetailsById(Long id);

@Delete("delete from order_detail where order_id=#{id}")
    void deleteBatchByOId(Long id);
}
