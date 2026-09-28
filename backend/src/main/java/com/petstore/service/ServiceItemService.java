package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petstore.common.BusinessException;
import com.petstore.common.PageResult;
import com.petstore.entity.BillItem;
import com.petstore.entity.ServiceItem;
import com.petstore.mapper.BillItemMapper;
import com.petstore.mapper.ServiceItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/** 服务项目管理（洗护/寄养/医疗保健等） */
@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;
    private final BillItemMapper billItemMapper;

    public PageResult<ServiceItem> page(long current, long size, String keyword, Integer status) {
        LambdaQueryWrapper<ServiceItem> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(keyword), ServiceItem::getName, keyword);
        w.eq(status != null, ServiceItem::getStatus, status);
        w.orderByDesc(ServiceItem::getId);
        return PageResult.of(serviceItemMapper.selectPage(new Page<>(current, size), w), s -> s);
    }

    /** 开单用：在架服务列表 */
    public List<ServiceItem> listOnShelf() {
        return serviceItemMapper.selectList(new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1).orderByDesc(ServiceItem::getId));
    }

    public void create(ServiceItem item) {
        item.setId(null);
        if (item.getStatus() == null) {
            item.setStatus(1);
        }
        serviceItemMapper.insert(item);
    }

    public void update(ServiceItem item) {
        if (serviceItemMapper.selectById(item.getId()) == null) {
            throw BusinessException.error("服务项目不存在");
        }
        serviceItemMapper.updateById(item);
    }

    public void delete(Long id) {
        if (serviceItemMapper.selectById(id) == null) {
            throw BusinessException.error("服务项目不存在");
        }
        if (billItemMapper.selectCount(new LambdaQueryWrapper<BillItem>()
                .eq(BillItem::getItemType, "SERVICE").eq(BillItem::getRefId, id)) > 0) {
            throw BusinessException.error("该服务已有账单引用，建议下架而非删除");
        }
        serviceItemMapper.deleteById(id);
    }
}
