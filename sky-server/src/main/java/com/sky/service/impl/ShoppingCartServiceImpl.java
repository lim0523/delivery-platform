
package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetMealMapper setMealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        if(shoppingCartDTO.getSetmealId()==null&&shoppingCartDTO.getDishId()==null){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        //1.先判断是否存在相同的菜品+口味或者相同的套餐，如果存在执行数量+1，不存在插入
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list!=null&&!list.isEmpty()){
            //存在相同数据，进行加一
            ShoppingCart cart = list.get(0);
            shoppingCartMapper.addNum(cart);
            return;
        }

        //2.如果不存在，需要根据dishId或者setmealId查询菜品/套餐信息，补全购物车基本字段
        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            Setmeal setmeal = setMealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }

        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setNumber(1);
        shoppingCartMapper.add(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list() {
        //1.获取当前用户id
        Long userId=BaseContext.getCurrentId();
        //2.调用mapper层方法进行查询
        ShoppingCart shoppingCart =new ShoppingCart();
        shoppingCart.setUserId(userId);
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        if(shoppingCartDTO.getSetmealId()==null&&shoppingCartDTO.getDishId()==null){
            throw new ShoppingCartBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //1.先查询对应的菜品/套餐数量
        ShoppingCart shoppingCart =new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //判断数量是否大于1
        ShoppingCart cart = list.get(0);
        if (cart.getNumber()==1){
            shoppingCartMapper.deleteById(cart);
            return;
        }
        shoppingCartMapper.subNum(cart);

    }

    @Override
    public void clean() {
        Long useId = BaseContext.getCurrentId();
        shoppingCartMapper.clean(useId);

    }
}
