import axios from 'axios';
import { Product, Inventory, Category, StockMovement, Message, MessageStatus, MessageCategory, Order, OrderStatus } from '../types';

// 대시보드 API 타입 정의
export interface DashboardStats {
  totalInventory: number;
  normalStock: number;
  lowStock: number;
  outOfStock: number;
  todayOrders: number;
  todaySales: number;
  todayDeliveries: number;
  totalProducts: number;
  activeProducts: number;
  serializedProducts: number;
  activeChannels: number;
  totalChannels: number;
  unreadNotifications: number;
  urgentNotifications: number;
  lastUpdated: string;
}

export interface ChannelStats {
  channel: string;
  name: string;
  icon: string;
  status: string;
  todayOrders: number;
  todaySales: number;
  activeProducts: number;
  totalProducts: number;
  allocatedStock: number;
  soldStock: number;
  lastSyncStatus: string;
  lastSyncAt: string | null;
  errorMessage: string | null;
}

export interface Notification {
  id: number;
  title: string;
  content: string;
  category: string;
  status: string;
  priority: string;
  actionUrl: string;
  actionText: string;
  createdAt: string;
  readAt: string | null;
  isRead: boolean;
  icon: string;
}

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
  auth: {
    username: 'admin',
    password: 'admin123'
  }
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 인증 오류 처리
      console.error('인증 오류:', error);
    }
    return Promise.reject(error);
  }
);

// 상품 API
export const productApi = {
  getAll: () => api.get<Product[]>('/products'),
  getById: (id: number) => api.get<Product>(`/products/${id}`),
  create: (product: Omit<Product, 'id' | 'createdAt' | 'updatedAt'>) => api.post<Product>('/products', product),
  update: (id: number, product: Partial<Product>) => api.put<Product>(`/products/${id}`, product),
  delete: (id: number) => api.delete(`/products/${id}`),
  checkBarcode: (barcode: string) => api.get<{ exists: boolean }>(`/products/barcode/${barcode}`),
};

// 재고 API
export const inventoryApi = {
  getAll: () => api.get<Inventory[]>('/inventories'),
  getById: (id: number) => api.get<Inventory>(`/inventories/${id}`),
  getByProductId: (productId: number) => api.get<Inventory>(`/inventories/product/${productId}`),
  getLowStock: () => api.get<Inventory[]>('/inventories/low-stock'),
  getOutOfStock: () => api.get<Inventory[]>('/inventories/out-of-stock'),
  create: (inventory: Omit<Inventory, 'id' | 'lastUpdated'>) => api.post<Inventory>('/inventories', inventory),
  update: (id: number, inventory: Partial<Inventory>) => api.put<Inventory>(`/inventories/${id}`, inventory),
  delete: (id: number) => api.delete(`/inventories/${id}`),
};

// 카테고리 API
export const categoryApi = {
  getAll: () => api.get<Category[]>('/categories'),
  getById: (id: number) => api.get<Category>(`/categories/${id}`),
  create: (category: Omit<Category, 'id' | 'createdAt' | 'updatedAt'>) => api.post<Category>('/categories', category),
  update: (id: number, category: Partial<Category>) => api.put<Category>(`/categories/${id}`, category),
  delete: (id: number) => api.delete(`/categories/${id}`),
};

// 재고 이동 API
export const stockMovementApi = {
  getAll: () => api.get<StockMovement[]>('/stock-movements'),
  getById: (id: number) => api.get<StockMovement>(`/stock-movements/${id}`),
  getByProductId: (productId: number) => api.get<StockMovement[]>(`/stock-movements/product/${productId}`),
  create: (movement: Omit<StockMovement, 'id' | 'createdAt' | 'updatedAt'>) => api.post<StockMovement>('/stock-movements', movement),
  update: (id: number, movement: Partial<StockMovement>) => api.put<StockMovement>(`/stock-movements/${id}`, movement),
  delete: (id: number) => api.delete(`/stock-movements/${id}`),
};

// 메시지 API
export const messageApi = {
  getAll: () => api.get<Message[]>('/messages'),
  getById: (id: number) => api.get<Message>(`/messages/${id}`),
  getByStatus: (status: MessageStatus) => api.get<Message[]>(`/messages/status/${status}`),
  getByCategory: (category: MessageCategory) => api.get<Message[]>(`/messages/category/${category}`),
  getNewCount: () => api.get<{ count: number }>('/messages/new/count'),
  create: (message: Omit<Message, 'id' | 'createdAt' | 'updatedAt'>) => api.post<Message>('/messages', message),
  updateStatus: (id: number, status: MessageStatus) => api.put<Message>(`/messages/${id}/status`, { status }),
  reply: (id: number, reply: string, repliedBy: string) => 
    api.put<Message>(`/messages/${id}/reply`, { reply, repliedBy }),
  delete: (id: number) => api.delete(`/messages/${id}`),
};

// 주문 API
export const orderApi = {
  getAll: () => api.get<Order[]>('/orders'),
  getById: (id: number) => api.get<Order>(`/orders/${id}`),
  getByOrderNumber: (orderNumber: string) => api.get<Order>(`/orders/number/${orderNumber}`),
  create: (order: Omit<Order, 'id' | 'createdAt' | 'updatedAt'>) => api.post<Order>('/orders', order),
  update: (id: number, order: Partial<Order>) => api.put<Order>(`/orders/${id}`, order),
  delete: (id: number) => api.delete(`/orders/${id}`),
  getByStatus: (status: OrderStatus) => api.get<Order[]>(`/orders/status/${status}`),
  searchByCustomerName: (customerName: string) => api.get<Order[]>(`/orders/search?customerName=${customerName}`),
};

// 대시보드 API
export const dashboardApi = {
  getStats: () => api.get<DashboardStats>('/dashboard/stats'),
  getStatsByDate: (date?: string, type?: string) => {
    const params = new URLSearchParams();
    if (date) params.append('date', date);
    if (type) params.append('type', type);
    return api.get<DashboardStats>(`/dashboard/stats/by-date?${params.toString()}`);
  },
  getChannelStats: () => api.get<ChannelStats[]>('/dashboard/channel-stats'),
  getNotifications: (limit: number = 10) => api.get<Notification[]>(`/dashboard/notifications?limit=${limit}`),
  markNotificationAsRead: (id: number) => api.put(`/dashboard/notifications/${id}/read`),
  markAllNotificationsAsRead: () => api.put('/dashboard/notifications/read-all'),
  getUnreadNotificationCount: () => api.get<number>('/dashboard/notifications/unread-count'),
};

// 번개장터 API (registration-service로 직접 호출)
const bunjangApiClient = axios.create({
  baseURL: 'http://localhost:8082/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const bunjangApi = {
  checkLoginStatus: () => bunjangApiClient.get('/automation/bunjang/session/status'),
  openForManualLogin: () => bunjangApiClient.post('/automation/bunjang/session/open'),
  openWithProduct: (productData: {
    productId: number;
    productName: string;
    description: string;
    price: number;
    quantity: number;
    category: string;
  }) => bunjangApiClient.post('/automation/bunjang/session/open-with-product', productData),
  closeSession: () => bunjangApiClient.post('/automation/bunjang/session/close'),
  registerProduct: (productData: {
    productId: number;
    productName: string;
    description: string;
    price: number;
    quantity: number;
    category: string;
  }) => bunjangApiClient.post('/automation/platform/bunjang/register', productData)
};

export default api;
