package com.petstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 开单入参：明细只传引用与数量，价格由服务端按现价快照 */
@Data
public class BillCreateDTO {

    @NotNull(message = "请选择客户")
    private Long customerId;

    /** 本次服务对象（可选） */
    private Long petId;

    /** 整单优惠（抹零/折扣） */
    @DecimalMin(value = "0", message = "优惠金额不能为负")
    private BigDecimal discount;

    private String remark;

    @NotEmpty(message = "请至少添加一个明细")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        /** PRODUCT / SERVICE */
        @NotBlank(message = "明细类型不合法")
        private String itemType;

        @NotNull(message = "请选择商品或服务")
        private Long refId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
