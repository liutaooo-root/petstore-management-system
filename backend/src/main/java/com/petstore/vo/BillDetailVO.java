package com.petstore.vo;

import com.petstore.entity.BillItem;
import lombok.Data;

import java.util.List;

/** 账单详情：账单头 + 明细 */
@Data
public class BillDetailVO {

    private BillVO bill;

    private List<BillItem> items;
}
