package com.petstore.vo;

import com.petstore.entity.Pet;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 宠物列表视图：附加主人姓名/手机号 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetVO extends Pet {

    private String customerName;

    private String customerPhone;
}
