package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        // 使用spring框架提供的DigestUtils工具类对密码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void addEmp(EmployeeDTO dto) {
        // 先定义一个更大的对象用于保存需要insert的信息
        Employee employee = new Employee();
        // 使用BeanUtils拷贝属性值
        BeanUtils.copyProperties(dto,employee);
        // 对新对象进行赋值
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        employee.setStatus(StatusConstant.ENABLE); // 设置状态
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        // 获取当前操作用户的id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.addEmp(employee);
    }

    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        // ========== 步骤 1：开启分页 ==========
        // PageHelper.startPage() 方法会利用 ThreadLocal 存储分页参数
        // 参数说明：pageNum=当前页码，pageSize=每页显示记录数
        // 例如：page=1, pageSize=10 表示查询第 1 页，每页 10 条记录
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // ========== 步骤 2：执行分页查询 ==========
        // 调用 Mapper 方法时，PageHelper 会自动拦截 SQL 并添加分页语句
        // MySQL 示例：在原 SQL 后追加 LIMIT ?, ?
        // 返回值 Page<Employee> 是一个特殊的 List，既包含数据列表也包含总数信息
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

       // ========== 步骤 3：获取分页数据列表 ==========
       // getResult() 方法返回当前页的数据列表
       // 如果查询第 1 页且 pageSize=10，则返回第 1-10 条员工记录
       // 返回类型：List<Employee>
       List<Employee> employeeList = page.getResult();

       // ========== 步骤 4：获取总记录数 ==========
       // getTotal() 方法返回符合条件的总记录数（用于计算总页数）
       // 例如：数据库共有 95 条符合条件的员工记录，则 getTotal() = 95
       // 注意：PageHelper 会自动执行一条 SELECT COUNT(*) 查询来获取总数
       long total = page.getTotal();

       // ========== 步骤 5：封装分页结果 ==========
       // PageResult 是自定义的分页响应对象，包含：
       // - total: 总记录数（前端用于显示"共 X 条记录"或计算总页数）
       // - records: 当前页的数据列表（前端表格实际展示的数据）
       PageResult pageResult = new PageResult(total, employeeList);

       // ========== 返回结果示例 ==========
       // 假设数据库有 95 条记录，查询第 1 页（pageSize=10）
       // 返回的 JSON 数据结构：
       // {
       //     "code": 1,
       //     "msg": "操作成功",
       //     "data": {
       //         "total": 95,           // 总记录数
       //         "records": [           // 当前页的 10 条员工数据
       //             { "id": 1, "name": "张三", ... },
       //             { "id": 2, "name": "李四", ... },
       //             ... (共 10 条)
       //         ]
       //     }
       // }
       return pageResult;
       }

    @Override
    public void enable(Integer status, Long id) {
        // 因为还需涉及时间、修改人id的传输所以新建一个对象来赋值
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee); // 因为修改状态也是更新操作，那么不如直接编写完整修改策略，然后基于if判断来实现部分更新
    }

    @Override
    public Employee selectById(Integer id) {
        return employeeMapper.selectByID(id);
    }

    @Override
    public void editEmp(EmployeeDTO dto) {
        // 因为涉及修改所以也需要写入一些操作时间、操作人信息
        Employee employee = new Employee();
        BeanUtils.copyProperties(dto,employee);
        employeeMapper.update(employee);
    }


}
