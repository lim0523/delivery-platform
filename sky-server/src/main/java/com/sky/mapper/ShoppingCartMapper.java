package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    void add(ShoppingCart shoppingCart);

    List<ShoppingCart> list(ShoppingCart shoppingCart);
    @Update("update shopping_cart set number=number+1 where id=#{id}")
    void addNum(ShoppingCart shoppingCart);
@Delete("delete from shopping_cart where id =#{id}")
    void deleteById(ShoppingCart cart);

@Update("update shopping_cart set number=number-1 where id=#{id}")
    void subNum(ShoppingCart cart);

@Delete("delete from shopping_cart where user_id=#{userId}")
    void clean(Long userId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
