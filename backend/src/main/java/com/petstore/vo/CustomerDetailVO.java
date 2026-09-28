package com.petstore.vo;

import com.petstore.entity.Bill;
import com.petstore.entity.Customer;
import com.petstore.entity.Pet;
import lombok.Data;

import java.util.List;

/** 客户详情抽屉：客户信息 + 名下宠物 + 近5笔账单 */
@Data
public class CustomerDetailVO {

    private Customer customer;

    private List<Pet> pets;

    private List<BillVO> recentBills;
}
