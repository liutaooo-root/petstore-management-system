<template>
  <div class="page-card">
    <div class="toolbar">
      <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" placeholder="按日期筛选" clearable style="width: 170px" @change="load" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px" @change="load">
        <el-option v-for="(label, v) in APPT_STATUS" :key="v" :label="label" :value="v" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增预约</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="appointmentTime" label="预约时间" width="170" />
      <el-table-column prop="customerName" label="客户" width="110" />
      <el-table-column prop="petName" label="宠物" width="100" />
      <el-table-column prop="serviceName" label="服务项目" min-width="180" show-overflow-tooltip />
      <el-table-column prop="staffName" label="负责员工" width="110">
        <template #default="{ row }">{{ row.staffName || '待分配' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="APPT_STATUS_TAG[row.status]" size="small">{{ APPT_STATUS[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Right" @click="changeStatus(row, 'IN_PROGRESS', '确认到店')">到店</el-button>
          <el-button link type="success" @click="changeStatus(row, 'COMPLETED', '确认完成服务')">完成</el-button>
          <el-button link type="warning" @click="changeStatus(row, 'CANCELLED', '取消该预约')">取消</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '修改预约' : '新增预约'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" filterable placeholder="选择客户" style="width: 100%" @change="onCustomerChange">
            <el-option v-for="c in customers" :key="c.id" :label="c.name + ' ' + c.phone" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物" prop="petId">
          <el-select v-model="form.petId" placeholder="先选择客户" :disabled="!form.customerId" style="width: 100%">
            <el-option v-for="p in pets" :key="p.id" :label="p.name + '（' + p.breed + '）'" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务项目" prop="serviceId">
          <el-select v-model="form.serviceId" style="width: 100%">
            <el-option v-for="s in services" :key="s.id" :label="s.name + ' ¥' + s.price" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdmin" label="负责员工">
          <el-select v-model="form.staffId" clearable placeholder="可不分配" style="width: 100%">
            <el-option v-for="u in staffs" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker
            v-model="form.appointmentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择到店时间" style="width: 100%" :disabled-date="d => d < new Date(new Date().setHours(0,0,0,0))"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Right } from '@element-plus/icons-vue'
import {
  pageAppointments, createAppointment, updateAppointment,
  changeAppointmentStatus, deleteAppointment,
  listCustomers, listPets, listServices, pageUsers
} from '../api'
import { useUserStore } from '../stores/user'
import { APPT_STATUS, APPT_STATUS_TAG } from '../utils/dict'

const store = useUserStore()
const isAdmin = computed(() => store.isAdmin)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const customers = ref([])
const pets = ref([])
const services = ref([])
const staffs = ref([])
const query = reactive({ current: 1, size: 10, status: null, date: null })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null, customerId: null, petId: null, serviceId: null,
  staffId: null, appointmentTime: '', remark: ''
})
const rules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  serviceId: [{ required: true, message: '请选择服务项目', trigger: 'change' }],
  appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const data = await pageAppointments(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function onCustomerChange() {
  form.petId = null
  pets.value = form.customerId ? await listPets(form.customerId) : []
}

function openCreate() {
  Object.assign(form, {
    id: null, customerId: null, petId: null, serviceId: null,
    staffId: null, appointmentTime: '', remark: ''
  })
  pets.value = []
  dialogVisible.value = true
}

async function changeStatus(row, status, tip) {
  await ElMessageBox.confirm(tip + '？', '提示', { type: 'warning' })
  await changeAppointmentStatus(row.id, status)
  ElMessage.success('状态已更新')
  load()
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    form.id ? await updateAppointment(form) : await createAppointment(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确定删除该预约？`, '提示', { type: 'warning' })
  await deleteAppointment(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  const [c, s] = await Promise.all([listCustomers(), listServices()])
  customers.value = c
  services.value = s
  // 员工名单接口仅管理员可访问（后端角色注解），店员建单时不指定员工
  staffs.value = isAdmin.value ? (await pageUsers({ current: 1, size: 50 })).records : []
  load()
})
</script>
