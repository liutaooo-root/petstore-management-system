<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.billNo" placeholder="单号" clearable style="width: 180px" />
      <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 120px">
        <el-option v-for="(label, v) in BILL_TYPE" :key="v" :label="label" :value="v" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
        <el-option v-for="(label, v) in BILL_STATUS" :key="v" :label="label" :value="v" />
      </el-select>
      <el-date-picker
        v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
        start-placeholder="开始日期" end-placeholder="结束日期" style="width: 250px" clearable
      />
      <el-button type="primary" :icon="Search" @click="onQuery">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openCreate">开单收银</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="billNo" label="单号" width="175" />
      <el-table-column prop="customerName" label="客户" width="100" />
      <el-table-column prop="petName" label="宠物" width="90">
        <template #default="{ row }">{{ row.petName || '-' }}</template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="BILL_TYPE_TAG[row.type]" size="small">{{ BILL_TYPE[row.type] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="总额" width="100">
        <template #default="{ row }"><span class="money">{{ fmtMoney(row.totalAmount) }}</span></template>
      </el-table-column>
      <el-table-column label="优惠" width="80">
        <template #default="{ row }">{{ fmtMoney(row.discountAmount) }}</template>
      </el-table-column>
      <el-table-column label="实收" width="100">
        <template #default="{ row }">
          <span class="money">{{ row.payAmount == null ? '-' : fmtMoney(row.payAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="90">
        <template #default="{ row }">{{ row.payMethod ? PAY_METHOD[row.payMethod] : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="BILL_STATUS_TAG[row.status]" size="small">{{ BILL_STATUS[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="收银员" width="90" />
      <el-table-column prop="createTime" label="开单时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'UNPAID'" link type="success" @click="openPay(row)">收款</el-button>
          <el-button v-if="row.status === 'UNPAID'" link type="warning" @click="onCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 14px; justify-content: flex-end"
      layout="total, sizes, prev, pager, next"
      :total="total"
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      @current-change="load"
      @size-change="load"
    />

    <!-- 开单 -->
    <el-dialog v-model="createVisible" title="开单收银" width="760px" top="4vh">
      <el-form :model="billForm" label-width="80px">
        <el-row :gutter="12">
          <el-col :span="10">
            <el-form-item label="客户" required>
              <el-select v-model="billForm.customerId" filterable placeholder="选择客户" style="width: 100%" @change="onCustomerChange">
                <el-option v-for="c in customers" :key="c.id" :label="c.name + ' ' + c.phone" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="宠物">
              <el-select v-model="billForm.petId" clearable :disabled="!billForm.customerId" placeholder="服务对象" style="width: 100%">
                <el-option v-for="p in pets" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="优惠">
              <el-input-number v-model="billForm.discount" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 添加明细 -->
        <el-tabs v-model="pickTab">
          <el-tab-pane label="选择商品" name="PRODUCT">
            <div style="display: flex; gap: 8px; align-items: center">
              <el-select v-model="pick.productId" filterable placeholder="搜索商品（名称）" style="flex: 1">
                <el-option v-for="p in products" :key="p.id" :label="`${p.name}｜库存${p.stock}｜¥${p.price}`" :value="p.id" :disabled="p.stock <= 0" />
              </el-select>
              <el-input-number v-model="pick.qty" :min="1" :max="99" size="default" style="width: 100px" />
              <el-button type="primary" :icon="Plus" @click="addItem('PRODUCT')">添加</el-button>
            </div>
          </el-tab-pane>
          <el-tab-pane label="选择服务" name="SERVICE">
            <div style="display: flex; gap: 8px; align-items: center">
              <el-select v-model="pick.serviceId" filterable placeholder="搜索服务" style="flex: 1">
                <el-option v-for="s in services" :key="s.id" :label="`${s.name}｜¥${s.price}`" :value="s.id" />
              </el-select>
              <el-input-number v-model="pick.qty" :min="1" :max="99" size="default" style="width: 100px" />
              <el-button type="primary" :icon="Plus" @click="addItem('SERVICE')">添加</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>

        <el-table :data="billForm.items" size="small" border empty-text="尚未添加明细" style="margin-top: 12px">
          <el-table-column label="类型" width="70">
            <template #default="{ row }">{{ row.itemType === 'PRODUCT' ? '商品' : '服务' }}</template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="price" label="单价" width="90">
            <template #default="{ row }">{{ fmtMoney(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :max="99" size="small" controls-position="right" style="width: 90px" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">{{ fmtMoney(row.price * row.quantity) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="70">
            <template #default="{ $index }">
              <el-button link type="danger" @click="billForm.items.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-form-item label="备注" style="margin-top: 12px">
          <el-input v-model="billForm.remark" type="textarea" :rows="1" placeholder="可选" />
        </el-form-item>
      </el-form>

      <div class="sum-line">
        <span>合计金额：<b>{{ fmtMoney(totalAmount) }}</b></span>
        <span>优惠：-{{ fmtMoney(billForm.discount || 0) }}</span>
        <span class="payable">应收：<b>{{ fmtMoney(payable) }}</b></span>
      </div>

      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" :disabled="!billForm.items.length" @click="submitBill">确认开单</el-button>
      </template>
    </el-dialog>

    <!-- 收款 -->
    <el-dialog v-model="payVisible" title="账单收款" width="380px">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="单号">{{ current.billNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ current.customerName }}</el-descriptions-item>
        <el-descriptions-item label="应收金额">
          <span class="money">{{ fmtMoney(current.payable) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px" style="margin-top: 14px">
        <el-form-item label="支付方式">
          <el-radio-group v-model="payMethod">
            <el-radio-button v-for="(label, v) in PAY_METHOD" :key="v" :value="v">{{ label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="success" :loading="paying" @click="submitPay">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="detailVisible" title="账单详情" size="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="单号">{{ detail.bill?.billNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="BILL_STATUS_TAG[detail.bill?.status]" size="small">{{ BILL_STATUS[detail.bill?.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.bill?.customerName }}</el-descriptions-item>
        <el-descriptions-item label="宠物">{{ detail.bill?.petName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ BILL_TYPE[detail.bill?.type] }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.bill?.payMethod ? PAY_METHOD[detail.bill?.payMethod] : '-' }}</el-descriptions-item>
        <el-descriptions-item label="总额">{{ fmtMoney(detail.bill?.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="优惠">{{ fmtMoney(detail.bill?.discountAmount) }}</el-descriptions-item>
        <el-descriptions-item label="实收">
          <b>{{ detail.bill?.payAmount == null ? '-' : fmtMoney(detail.bill?.payAmount) }}</b>
        </el-descriptions-item>
        <el-descriptions-item label="收银员">{{ detail.bill?.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="开单时间">{{ detail.bill?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="收款时间">{{ detail.bill?.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.bill?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">消费明细</el-divider>
      <el-table :data="detail.items" size="small" border>
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ row.itemType === 'PRODUCT' ? '商品' : '服务' }}</template>
        </el-table-column>
        <el-table-column prop="itemName" label="名称" min-width="160" />
        <el-table-column prop="unitPrice" label="单价" width="90" />
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column prop="amount" label="小计" width="90" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageBills, createBill, payBill, cancelBill, getBill, listCustomers, listPets, listProducts, listServices } from '../api'
import { BILL_TYPE, BILL_TYPE_TAG, BILL_STATUS, BILL_STATUS_TAG, PAY_METHOD, fmtMoney } from '../utils/dict'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const paying = ref(false)
const dateRange = ref([])
const query = reactive({ current: 1, size: 10, billNo: '', type: null, status: null, beginDate: null, endDate: null, customerId: null })

const customers = ref([])
const pets = ref([])
const products = ref([])
const services = ref([])

const createVisible = ref(false)
const payVisible = ref(false)
const detailVisible = ref(false)
const detail = ref({})
const current = ref({})
const payMethod = ref('WECHAT')
const pickTab = ref('PRODUCT')
const pick = reactive({ productId: null, serviceId: null, qty: 1 })
const billForm = reactive({ customerId: null, petId: null, discount: 0, remark: '', items: [] })

const totalAmount = computed(() => billForm.items.reduce((s, i) => s + i.price * i.quantity, 0))
const payable = computed(() => Math.max(0, totalAmount.value - (billForm.discount || 0)))

async function load() {
  loading.value = true
  try {
    const data = await pageBills(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onQuery() {
  query.beginDate = dateRange.value?.[0] || null
  query.endDate = dateRange.value?.[1] || null
  query.current = 1
  load()
}

async function onCustomerChange() {
  billForm.petId = null
  pets.value = billForm.customerId ? await listPets(billForm.customerId) : []
}

function addItem(type) {
  const source = type === 'PRODUCT' ? products.value : services.value
  const id = type === 'PRODUCT' ? pick.productId : pick.serviceId
  const target = source.find(i => i.id === id)
  if (!target) {
    ElMessage.warning(type === 'PRODUCT' ? '请先选择商品' : '请先选择服务')
    return
  }
  const exist = billForm.items.find(i => i.itemType === type && i.refId === target.id)
  if (exist) {
    exist.quantity += pick.qty
  } else {
    billForm.items.push({
      itemType: type, refId: target.id, name: target.name,
      price: Number(target.price), quantity: pick.qty
    })
  }
  pick.productId = null
  pick.serviceId = null
  pick.qty = 1
}

function openCreate() {
  Object.assign(billForm, { customerId: null, petId: null, discount: 0, remark: '', items: [] })
  pets.value = []
  createVisible.value = true
}

async function submitBill() {
  if (!billForm.customerId) {
    ElMessage.warning('请先选择客户')
    return
  }
  submitting.value = true
  try {
    const created = await createBill({
      customerId: billForm.customerId,
      petId: billForm.petId || null,
      discount: billForm.discount || 0,
      remark: billForm.remark,
      items: billForm.items.map(i => ({ itemType: i.itemType, refId: i.refId, quantity: i.quantity }))
    })
    ElMessage.success(`开单成功，单号 ${created.bill.billNo}`)
    createVisible.value = false
    // 开完单直接进入收款，符合门店收银动线
    openPay(created.bill)
    load()
  } finally {
    submitting.value = false
  }
}

function openPay(row) {
  current.value = {
    id: row.id,
    billNo: row.billNo,
    customerName: row.customerName,
    payable: row.payAmount ?? (row.totalAmount - (row.discountAmount || 0))
  }
  payMethod.value = 'WECHAT'
  payVisible.value = true
}

async function submitPay() {
  paying.value = true
  try {
    await payBill(current.value.id, payMethod.value)
    ElMessage.success('收款成功')
    payVisible.value = false
    load()
  } finally {
    paying.value = false
  }
}

async function onCancel(row) {
  await ElMessageBox.confirm(`确定取消单号 ${row.billNo}？`, '提示', { type: 'warning' })
  await cancelBill(row.id)
  ElMessage.success('已取消')
  load()
}

async function openDetail(row) {
  detail.value = await getBill(row.id)
  detailVisible.value = true
}

onMounted(async () => {
  const [c, p, s] = await Promise.all([listCustomers(), listProducts(), listServices()])
  customers.value = c
  products.value = p
  services.value = s
  load()
})
</script>

<style scoped>
.sum-line {
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 4px 0;
  font-size: 14px;
  color: #606266;
}
.sum-line .payable { font-size: 16px; color: #f56c6c; }
.sum-line b { font-variant-numeric: tabular-nums; }
</style>
