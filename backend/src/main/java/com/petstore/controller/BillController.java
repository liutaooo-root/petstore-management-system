package com.petstore.controller;

import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.dto.BillCreateDTO;
import com.petstore.service.BillService;
import com.petstore.vo.BillDetailVO;
import com.petstore.vo.BillVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public Result<BillDetailVO> create(@RequestBody @Valid BillCreateDTO dto) {
        return Result.ok(billService.create(dto));
    }

    /** 收款：事务内扣库存、记积分、升级会员等级 */
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id, @RequestParam String method) {
        billService.pay(id, method);
        return Result.ok();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        billService.cancel(id);
        return Result.ok();
    }

    @GetMapping("/page")
    public Result<PageResult<BillVO>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String billNo,
                                           @RequestParam(required = false) String type,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String beginDate,
                                           @RequestParam(required = false) String endDate,
                                           @RequestParam(required = false) Long customerId) {
        return Result.ok(billService.page(current, size, billNo, type, status, beginDate, endDate, customerId));
    }

    @GetMapping("/{id}")
    public Result<BillDetailVO> detail(@PathVariable Long id) {
        return Result.ok(billService.detail(id));
    }
}
