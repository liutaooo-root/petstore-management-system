package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petstore.common.BusinessException;
import com.petstore.common.PageResult;
import com.petstore.entity.BillItem;
import com.petstore.entity.Product;
import com.petstore.entity.ProductCategory;
import com.petstore.mapper.BillItemMapper;
import com.petstore.mapper.ProductCategoryMapper;
import com.petstore.mapper.ProductMapper;
import com.petstore.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 商品与分类管理 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final BillItemMapper billItemMapper;

    /* ==================== 分类 ==================== */

    public List<ProductCategory> categories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>().orderByAsc(ProductCategory::getSort));
    }

    public void createCategory(ProductCategory category) {
        checkCategoryName(category.getName(), null);
        category.setId(null);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.insert(category);
    }

    public void updateCategory(ProductCategory category) {
        if (categoryMapper.selectById(category.getId()) == null) {
            throw BusinessException.error("分类不存在");
        }
        checkCategoryName(category.getName(), category.getId());
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw BusinessException.error("分类不存在");
        }
        if (productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id)) > 0) {
            throw BusinessException.error("该分类下存在商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    /* ==================== 商品 ==================== */

    public PageResult<ProductVO> page(long current, long size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(keyword), Product::getName, keyword);
        w.eq(categoryId != null, Product::getCategoryId, categoryId);
        w.eq(status != null, Product::getStatus, status);
        w.orderByDesc(Product::getId);
        Page<Product> p = productMapper.selectPage(new Page<>(current, size), w);

        List<ProductCategory> categories = categories();
        Map<Long, String> categoryNames = categories.stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
        return PageResult.of(p, prod -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(prod, vo);
            vo.setCategoryName(categoryNames.getOrDefault(prod.getCategoryId(), "-"));
            return vo;
        });
    }

    /** 开单用：在架商品列表 */
    public List<Product> listOnShelf() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1).orderByDesc(Product::getId));
    }

    public void create(Product product) {
        requireCategory(product.getCategoryId());
        product.setId(null);
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);
    }

    public void update(Product product) {
        if (productMapper.selectById(product.getId()) == null) {
            throw BusinessException.error("商品不存在");
        }
        requireCategory(product.getCategoryId());
        productMapper.updateById(product);
    }

    public void delete(Long id) {
        if (productMapper.selectById(id) == null) {
            throw BusinessException.error("商品不存在");
        }
        if (billItemMapper.selectCount(new LambdaQueryWrapper<BillItem>()
                .eq(BillItem::getItemType, "PRODUCT").eq(BillItem::getRefId, id)) > 0) {
            throw BusinessException.error("该商品已有账单引用，建议下架而非删除");
        }
        productMapper.deleteById(id);
    }

    private void requireCategory(Long categoryId) {
        if (categoryId == null || categoryMapper.selectById(categoryId) == null) {
            throw BusinessException.error("所选分类不存在");
        }
    }

    private void checkCategoryName(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        LambdaQueryWrapper<ProductCategory> w =
                new LambdaQueryWrapper<ProductCategory>().eq(ProductCategory::getName, name);
        if (excludeId != null) {
            w.ne(ProductCategory::getId, excludeId);
        }
        if (categoryMapper.selectCount(w) > 0) {
            throw BusinessException.error("分类名称已存在");
        }
    }
}
