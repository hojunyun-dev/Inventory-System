export interface Product {
  id?: number;
  name: string;
  description?: string;
  price: number;
  cost?: number;
  sku: string;
  barcode?: string;
  category?: Category;
  categoryId?: number;
  
  // 자동차 부품 특화 필드
  oemPartNumber?: string;
  manufacturerCode?: string;
  aftermarketPartNumber?: string;
  partType?: PartType;
  movementCategory?: MovementCategory;
  minimumOrderQuantity?: number;
  leadTimeDays?: number;
  reorderPoint?: number;
  warrantyMonths?: number;
  weight?: number;
  dimensions?: string;
  isSerialized?: boolean;
  imageUrl?: string;
  imageAltText?: string;
  technicalDrawingUrl?: string;
  notes?: string;
  isActive?: boolean;
  
  // 차종 정보
  vehicleManufacturer?: string;
  vehicleModel?: string;
  vehicleYear?: string;
  
  createdAt?: string;
  updatedAt?: string;
}

export enum PartType {
  ENGINE = 'ENGINE',
  TRANSMISSION = 'TRANSMISSION',
  BRAKE = 'BRAKE',
  SUSPENSION = 'SUSPENSION',
  ELECTRICAL = 'ELECTRICAL',
  EXHAUST = 'EXHAUST',
  COOLING = 'COOLING',
  FUEL = 'FUEL',
  INTERIOR = 'INTERIOR',
  EXTERIOR = 'EXTERIOR',
  TIRE_WHEEL = 'TIRE_WHEEL',
  LIGHTING = 'LIGHTING',
  FILTER = 'FILTER',
  FLUID = 'FLUID',
  BODY = 'BODY',
  CLIMATE_CONTROL = 'CLIMATE_CONTROL',
  STEERING = 'STEERING',
  SAFETY = 'SAFETY',
  ACCESSORY = 'ACCESSORY',
  OTHER = 'OTHER'
}

export enum MovementCategory {
  FAST_MOVING = 'FAST_MOVING',
  SLOW_MOVING = 'SLOW_MOVING',
  CRITICAL = 'CRITICAL',
  OBSOLETE = 'OBSOLETE',
  SEASONAL = 'SEASONAL'
}

export interface Category {
  id?: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Inventory {
  id?: number;
  productId: number;
  productName?: string;
  productSku?: string;
  productCategory?: string;
  product?: Product;
  quantity: number;
  location?: string;
  warehouseLocation?: string;
  minStockLevel?: number;
  maxStockLevel?: number;
  status?: InventoryStatus;
  lastUpdated?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WarehouseLocation {
  warehouse?: string;
  zone?: string;
  aisle?: string;
  rack?: string;
  shelf?: string;
  bin?: string;
  fullLocation?: string;
  compactLocation?: string;
}

export enum InventoryStatus {
  IN_STOCK = 'IN_STOCK',
  LOW_STOCK = 'LOW_STOCK',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  DISCONTINUED = 'DISCONTINUED'
}

export interface StockMovement {
  id?: number;
  productId: number;
  product?: Product;
  quantity: number;
  movementType: MovementType;
  reason?: string;
  reference?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum MovementType {
  IN = 'IN',
  OUT = 'OUT',
  TRANSFER = 'TRANSFER',
  ADJUSTMENT = 'ADJUSTMENT',
  RETURN = 'RETURN'
}

export interface Message {
  id?: number;
  senderName: string;
  senderEmail: string;
  senderPhone?: string;
  subject: string;
  content: string;
  status: MessageStatus;
  category: MessageCategory;
  reply?: string;
  repliedAt?: string;
  repliedBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum MessageStatus {
  NEW = 'NEW',
  READ = 'READ',
  IN_PROGRESS = 'IN_PROGRESS',
  REPLIED = 'REPLIED',
  CLOSED = 'CLOSED',
  UNREAD = 'UNREAD'
}

export enum MessageCategory {
  PRODUCT_INQUIRY = 'PRODUCT_INQUIRY',
  ORDER_INQUIRY = 'ORDER_INQUIRY',
  INVENTORY_INQUIRY = 'INVENTORY_INQUIRY',
  TECHNICAL_SUPPORT = 'TECHNICAL_SUPPORT',
  SUGGESTION = 'SUGGESTION',
  LOW_STOCK = 'LOW_STOCK',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  ORDER = 'ORDER',
  DELIVERY = 'DELIVERY',
  URGENT = 'URGENT',
  ETC = 'ETC'
}

export interface Order {
  id?: number;
  orderNumber: string;
  channel?: Channel;
  channelOrderId?: string;
  channelOrderNumber?: string;
  customerName: string;
  customerEmail?: string;
  customerPhone?: string;
  status: OrderStatus;
  totalAmount?: number;
  shippingAddress?: string;
  trackingNumber?: string;
  carrier?: string;
  shippingDate?: string;
  estimatedDelivery?: string;
  notes?: string;
  orderItems?: OrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderItem {
  id?: number;
  productId: number;
  productName?: string;
  productSku?: string;
  quantity: number;
  unitPrice: number;
  totalPrice?: number;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export interface User {
  username: string;
  password?: string;
}

export interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

// 호환 차종 정보
export interface VehicleCompatibility {
  id?: number;
  manufacturer: string;
  model: string;
  yearStart: number;
  yearEnd?: number;
  engineType?: string;
  transmission?: string;
  trim?: string;
  notes?: string;
  yearRange?: string;
  fullDescription?: string;
  productIds?: number[];
  createdAt?: string;
  updatedAt?: string;
}

// 시리얼 넘버 재고
export interface SerializedInventory {
  id?: number;
  productId: number;
  productName?: string;
  productSku?: string;
  serialNumber: string;
  batchNumber?: string;
  status: SerializedInventoryStatus;
  warehouseLocation?: WarehouseLocation;
  purchaseDate?: string;
  warrantyStartDate?: string;
  warrantyEndDate?: string;
  soldDate?: string;
  soldToCustomer?: string;
  orderId?: number;
  returnDate?: string;
  returnReason?: string;
  notes?: string;
  isUnderWarranty?: boolean;
  remainingWarrantyDays?: number;
  createdAt?: string;
  updatedAt?: string;
}

export enum SerializedInventoryStatus {
  IN_STOCK = 'IN_STOCK',
  RESERVED = 'RESERVED',
  SOLD = 'SOLD',
  IN_TRANSIT = 'IN_TRANSIT',
  RETURNED = 'RETURNED',
  DEFECTIVE = 'DEFECTIVE',
  SCRAPPED = 'SCRAPPED',
  UNDER_REPAIR = 'UNDER_REPAIR'
}

// 판매 채널
export enum Channel {
  CARROT_MARKET = 'CARROT_MARKET',
  BUNGAE_MARKET = 'BUNGAE_MARKET',
  JOONGGONARA = 'JOONGGONARA',
  CAFE24 = 'CAFE24',
  NAVER_STORE = 'NAVER_STORE',
  COUPANG = 'COUPANG',
  AUCTION = 'AUCTION',
  DIRECT_SALE = 'DIRECT_SALE'
}

// 채널 상품
export interface ChannelProduct {
  id?: number;
  productId: number;
  productName?: string;
  productSku?: string;
  productOemNumber?: string;
  channel: Channel;
  channelProductId?: string;
  channelProductUrl?: string;
  status: ChannelProductStatus;
  channelPrice?: number;
  allocatedQuantity?: number;
  soldQuantity?: number;
  availableQuantity?: number;
  isAutoSync?: boolean;
  lastSyncAt?: string;
  syncStatus?: string;
  syncErrorMessage?: string;
  channelTitle?: string;
  channelDescription?: string;
  displayOrder?: number;
  productImageUrl?: string;
  productImageAltText?: string;
  isFeatured?: boolean;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum ChannelProductStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  PAUSED = 'PAUSED',
  DELETED = 'DELETED',
  SYNC_PENDING = 'SYNC_PENDING',
  SYNC_FAILED = 'SYNC_FAILED'
}
