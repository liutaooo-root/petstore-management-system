<template>
  <div>
    <!-- 顶部指标 -->
    <el-row :gutter="14">
      <el-col v-for="c in cards" :key="c.label" :span="4">
        <div class="stat-card" style="margin-bottom: 14px">
          <div class="stat-icon" :style="{ background: c.color }">
            <el-icon><component :is="c.icon" /></el-icon>
          </div>
          <div>
            <div class="stat-value">{{ c.value }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14">
      <el-col :span="16">
        <div class="page-card">
          <div class="chart-title">近 30 天营收趋势（元）</div>
          <div ref="trendRef" style="height: 330px"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="page-card">
          <div class="chart-title">商品 / 服务营收占比</div>
          <div ref="ratioRef" style="height: 330px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="12">
        <div class="page-card">
          <div class="chart-title">热销商品 TOP5（按销售额）</div>
          <div ref="prodRef" style="height: 300px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <div class="chart-title">热门服务 TOP5（按销售额）</div>
          <div ref="servRef" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { dashboardSummary, revenueTrend, revenueRatio, topProducts, topServices } from '../api'

const summary = ref({})
const trend = ref([])
const ratio = ref([])
const prodTop = ref([])
const servTop = ref([])

const trendRef = ref()
const ratioRef = ref()
const prodRef = ref()
const servRef = ref()
let trendChart, ratioChart, prodChart, servChart

const cards = computed(() => [
  { label: '今日营收', value: '¥' + Number(summary.value.todayRevenue || 0).toFixed(0), icon: 'Wallet', color: '#409eff' },
  { label: '本月营收', value: '¥' + Number(summary.value.monthRevenue || 0).toFixed(0), icon: 'Money', color: '#67c23a' },
  { label: '今日订单', value: summary.value.todayBillCount ?? 0, icon: 'Tickets', color: '#e6a23c' },
  { label: '本月订单', value: summary.value.monthBillCount ?? 0, icon: 'List', color: '#909399' },
  { label: '客户总数', value: summary.value.customerTotal ?? 0, icon: 'User', color: '#f56c6c' },
  { label: '宠物总数', value: summary.value.petTotal ?? 0, icon: 'Notebook', color: '#9b59b6' }
])

async function load() {
  const [s, t, r, p, sv] = await Promise.all([
    dashboardSummary(), revenueTrend(30), revenueRatio(), topProducts(5), topServices(5)
  ])
  summary.value = s
  trend.value = t || []
  ratio.value = r || []
  prodTop.value = p || []
  servTop.value = sv || []
  await nextTick()
  renderCharts()
}

function renderCharts() {
  // 营收趋势（折线 + 渐变填充）
  trendChart = trendChart || echarts.init(trendRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis', valueFormatter: v => '¥' + v },
    grid: { left: 50, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: trend.value.map(i => i.k.slice(5)), boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      data: trend.value.map(i => Number(i.v)),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      lineStyle: { width: 3, color: '#409eff' },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.35)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' }
        ])
      }
    }]
  })

  // 营收占比（环形图）
  const ratioMap = { PRODUCT: '商品', SERVICE: '服务' }
  ratioChart = ratioChart || echarts.init(ratioRef.value)
  ratioChart.setOption({
    tooltip: { trigger: 'item', formatter: p => `${p.name}<br/>¥${p.value} (${p.percent}%)` },
    legend: { bottom: 10 },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: { formatter: '{b}\n{d}%', fontSize: 12 },
      data: ratio.value.map(i => ({ name: ratioMap[i.k] || i.k, value: Number(i.v) })),
      color: ['#409eff', '#67c23a']
    }]
  })

  // 排行（横向条形图）
  const barOption = (data, color) => ({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, valueFormatter: v => '¥' + v },
    grid: { left: 110, right: 40, top: 16, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: data.map(i => i.k).reverse(),
      axisLabel: { fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: data.map(i => Number(i.v)).reverse(),
      barWidth: 16,
      itemStyle: { color, borderRadius: [0, 6, 6, 0] }
    }]
  })
  prodChart = prodChart || echarts.init(prodRef.value)
  prodChart.setOption(barOption(prodTop.value, '#409eff'))
  servChart = servChart || echarts.init(servRef.value)
  servChart.setOption(barOption(servTop.value, '#67c23a'))
}

function onResize() {
  trendChart?.resize()
  ratioChart?.resize()
  prodChart?.resize()
  servChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  ;[trendChart, ratioChart, prodChart, servChart].forEach(c => c?.dispose())
})
</script>

<style scoped>
.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
</style>
