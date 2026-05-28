package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Override
    @Transactional
    public void add(DishDTO dishDTO) {
        //1.先将菜品的基本信息插入菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        //2.将菜品口味存入对应表，先获取当前菜品的ID
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&!flavors.isEmpty()){
            flavors.forEach(flavor->{
                flavor.setDishId(dish.getId());
            });
        }
        dishFlavorMapper.insertBatch(flavors);

    }

    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO pageQueryDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(pageQueryDTO,dish);
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        List<Dish> dishList =dishMapper.page(dish);
        Page pages= (Page) dishList;
        return new PageResult(pages.getTotal(),pages.getResult());
    }
}
