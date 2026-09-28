import request from './request'

/* ==================== 认证 ==================== */
export const login = data => request.post('/auth/login', data)
export const changePassword = data => request.put('/auth/password', data)

/* ==================== 客户 ==================== */
export const pageCustomers = params => request.get('/customer/page', { params })
export const listCustomers = () => request.get('/customer/list')
export const getCustomer = id => request.get(`/customer/${id}`)
export const createCustomer = data => request.post('/customer', data)
export const updateCustomer = data => request.put('/customer', data)
export const deleteCustomer = id => request.delete(`/customer/${id}`)

/* ==================== 宠物 ==================== */
export const pagePets = params => request.get('/pet/page', { params })
export const listPets = customerId => request.get('/pet/list', { params: { customerId } })
export const createPet = data => request.post('/pet', data)
export const updatePet = data => request.put('/pet', data)
export const deletePet = id => request.delete(`/pet/${id}`)

/* ==================== 商品与分类 ==================== */
export const pageProducts = params => request.get('/product/page', { params })
export const listProducts = () => request.get('/product/list')
export const createProduct = data => request.post('/product', data)
export const updateProduct = data => request.put('/product', data)
export const deleteProduct = id => request.delete(`/product/${id}`)

export const listCategories = () => request.get('/product/category/list')
export const createCategory = data => request.post('/product/category', data)
export const updateCategory = data => request.put('/product/category', data)
export const deleteCategory = id => request.delete(`/product/category/${id}`)

/* ==================== 服务项目 ==================== */
export const pageServices = params => request.get('/service-item/page', { params })
export const listServices = () => request.get('/service-item/list')
export const createService = data => request.post('/service-item', data)
export const updateService = data => request.put('/service-item', data)
export const deleteService = id => request.delete(`/service-item/${id}`)

/* ==================== 账单 ==================== */
export const createBill = data => request.post('/bill', data)
export const payBill = (id, method) => request.put(`/bill/${id}/pay`, null, { params: { method } })
export const cancelBill = id => request.put(`/bill/${id}/cancel`)
export const pageBills = params => request.get('/bill/page', { params })
export const getBill = id => request.get(`/bill/${id}`)

/* ==================== 预约 ==================== */
export const pageAppointments = params => request.get('/appointment/page', { params })
export const createAppointment = data => request.post('/appointment', data)
export const updateAppointment = data => request.put('/appointment', data)
export const changeAppointmentStatus = (id, status) => request.put(`/appointment/${id}/status`, null, { params: { status } })
export const deleteAppointment = id => request.delete(`/appointment/${id}`)

/* ==================== 看板 ==================== */
export const dashboardSummary = () => request.get('/dashboard/summary')
export const revenueTrend = (days = 30) => request.get('/dashboard/revenue-trend', { params: { days } })
export const revenueRatio = () => request.get('/dashboard/revenue-ratio')
export const topProducts = (limit = 5) => request.get('/dashboard/top-products', { params: { limit } })
export const topServices = (limit = 5) => request.get('/dashboard/top-services', { params: { limit } })

/* ==================== 系统用户（仅管理员） ==================== */
export const pageUsers = params => request.get('/sys-user/page', { params })
export const createUser = data => request.post('/sys-user', data)
export const updateUser = data => request.put('/sys-user', data)
export const resetUserPassword = (id, newPassword) => request.put(`/sys-user/${id}/password`, null, { params: { newPassword } })
export const toggleUserStatus = id => request.put(`/sys-user/${id}/status`)
export const deleteUser = id => request.delete(`/sys-user/${id}`)
