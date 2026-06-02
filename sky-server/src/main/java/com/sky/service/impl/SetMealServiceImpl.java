package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {
    @Autowired
    SetMealMapper setMealMapper;
    @Autowired
    SetMealDishMapper setMealDishMapper;
    @Autowired
    DishMapper dishMapper;
    @Transactional
    @Override
    public void add(SetmealDTO setmealDTO) {
        //1.先添加基本信息到setMeal表
        Setmeal setmeal =new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setMealMapper.insert(setmeal);
        //2.将setMealDishes信息提交到关系表
        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes();
        setMealDishes.forEach(setmealDish -> {
            setmealDish.setSetMealId(setmeal.getId());
        });
        setMealDishMapper.insertBatch(setMealDishes);

    }

    /**
     *分页查询
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        List<SetmealVO> voList =setMealMapper.page(setmealPageQueryDTO);
        Page pages =(Page) voList;
        return new PageResult(pages.getTotal(),pages.getResult());
    }

    /**
     *批量删除
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        //1.先删套餐菜品关系表对应信息
        setMealDishMapper.deleteBatch(ids);
        //1.再删套餐表基本信息
        setMealMapper.deleteBatch(ids);
    }

    @Override
    public SetmealVO find(Long id) {
       //1.查询基本信息
    SetmealVO setmealVO=setMealMapper.findById(id);
        if (setmealVO == null) {
            return null;
        }
    //2.查询关联菜品信息
        List<SetmealDish> setmealDishes=setMealDishMapper.findBySmId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     *修改套餐信息
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        //1.先修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setMealMapper.update(setmeal);
        //2.统一删除套餐关联菜品
        List<Long> ids=new ArrayList<>();
        ids.add(setmealDTO.getId());
        setMealDishMapper.deleteBatch(ids);
        //3.更新套餐关联菜品信息
        // 3. 插入新的套餐-菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetMealId(setmealDTO.getId());
            });
            setMealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public void saleOrForbidden(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status ==StatusConstant.ENABLE){
        List<Dish> dishList =dishMapper.selectBySMId(id);
        dishList.forEach(dish -> {
            if(dish.getStatus()==StatusConstant.DISABLE)
            {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        });
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setMealMapper.saleOrForbidden(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setMealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetmealId(id);
    }
}

