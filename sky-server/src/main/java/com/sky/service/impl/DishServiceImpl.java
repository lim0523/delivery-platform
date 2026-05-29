package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetMealDishMapper mealDishMapper;

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
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        List<DishVO> dishList =dishMapper.page(pageQueryDTO);
        Page pages= (Page) dishList;
        return new PageResult(pages.getTotal(),pages.getResult());
    }

    /**
     * 批量删除菜品，删除前两个判断+删除时事务管理
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //1.判断菜品状态
        ids.forEach(id->{
            if(dishMapper.onOrOff(id)==1){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        //2.判断是否存在套餐与当前菜品关联
        List<Long> longs = mealDishMapper.selectByDishId(ids);
        if(longs!=null&&!longs.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //菜品能删除的情况下，删除菜品及其对应的口味
        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatch(ids);

    }

    /**
     *修改菜品信息时的查询回显
     */
    @Override
    public DishVO find(Long id) {
        return dishMapper.find(id);
    }

    /**
     *修改菜品信息
     * Controller 接收 DishDTO
     *         ↓
     * Service 开事务
     *         ↓
     * 更新 dish 表基本信息
     *         ↓
     * 删除原来的 dish_flavor
     *         ↓
     * 重新批量插入新的 flavors
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        //1.更新dish基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //2.删除原来的dish_flavor
        List<Long> list = new ArrayList<>();
        list.add(dishDTO.getId());
        dishFlavorMapper.deleteBatch(list);
        //3.批量插入新的flavors
        List<DishFlavor> flavors =dishDTO.getFlavors();
        if (flavors!=null&&!flavors.isEmpty()) {
            flavors.forEach(flavor->{
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void saleOrForbidden(Long id,Integer status) {
        dishMapper.saleOrForbidden(id,status);
    }

    @Override
    public List<DishVO> findList(Integer categoryId) {
        return dishMapper.selectBatch(categoryId);
    }
}
