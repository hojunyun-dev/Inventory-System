import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Box, Card, CardContent, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, IconButton, TextField, InputAdornment, Button,
  Dialog, DialogTitle, DialogContent, DialogActions, Grid, Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon, Visibility as ViewIcon, LocalShipping as ShippingIcon,
  Receipt as ReceiptIcon, Person as PersonIcon, Email as EmailIcon,
  Phone as PhoneIcon, LocationOn as LocationIcon, Notes as NotesIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { orderApi } from '../services/api';
import { Order, OrderStatus, Channel } from '../types';

const OrderHistory: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [channelFilter, setChannelFilter] = useState<string>('ALL');
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState<boolean>(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await orderApi.getAll();
      setOrders(response.data);
    } catch (err) {
      console.error('Error fetching orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.PENDING:
        return 'warning';
      case OrderStatus.CONFIRMED:
        return 'info';
      case OrderStatus.PROCESSING:
        return 'primary';
      case OrderStatus.SHIPPED:
        return 'secondary';
      case OrderStatus.DELIVERED:
        return 'success';
      case OrderStatus.CANCELLED:
        return 'error';
      case OrderStatus.REFUNDED:
        return 'default';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.PENDING:
        return '대기중';
      case OrderStatus.CONFIRMED:
        return '확정됨';
      case OrderStatus.PROCESSING:
        return '처리중';
      case OrderStatus.SHIPPED:
        return '배송중';
      case OrderStatus.DELIVERED:
        return '배송완료';
      case OrderStatus.CANCELLED:
        return '취소됨';
      case OrderStatus.REFUNDED:
        return '환불됨';
      default:
        return status;
    }
  };

  const filteredOrders = orders.filter(order => {
    const matchesSearch = order.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         order.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (order.customerEmail && order.customerEmail.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
    const matchesChannel = channelFilter === 'ALL' || order.channel === channelFilter;
    
    return matchesSearch && matchesStatus && matchesChannel;
  });

  const getChannelLabel = (channel?: Channel) => {
    if (!channel) return '-';
    const labels = {
      [Channel.CARROT_MARKET]: '🥕 당근',
      [Channel.BUNGAE_MARKET]: '⚡ 번개',
      [Channel.JOONGGONARA]: '📱 중고나라',
      [Channel.CAFE24]: '🛒 카페24',
      [Channel.NAVER_STORE]: '🟢 네이버',
      [Channel.COUPANG]: '🔵 쿠팡',
      [Channel.AUCTION]: '🟡 옥션',
      [Channel.DIRECT_SALE]: '🏪 직접',
    };
    return labels[channel] || channel;
  };

  const handleViewDetails = (order: Order) => {
    setSelectedOrder(order);
    setDetailDialogOpen(true);
  };

  const handleCloseDetailDialog = () => {
    setDetailDialogOpen(false);
    setSelectedOrder(null);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Container maxWidth="xl">
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
          <Typography>주문 내역을 불러오는 중...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          주문 내역
        </Typography>
        <Typography variant="body1" color="text.secondary">
          모든 주문 내역을 확인하고 관리할 수 있습니다.
        </Typography>
      </Box>

      {/* 검색 및 필터 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                placeholder="고객명, 주문번호, 이메일로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <TextField
                fullWidth
                select
                label="판매 채널"
                value={channelFilter}
                onChange={(e) => setChannelFilter(e.target.value)}
                SelectProps={{ native: true }}
              >
                <option value="ALL">전체 채널</option>
                <option value={Channel.CARROT_MARKET}>🥕 당근</option>
                <option value={Channel.BUNGAE_MARKET}>⚡ 번개</option>
                <option value={Channel.JOONGGONARA}>📱 중고나라</option>
                <option value={Channel.CAFE24}>🛒 카페24</option>
                <option value={Channel.NAVER_STORE}>🟢 네이버</option>
                <option value={Channel.COUPANG}>🔵 쿠팡</option>
                <option value={Channel.AUCTION}>🟡 옥션</option>
                <option value={Channel.DIRECT_SALE}>🏪 직접</option>
              </TextField>
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                select
                label="주문 상태"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                SelectProps={{ native: true }}
              >
                <option value="ALL">전체</option>
                <option value={OrderStatus.PENDING}>대기중</option>
                <option value={OrderStatus.CONFIRMED}>확정됨</option>
                <option value={OrderStatus.PROCESSING}>처리중</option>
                <option value={OrderStatus.SHIPPED}>배송중</option>
                <option value={OrderStatus.DELIVERED}>배송완료</option>
                <option value={OrderStatus.CANCELLED}>취소됨</option>
                <option value={OrderStatus.REFUNDED}>환불됨</option>
              </TextField>
            </Grid>
            <Grid item xs={12} md={3}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<ShippingIcon />}
                onClick={() => navigate('/orders/shipping')}
                sx={{ height: '56px' }}
              >
                운송장 입력
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* 주문 목록 */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>주문번호</TableCell>
                <TableCell>판매 채널</TableCell>
                <TableCell>고객정보</TableCell>
                <TableCell>상품수</TableCell>
                <TableCell>총금액</TableCell>
                <TableCell>주문상태</TableCell>
                <TableCell>주문일시</TableCell>
                <TableCell>관리</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredOrders.map((order) => (
                <TableRow key={order.id} hover>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <ReceiptIcon sx={{ mr: 1, color: 'primary.main' }} />
                      <Typography variant="body2" fontWeight="bold">
                        {order.orderNumber}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={getChannelLabel(order.channel)} 
                      size="small"
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell>
                    <Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                        <PersonIcon sx={{ mr: 0.5, fontSize: 16, color: 'text.secondary' }} />
                        <Typography variant="body2" fontWeight="medium">
                          {order.customerName}
                        </Typography>
                      </Box>
                      {order.customerEmail && (
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <EmailIcon sx={{ mr: 0.5, fontSize: 14, color: 'text.secondary' }} />
                          <Typography variant="caption" color="text.secondary">
                            {order.customerEmail}
                          </Typography>
                        </Box>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">
                      {order.orderItems?.length || 0}개
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold" color="primary">
                      {order.totalAmount ? formatCurrency(order.totalAmount) : '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusText(order.status)}
                      color={getStatusColor(order.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {order.createdAt ? formatDate(order.createdAt) : '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Tooltip title="상세보기">
                      <IconButton
                        size="small"
                        onClick={() => handleViewDetails(order)}
                      >
                        <ViewIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      {/* 주문 상세 다이얼로그 */}
      <Dialog
        open={detailDialogOpen}
        onClose={handleCloseDetailDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <ReceiptIcon sx={{ mr: 1 }} />
            주문 상세 정보
          </Box>
        </DialogTitle>
        <DialogContent>
          {selectedOrder && (
            <Grid container spacing={3}>
              {/* 기본 정보 */}
              <Grid item xs={12} md={6}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      주문 정보
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        주문번호
                      </Typography>
                      <Typography variant="body1" fontWeight="bold">
                        {selectedOrder.orderNumber}
                      </Typography>
                    </Box>
                    {selectedOrder.channel && (
                      <Box sx={{ mb: 2 }}>
                        <Typography variant="body2" color="text.secondary">
                          판매 채널
                        </Typography>
                        <Chip
                          label={getChannelLabel(selectedOrder.channel)}
                          size="small"
                          variant="outlined"
                          sx={{ mt: 0.5 }}
                        />
                        {selectedOrder.channelOrderNumber && (
                          <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
                            채널 주문번호: {selectedOrder.channelOrderNumber}
                          </Typography>
                        )}
                      </Box>
                    )}
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        주문상태
                      </Typography>
                      <Chip
                        label={getStatusText(selectedOrder.status)}
                        color={getStatusColor(selectedOrder.status) as any}
                        size="small"
                      />
                    </Box>
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        총금액
                      </Typography>
                      <Typography variant="h6" color="primary">
                        {selectedOrder.totalAmount ? formatCurrency(selectedOrder.totalAmount) : '-'}
                      </Typography>
                    </Box>
                    <Box>
                      <Typography variant="body2" color="text.secondary">
                        주문일시
                      </Typography>
                      <Typography variant="body1">
                        {selectedOrder.createdAt ? formatDate(selectedOrder.createdAt) : '-'}
                      </Typography>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>

              {/* 고객 정보 */}
              <Grid item xs={12} md={6}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      고객 정보
                    </Typography>
                    <Box sx={{ mb: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                        <PersonIcon sx={{ mr: 1, color: 'text.secondary' }} />
                        <Typography variant="body1" fontWeight="medium">
                          {selectedOrder.customerName}
                        </Typography>
                      </Box>
                      {selectedOrder.customerEmail && (
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                          <EmailIcon sx={{ mr: 1, color: 'text.secondary' }} />
                          <Typography variant="body2">
                            {selectedOrder.customerEmail}
                          </Typography>
                        </Box>
                      )}
                      {selectedOrder.customerPhone && (
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                          <PhoneIcon sx={{ mr: 1, color: 'text.secondary' }} />
                          <Typography variant="body2">
                            {selectedOrder.customerPhone}
                          </Typography>
                        </Box>
                      )}
                    </Box>
                    {selectedOrder.shippingAddress && (
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          배송 주소
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'flex-start' }}>
                          <LocationIcon sx={{ mr: 1, mt: 0.5, color: 'text.secondary' }} />
                          <Typography variant="body2">
                            {selectedOrder.shippingAddress}
                          </Typography>
                        </Box>
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>

              {/* 배송 요청사항 (별도 섹션) */}
              <Grid item xs={12}>
                <Card 
                  variant="outlined"
                  sx={{
                    bgcolor: selectedOrder.notes ? 'warning.lighter' : 'background.paper',
                    borderColor: selectedOrder.notes ? 'warning.light' : 'divider'
                  }}
                >
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <ShippingIcon sx={{ mr: 1, color: selectedOrder.notes ? 'warning.main' : 'text.secondary' }} />
                      <Typography variant="h6">
                        배송 요청사항
                      </Typography>
                      <Chip 
                        label="고객 요청" 
                        size="small" 
                        color="warning" 
                        variant="outlined"
                        sx={{ ml: 1 }}
                      />
                    </Box>
                    {selectedOrder.notes ? (
                      <Box 
                        sx={{ 
                          display: 'flex', 
                          alignItems: 'flex-start',
                          p: 2,
                          bgcolor: 'background.paper',
                          borderRadius: 1,
                          border: '2px solid',
                          borderColor: 'warning.main'
                        }}
                      >
                        <NotesIcon sx={{ mr: 1, mt: 0.5, color: 'warning.main' }} />
                        <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap', fontWeight: 500 }}>
                          {selectedOrder.notes}
                        </Typography>
                      </Box>
                    ) : (
                      <Box 
                        sx={{ 
                          p: 3,
                          bgcolor: 'action.hover',
                          borderRadius: 1,
                          textAlign: 'center',
                          border: '1px dashed',
                          borderColor: 'divider'
                        }}
                      >
                        <NotesIcon sx={{ fontSize: 40, color: 'text.disabled', mb: 1 }} />
                        <Typography variant="body1" color="text.secondary">
                          고객 요청사항이 없습니다
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          주문 시 고객이 남긴 배송 요청사항이 여기에 표시됩니다
                        </Typography>
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>

              {/* 주문 상품 */}
              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      주문 상품
                    </Typography>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>상품명</TableCell>
                            <TableCell>SKU</TableCell>
                            <TableCell align="right">수량</TableCell>
                            <TableCell align="right">단가</TableCell>
                            <TableCell align="right">총가격</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {selectedOrder.orderItems?.map((item, index) => (
                            <TableRow key={index}>
                              <TableCell>{item.productName || '-'}</TableCell>
                              <TableCell>{item.productSku || '-'}</TableCell>
                              <TableCell align="right">{item.quantity}</TableCell>
                              <TableCell align="right">
                                {item.unitPrice ? formatCurrency(item.unitPrice) : '-'}
                              </TableCell>
                              <TableCell align="right">
                                {item.totalPrice ? formatCurrency(item.totalPrice) : '-'}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDetailDialog}>닫기</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default OrderHistory;
