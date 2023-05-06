package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {
        @Autowired
        private OrdersService ordersService;
        @Autowired
        private UserService userService;

        @Autowired
        private AddressBookService addressBookService;
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String number, String beginTime, String endTime){
        log.info(beginTime);
        log.info(endTime);
        Page<Orders> o=new Page<>(page,pageSize);
        Page<OrdersDto> os=new Page<>();

        LambdaQueryWrapper<Orders> w=new LambdaQueryWrapper<>();
        w.like(!StringUtils.isEmpty(number),Orders::getNumber,number);
        w.between(!StringUtils.isEmpty(beginTime),Orders::getOrderTime,beginTime,endTime);

        ordersService.page(o,w);

        BeanUtils.copyProperties(o,os,"records");

        List<Orders> records= o.getRecords();
        List<OrdersDto> list= records.stream().map((item)->{
            OrdersDto dto=new OrdersDto();

            BeanUtils.copyProperties(item,dto);

            Long userid=item.getUserId();

            User user = userService.getById(userid);

            if (user!=null){
                dto.setUserName(user.getName());
            }

            Long addid=item.getAddressBookId();

            AddressBook addrss = addressBookService.getById(addid);
            if (addrss!=null){
                String Provin=addrss.getProvinceName()==null?"":addrss.getProvinceName();
                String City=addrss.getCityName()==null?"":addrss.getCityName();
                String District=addrss.getDistrictName()==null?"":addrss.getDistrictName();
                dto.setAddress(Provin+City+District+addrss.getDetail());
                if (addrss.getPhone()!=null){
                    dto.setPhone(addrss.getPhone());
                }
                if (addrss.getConsignee()!=null){
                    dto.setConsignee(addrss.getConsignee());
                }
            }

            return dto;
        }).collect(Collectors.toList());

        os.setRecords(list);
        return R.success(os);
    }


}

