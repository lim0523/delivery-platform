package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     *新增员工
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "VALUES (#{id}, #{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    /**
     * 分页查询
     */
    List<Employee> selectByPage(EmployeePageQueryDTO empDTO);

    /**
     * 根据id修改员工status
     */
@Update("update employee set status=#{status} where id=#{id}")
    void startOrStop(Integer status, Long id);

    /**
     * 根据id修改员工(可复用）
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     *查询员工
     */
    @Select("select id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user from employee where id =#{id}")
    Employee selectById(Integer id);
}
