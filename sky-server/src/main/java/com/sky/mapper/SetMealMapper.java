package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     *插入基本信息，注意要主键回显
     */
    @AutoFill(value = OperationType.INSERT)
    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into setmeal(" +
            "name, category_id, price, image, description, status, " +
            "create_time, update_time, create_user, update_user" +
            ") values (" +
            "#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, " +
            "#{createTime}, #{updateTime}, #{createUser}, #{updateUser}" +
            ")")
    void insert(Setmeal setmeal);

    List<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    /**
     *查询回显
     */
    SetmealVO findById(Long id);


@AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);


@AutoFill(value = OperationType.UPDATE)
    void saleOrForbidden(Setmeal setmeal);


    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
