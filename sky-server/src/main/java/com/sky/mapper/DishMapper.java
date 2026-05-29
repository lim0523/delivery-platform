package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
@AutoFill(value = OperationType.INSERT)
@Options(useGeneratedKeys = true, keyProperty = "id")
@Insert("insert into dish " +
        "(id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
        "values " +
        "(#{id}, #{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, " +
        "#{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Dish dish);

    /**
     *多表+分页查询
     */
    List<DishVO> page(DishPageQueryDTO pageQueryDTO);
    /**
     * 查询菜品状态
     */
@Select("select status from dish where id =#{id} ")
    Integer onOrOff(Long id);

    /**
     * 批量删除
     */
    void deleteBatch(List<Long> ids);

    /**
     *根据id查询
     */
    DishVO find(Long id);

    /**
     *修改菜品基本信息
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     *根据id修改状态
     */
    @Update("update dish set status =#{status} where id=#{id} ")
    void saleOrForbidden(Long id,Integer status);

    @Select("select id, name,  price, status " +
            " from dish where category_id=#{categoryId}")
    List<DishVO> selectBatch(Integer categoryId);
    @Select("select a.* from dish a left join setmeal_dish b " +
            "on a.id = b.dish_id where b.setmeal_id = #{id}")
    List<Dish> selectBySMId(Long id);
}
