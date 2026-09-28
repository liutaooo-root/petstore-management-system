package com.petstore.vo;

import com.petstore.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商品列表视图：附加分类名 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends Product {

    private String categoryName;
}
