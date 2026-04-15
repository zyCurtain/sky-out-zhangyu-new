package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private OrderDetailMapper detailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;



    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO submitDTO) {
        // 获取地址簿对象
        AddressBook addressBook = addressBookMapper.getById(submitDTO.getAddressBookId());
        // 保证提交订单的时候地址簿不为空
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        // 保证购物车内容不为空
        List<ShoppingCart> shoppingCartList = shopCartMapper.listCart(BaseContext.getCurrentId());
        if (shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 1、复制前端传输的相关订单信息进行订单表数据插入
        Orders order = new Orders();
        BeanUtils.copyProperties(submitDTO,order);
        // 1.1 补充订单表相关信息
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(BaseContext.getCurrentId());
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());
        //向订单表插入1条数据
        orderMapper.insertOne(order); // 需要返回订单id方便后续明细表获取

        // 2、进行订单明细表的插入（其实就是某个订单当中若干商品的具体配置信息）
        List<OrderDetail> detailList = new ArrayList<>();
        shoppingCartList.forEach(cart ->{
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(cart,detail);
            detail.setId(null); // 因为两个对象当中都有id字段但含义不同
            detail.setOrderId(order.getId());
            detailList.add(detail);
        });
        detailMapper.insertBatch(detailList); // 批量插入
        // 情况购物车数据
        shopCartMapper.clean(BaseContext.getCurrentId());
        // 返回目标对象
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderAmount(order.getAmount())
                .orderNumber(order.getNumber())
                .orderTime(order.getOrderTime())
                .build();
        return submitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
        // 因为没有商户ID所以我们直接模拟支付成功
        paySuccess(ordersPaymentDTO.getOrderNumber()); // 修改订单状态
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    public void checkOutOfRange(String address){
        // 请求参数当中的3个参数
        Map requestMap = new HashMap();
        requestMap.put("address",shopAddress); // 先获取店铺的经纬度坐标
        requestMap.put("output","json");
        requestMap.put("ak",ak);

        // 通过HttpClient发起get请求
        String resultJson = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3/?", requestMap);

        // 解析返回的JSON对象字符串
        JSONObject jsonObject = JSONObject.parseObject(resultJson); // 将字符串转换成JSON对象
        log.info("已经拿到店铺地址JSON：{}",jsonObject);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }
        /**
         * json格式为：
         * {
         *   "status": 0,
         *   "result": {
         *     "location": {
         *       "lng": 116.30787799999993,
         *       "lat": 40.05702706489032
         *     },
         *     "precise": 1,
         *     "confidence": 100,
         *     "comprehension": 100,
         *     "level": ""
         *   }
         * }
         */
        // 获取result当中的location当中的经纬度
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng; // 之所以这样拼接也是因为后续的路线规划当中百度API的接收就是需要这种形式
        // 再获取用户的配送地址经纬度坐标
        requestMap.put("address",address);
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3/?", requestMap);
        log.info("已经拿到配送地址JSON：{}",userCoordinate);


        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        Map map = new LinkedHashMap<String, String>();
        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("ak",ak);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/direction/v2/driving?", map);
        log.info("已经拿到规划JSON：{}",json);


        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }

    }

}
