package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {
    void add(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO pageQueryDTO);

    void deleteBatch(List<Long> ids);
}
