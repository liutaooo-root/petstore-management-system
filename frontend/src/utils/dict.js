/** 业务字典与格式化：集中维护，页面只管展示 */

export const GENDER = { 1: '男', 2: '女' }
export const PET_GENDER = { 1: '公', 2: '母' }
export const SPECIES = ['狗', '猫', '兔', '仓鼠', '鸟类', '其他']

/** 会员等级 → el-tag 类型 */
export const LEVEL_TAG = { 普通: 'info', 白银: 'primary', 黄金: 'warning', 白金: 'danger' }

export const BILL_TYPE = { PRODUCT: '商品', SERVICE: '服务', MIXED: '混合' }
export const BILL_TYPE_TAG = { PRODUCT: 'primary', SERVICE: 'success', MIXED: 'warning' }

export const BILL_STATUS = { UNPAID: '待支付', PAID: '已支付', CANCELLED: '已取消' }
export const BILL_STATUS_TAG = { UNPAID: 'warning', PAID: 'success', CANCELLED: 'info' }

export const PAY_METHOD = { CASH: '现金', WECHAT: '微信', ALIPAY: '支付宝', CARD: '刷卡' }

export const APPT_STATUS = { BOOKED: '待到店', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }
export const APPT_STATUS_TAG = { BOOKED: 'primary', IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'info' }

export const ROLE = { ADMIN: '管理员', STAFF: '店员' }

export const fmtMoney = v => '¥' + Number(v ?? 0).toFixed(2)
