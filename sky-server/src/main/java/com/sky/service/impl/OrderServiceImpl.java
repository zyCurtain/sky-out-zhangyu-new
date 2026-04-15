package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShopCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private OrderDetailMapper detailMapper;



    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO submitDTO) {
        // 获取地址簿对象
        AddressBook addressBook = addressBookMapper.getById(submitDTO.getAddressBookId());
        // 保证提交订单的时候地址簿不为空
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
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
}
