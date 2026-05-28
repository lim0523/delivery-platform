package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void add(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO pageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO find(Long id);

    void update(DishDTO dishDTO);

    void saleOrForbidden(Long id,Integer status);
}
