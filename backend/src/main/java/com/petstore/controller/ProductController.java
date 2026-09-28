package com.petstore.controller;

import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.entity.Product;
import com.petstore.entity.ProductCategory;
import com.petstore.service.ProductService;
import com.petstore.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/page")
    public Result<PageResult<ProductVO>> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(required = false) Integer status) {
        return Result.ok(productService.page(current, size, keyword, categoryId, status));
    }

    /** 开单用：在架商品 */
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.ok(productService.listOnShelf());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid Product product) {
        productService.create(product);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid Product product) {
        productService.update(product);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    /* ========== 分类 ========== */

    @GetMapping("/category/list")
    public Result<List<ProductCategory>> categories() {
        return Result.ok(productService.categories());
    }

    @PostMapping("/category")
    public Result<Void> createCategory(@RequestBody @Valid ProductCategory category) {
        productService.createCategory(category);
        return Result.ok();
    }

    @PutMapping("/category")
    public Result<Void> updateCategory(@RequestBody @Valid ProductCategory category) {
        productService.updateCategory(category);
        return Result.ok();
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return Result.ok();
    }
}
