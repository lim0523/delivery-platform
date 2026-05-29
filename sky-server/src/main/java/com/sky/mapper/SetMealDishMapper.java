package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    List<Long> selectByDishId(List<Long> ids);

    void insertBatch(@Param("setMealDishes")List<SetmealDish> setMealDishes);

    void deleteBatch(List<Long> ids);
@Select("select id, setmeal_id, dish_id, name, price, copies " +
        "from setmeal_dish where setmeal_id =#{id}")
    List<SetmealDish> findBySmId(Long id);
}
