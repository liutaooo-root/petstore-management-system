package com.petstore.mapper;

import com.petstore.vo.KV;
import com.petstore.vo.TopPoint;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 数据看板统计（仅统计已支付账单） */
public interface DashboardMapper {

    @Select("SELECT IFNULL(SUM(pay_amount),0) FROM bill WHERE status='PAID' AND DATE(pay_time)=CURDATE()")
    BigDecimal todayRevenue();

    @Select("SELECT IFNULL(SUM(pay_amount),0) FROM bill WHERE status='PAID' AND DATE_FORMAT(pay_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m')")
    BigDecimal monthRevenue();

    @Select("SELECT COUNT(*) FROM bill WHERE DATE(create_time)=CURDATE()")
    long todayBillCount();

    @Select("SELECT COUNT(*) FROM bill WHERE DATE_FORMAT(create_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m')")
    long monthBillCount();

    @Select("SELECT COUNT(*) FROM customer WHERE DATE_FORMAT(create_time,'%Y-%m')=DATE_FORMAT(NOW(),'%Y-%m')")
    long newCustomerThisMonth();

    @Select("SELECT DATE_FORMAT(pay_time,'%Y-%m-%d') AS k, IFNULL(SUM(pay_amount),0) AS v "
            + "FROM bill WHERE status='PAID' AND pay_time >= #{begin} "
            + "GROUP BY DATE_FORMAT(pay_time,'%Y-%m-%d') ORDER BY k")
    List<KV> revenueTrend(@Param("begin") LocalDateTime begin);

    /** 商品/服务营收占比（按明细类型拆分，混合账单分别计入） */
    @Select("SELECT bi.item_type AS k, IFNULL(SUM(bi.amount),0) AS v "
            + "FROM bill_item bi JOIN bill b ON b.id=bi.bill_id AND b.status='PAID' "
            + "GROUP BY bi.item_type")
    List<KV> revenueRatio();

    /** 热销商品/服务排行 */
    @Select("SELECT bi.item_name AS k, IFNULL(SUM(bi.quantity),0) AS qty, IFNULL(SUM(bi.amount),0) AS v "
            + "FROM bill_item bi JOIN bill b ON b.id=bi.bill_id AND b.status='PAID' "
            + "WHERE bi.item_type=#{itemType} "
            + "GROUP BY bi.ref_id, bi.item_name ORDER BY v DESC LIMIT #{limit}")
    List<TopPoint> topItems(@Param("limit") int limit, @Param("itemType") String itemType);
}
