package com.petstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petstore.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    /** 收款时原子扣库存：库存不足或商品已下架则影响行数为 0，事务回滚 */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND status = 1 AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
