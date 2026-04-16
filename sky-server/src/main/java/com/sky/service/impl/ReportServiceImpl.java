package com.sky.service.impl;

import com.sky.service.ReportService;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间区间内的营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            //日期计算，计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额数据，营业额是指：状态为“已完成”的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(amount) from orders where order_time > beginTime and order_time < endTime and status = 5
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end之间的每天对应的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的新增用户数量 select count(id) from user where create_time < ? and create_time > ?
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的总用户数量 select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);

            //总用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        //封装结果数据
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end之间的每天对应的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        //遍历dateList集合，查询每天的有效订单数和订单总数
        for (LocalDate date : dateList) {
            //查询每天的订单总数 select count(id) from orders where order_time > ? and order_time < ?
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单数 select count(id) from orders where order_time > ? and order_time < ? and status = 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        //计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            //计算订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return  OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return orderMapper.countByMap(map);
    }

    /**
     * 统计指定时间区间内的销量排名前10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        //封装返回结果数据
        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出近30天的运营数据报表（基于 Apache POI 模板模式实现）
     * @param response 用于将生成的 Excel 文件写回浏览器下载
     **/
    public void exportBusinessData(HttpServletResponse response) {
        // 1. 计算时间范围：起止日期（前30天到昨天）
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        // 2. 查询概览数据：调用 WorkspaceService 获取总额、完成率等汇总信息
        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX)
        );

        // 3. 读取模板文件：通过类加载器从 resources/template 目录下获取模板输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            // 4. 核心对象：基于模板输入流，在内存中构建一个“工作簿”对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            // 5. 获取工作表：通过名称定位到模板中的第一个 Sheet 页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 6. 填充时间范围：定位到第 2 行第 2 列（索引从 0 开始，即 Row(1), Cell(1)）
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);

            // 7. 填充概览数据第一行（第 4 行）：营业额、订单完成率、新增用户
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());           // 营业额
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate()); // 完成率
            row.getCell(6).setCellValue(businessData.getNewUsers());             // 新增用户

            // 8. 填充概览数据第二行（第 5 行）：有效订单数、平均客单价
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());     // 有效订单
            row.getCell(4).setCellValue(businessData.getUnitPrice());           // 客单价

            // 9. 循环填充明细数据：近 30 天每一天的具体数值
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);

                // 每次循环重新查询当天的运营数据
                businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                // 定位明细行：从第 8 行开始（索引 7），随着 i 递增
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());                   // 日期
                row.getCell(2).setCellValue(businessData.getTurnover());         // 当日营业额
                row.getCell(3).setCellValue(businessData.getValidOrderCount()); // 当日有效订单
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate()); // 当日完成率
                row.getCell(5).setCellValue(businessData.getUnitPrice());         // 当日客单价
                row.getCell(6).setCellValue(businessData.getNewUsers());           // 当日新增用户
            }

            // 10. 文件写出：通过 HttpServletResponse 获取输出流，将内存中的 excel 对象写出
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 11. 资源释放：刷空缓存，关闭流，关闭工作簿（防止内存泄漏）
            out.flush();
            out.close();
            excel.close();

        } catch (IOException e) {
            // 异常处理：打印堆栈信息，生产环境建议记录到日志文件
            e.printStackTrace();
        }
    }
}
