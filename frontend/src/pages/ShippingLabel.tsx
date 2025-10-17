import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Box, Card, CardContent, TextField, Button, Grid,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Chip, Alert, Divider, FormControl, InputLabel, Select, MenuItem, IconButton, Tooltip,
} from '@mui/material';
import {
  LocalShipping as ShippingIcon, Search as SearchIcon, Check as CheckIcon,
  Receipt as ReceiptIcon, Person as PersonIcon, LocationOn as LocationIcon,
  Refresh as RefreshIcon, FilterList as FilterIcon, Sort as SortIcon,
  AccessTime as UrgencyIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { orderApi } from '../services/api';
import { Order, OrderStatus, Channel } from '../types';

const ShippingLabel: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [orders, setOrders] = useState<Order[]>([]);
  const [filteredOrders, setFilteredOrders] = useState<Order[]>([]);
  const [selectedOrders, setSelectedOrders] = useState<Set<number>>(new Set());
  const [shippingInfo, setShippingInfo] = useState({
    trackingNumber: '',
    carrier: '',
    shippingDate: '',
    estimatedDelivery: '',
    notes: ''
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  
  // 필터 및 정렬 상태
  const [sortOrder, setSortOrder] = useState<'newest' | 'oldest' | 'high' | 'low'>('newest');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'CONFIRMED' | 'PROCESSING'>('ALL');
  const [channelFilter, setChannelFilter] = useState<'ALL' | string>('ALL');
  const [urgencyFilter, setUrgencyFilter] = useState<'ALL' | 'today' | 'yesterday' | 'old'>('ALL');
  
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
  }, []);

  useEffect(() => {
    filterAndSortOrders();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchTerm, orders, sortOrder, statusFilter, channelFilter, urgencyFilter]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      // 배송 가능한 주문만 조회 (확정됨, 처리중 상태)
      const [confirmedResponse, processingResponse] = await Promise.all([
        orderApi.getByStatus(OrderStatus.CONFIRMED),
        orderApi.getByStatus(OrderStatus.PROCESSING)
      ]);
      
      const allOrders = [...confirmedResponse.data, ...processingResponse.data];
      setOrders(allOrders);
    } catch (err) {
      setError('주문 정보를 불러오는데 실패했습니다.');
      console.error('Error fetching orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterAndSortOrders = () => {
    let filtered = [...orders];
    
    // 검색 필터
    if (searchTerm) {
      filtered = filtered.filter(order =>
        order.orderNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.customerName.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }
    
    // 상태 필터
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(order => order.status === statusFilter);
    }
    
    // 채널 필터
    if (channelFilter !== 'ALL') {
      filtered = filtered.filter(order => order.channel === channelFilter);
    }
    
    // 긴급도 필터
    if (urgencyFilter !== 'ALL') {
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);
      
      filtered = filtered.filter(order => {
        if (!order.createdAt) return false;
        const orderDate = new Date(order.createdAt);
        const orderDateOnly = new Date(orderDate.getFullYear(), orderDate.getMonth(), orderDate.getDate());
        
        switch (urgencyFilter) {
          case 'today':
            return orderDateOnly.getTime() === today.getTime();
          case 'yesterday':
            return orderDateOnly.getTime() === yesterday.getTime();
          case 'old':
            return orderDateOnly.getTime() < yesterday.getTime();
          default:
            return true;
        }
      });
    }
    
    // 정렬
    filtered.sort((a, b) => {
      switch (sortOrder) {
        case 'newest':
          return new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime();
        case 'oldest':
          return new Date(a.createdAt || 0).getTime() - new Date(b.createdAt || 0).getTime();
        case 'high':
          return (b.totalAmount || 0) - (a.totalAmount || 0);
        case 'low':
          return (a.totalAmount || 0) - (b.totalAmount || 0);
        default:
          return 0;
      }
    });
    
    setFilteredOrders(filtered);
  };

  const generateTrackingNumber = () => {
    // 현재 날짜 + 랜덤 6자리 숫자로 운송장 번호 생성
    const date = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const random = Math.floor(100000 + Math.random() * 900000);
    return `${date}${random}`;
  };

  const getDefaultCarrier = (channel?: string) => {
    // 채널별 기본 택배사 설정
    switch (channel) {
      case 'CAFE24':
        return 'CJ대한통운';
      case 'NAVER_STORE':
        return '한진택배';
      case 'COUPANG':
        return '쿠팡';
      default:
        return 'CJ대한통운';
    }
  };

  const autoFillShippingInfo = (orderId: number) => {
    const order = orders.find(o => o.id === orderId);
    if (!order) return;

    const today = new Date();
    const estimatedDate = new Date(today);
    estimatedDate.setDate(today.getDate() + 2); // 2일 후 예상 배송

    const trackingNumber = generateTrackingNumber();
    const carrier = getDefaultCarrier(order.channel);
    const shippingDate = today.toISOString().split('T')[0];
    const estimatedDelivery = estimatedDate.toISOString().split('T')[0];
    const notes = order.notes || `${order.customerName}님 주문 - 안전하게 배송하겠습니다.`;

    setShippingInfo({
      trackingNumber,
      carrier,
      shippingDate,
      estimatedDelivery,
      notes
    });
  };

  const handleOrderSelect = (orderId: number) => {
    const newSelected = new Set(selectedOrders);
    if (newSelected.has(orderId)) {
      newSelected.delete(orderId);
      // 선택 해제 시 운송장 정보 초기화
      if (newSelected.size === 0) {
        setShippingInfo({
          trackingNumber: '',
          carrier: '',
          shippingDate: '',
          estimatedDelivery: '',
          notes: ''
        });
      }
    } else {
      newSelected.add(orderId);
      // 첫 번째 주문 선택 시 자동으로 운송장 정보 채우기
      if (newSelected.size === 1) {
        autoFillShippingInfo(orderId);
      }
    }
    setSelectedOrders(newSelected);
  };

  const handleSelectAll = () => {
    if (selectedOrders.size === filteredOrders.length) {
      setSelectedOrders(new Set());
      // 전체 해제 시 운송장 정보 초기화
      setShippingInfo({
        trackingNumber: '',
        carrier: '',
        shippingDate: '',
        estimatedDelivery: '',
        notes: ''
      });
    } else {
      const allOrderIds = filteredOrders.map(order => order.id!);
      setSelectedOrders(new Set(allOrderIds));
      // 전체 선택 시 첫 번째 주문 정보로 자동 채우기
      if (allOrderIds.length > 0) {
        autoFillShippingInfo(allOrderIds[0]);
      }
    }
  };

  const handleShippingInfoChange = (field: string, value: string) => {
    setShippingInfo(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSubmitShipping = async () => {
    if (selectedOrders.size === 0) {
      setError('배송할 주문을 선택해주세요.');
      return;
    }

    if (!shippingInfo.carrier) {
      setError('택배사는 필수 입력 항목입니다.');
      return;
    }

    try {
      // 각 주문마다 고유한 운송장 정보 생성
      const updatePromises = Array.from(selectedOrders).map(orderId => {
        const order = orders.find(o => o.id === orderId);
        if (!order) return null;

        // 각 주문마다 고유한 운송장 번호 생성
        const uniqueTrackingNumber = generateTrackingNumber();
        
        // 채널에 맞는 택배사 선택 (사용자가 선택한 택배사 우선)
        const carrier = shippingInfo.carrier || getDefaultCarrier(order.channel);
        
        // 각 주문별 배송 메모 생성
        const orderNotes = order.notes || `${order.customerName}님 주문 - 안전하게 배송하겠습니다.`;
        
        // 공통 날짜 정보 사용
        const shippingDate = shippingInfo.shippingDate || new Date().toISOString().split('T')[0];
        const estimatedDelivery = shippingInfo.estimatedDelivery;

        console.log(`주문 ${order.orderNumber}:`, {
          trackingNumber: uniqueTrackingNumber,
          carrier,
          shippingDate,
          estimatedDelivery,
          notes: orderNotes
        });

        return orderApi.update(orderId, { 
          status: OrderStatus.SHIPPED,
          // TODO: 백엔드에 운송장 정보 저장 필드가 추가되면 여기에 포함
          // trackingNumber: uniqueTrackingNumber,
          // carrier,
          // shippingDate,
          // estimatedDelivery,
          // shippingNotes: orderNotes
        });
      }).filter(p => p !== null);

      await Promise.all(updatePromises);

      setSuccess(`${selectedOrders.size}건의 주문이 배송중 상태로 변경되었습니다. (각 주문마다 고유한 운송장 번호가 생성되었습니다)`);
      setSelectedOrders(new Set());
      setShippingInfo({
        trackingNumber: '',
        carrier: '',
        shippingDate: '',
        estimatedDelivery: '',
        notes: ''
      });

      // 주문 목록 새로고침
      setTimeout(() => {
        fetchOrders();
        setSuccess('');
      }, 2000);

    } catch (err) {
      setError('배송 정보 업데이트에 실패했습니다.');
      console.error('Error updating shipping info:', err);
    }
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
      day: '2-digit'
    });
  };

  const getStatusColor = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.CONFIRMED:
        return 'info';
      case OrderStatus.PROCESSING:
        return 'primary';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.CONFIRMED:
        return '확정됨';
      case OrderStatus.PROCESSING:
        return '처리중';
      default:
        return status;
    }
  };

  return (
    <Container maxWidth="xl">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          운송장 입력
        </Typography>
        <Typography variant="body1" color="text.secondary">
          배송 가능한 주문에 운송장 정보를 입력하고 배송 상태를 업데이트합니다.
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {/* 필터 및 정렬 섹션 */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <FilterIcon sx={{ mr: 1, color: 'primary.main' }} />
            <Typography variant="h6">
              필터 및 정렬
            </Typography>
          </Box>
          
          <Grid container spacing={2}>
            {/* 정렬 */}
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>정렬</InputLabel>
                <Select
                  value={sortOrder}
                  onChange={(e) => setSortOrder(e.target.value as any)}
                  label="정렬"
                  startAdornment={<SortIcon sx={{ mr: 1, color: 'action.active' }} />}
                >
                  <MenuItem value="newest">최신순</MenuItem>
                  <MenuItem value="oldest">오래된 순</MenuItem>
                  <MenuItem value="high">금액 높은 순</MenuItem>
                  <MenuItem value="low">금액 낮은 순</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* 상태 필터 */}
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>주문 상태</InputLabel>
                <Select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as any)}
                  label="주문 상태"
                >
                  <MenuItem value="ALL">전체</MenuItem>
                  <MenuItem value="CONFIRMED">확정됨</MenuItem>
                  <MenuItem value="PROCESSING">처리중</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* 채널 필터 */}
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>판매 채널</InputLabel>
                <Select
                  value={channelFilter}
                  onChange={(e) => setChannelFilter(e.target.value)}
                  label="판매 채널"
                >
                  <MenuItem value="ALL">전체 채널</MenuItem>
                  <MenuItem value={Channel.CAFE24}>🛒 카페24</MenuItem>
                  <MenuItem value={Channel.NAVER_STORE}>🟢 네이버</MenuItem>
                  <MenuItem value={Channel.COUPANG}>🔵 쿠팡</MenuItem>
                  <MenuItem value={Channel.AUCTION}>🟡 옥션</MenuItem>
                  <MenuItem value={Channel.DIRECT_SALE}>🏪 직접판매</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* 긴급도 필터 */}
            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth size="small">
                <InputLabel>긴급도</InputLabel>
                <Select
                  value={urgencyFilter}
                  onChange={(e) => setUrgencyFilter(e.target.value as any)}
                  label="긴급도"
                  startAdornment={<UrgencyIcon sx={{ mr: 1, color: 'action.active' }} />}
                >
                  <MenuItem value="ALL">전체</MenuItem>
                  <MenuItem value="today">🔴 오늘 주문</MenuItem>
                  <MenuItem value="yesterday">🟠 어제 주문</MenuItem>
                  <MenuItem value="old">⚪ 2일 이상</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>

          {/* 필터 요약 */}
          <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {statusFilter !== 'ALL' && (
              <Chip 
                label={`상태: ${statusFilter === 'CONFIRMED' ? '확정됨' : '처리중'}`}
                onDelete={() => setStatusFilter('ALL')}
                size="small"
                color="primary"
                variant="outlined"
              />
            )}
            {channelFilter !== 'ALL' && (
              <Chip 
                label={`채널: ${channelFilter}`}
                onDelete={() => setChannelFilter('ALL')}
                size="small"
                color="secondary"
                variant="outlined"
              />
            )}
            {urgencyFilter !== 'ALL' && (
              <Chip 
                label={`긴급도: ${
                  urgencyFilter === 'today' ? '오늘 주문' :
                  urgencyFilter === 'yesterday' ? '어제 주문' : '2일 이상'
                }`}
                onDelete={() => setUrgencyFilter('ALL')}
                size="small"
                color="warning"
                variant="outlined"
              />
            )}
            {(statusFilter !== 'ALL' || channelFilter !== 'ALL' || urgencyFilter !== 'ALL') && (
              <Button 
                size="small" 
                onClick={() => {
                  setStatusFilter('ALL');
                  setChannelFilter('ALL');
                  setUrgencyFilter('ALL');
                }}
              >
                전체 초기화
              </Button>
            )}
          </Box>
        </CardContent>
      </Card>

      <Grid container spacing={3}>
        {/* 배송 가능 주문 목록 */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  배송 가능 주문 목록 ({filteredOrders.length}건)
                </Typography>
                <TextField
                  size="small"
                  placeholder="주문번호 또는 고객명으로 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  InputProps={{
                    startAdornment: <SearchIcon sx={{ mr: 1 }} />,
                  }}
                  sx={{ minWidth: 300 }}
                />
              </Box>

              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell padding="checkbox">
                        <Button
                          size="small"
                          onClick={handleSelectAll}
                          disabled={filteredOrders.length === 0}
                        >
                          {selectedOrders.size === filteredOrders.length ? '전체 해제' : '전체 선택'}
                        </Button>
                      </TableCell>
                      <TableCell>주문번호</TableCell>
                      <TableCell>고객정보</TableCell>
                      <TableCell>배송주소</TableCell>
                      <TableCell>주문금액</TableCell>
                      <TableCell>상태</TableCell>
                      <TableCell>주문일</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {filteredOrders.map((order) => (
                      <TableRow 
                        key={order.id} 
                        hover
                        selected={selectedOrders.has(order.id!)}
                        onClick={() => handleOrderSelect(order.id!)}
                        sx={{ cursor: 'pointer' }}
                      >
                        <TableCell padding="checkbox">
                          <CheckIcon 
                            color={selectedOrders.has(order.id!) ? 'primary' : 'disabled'} 
                          />
                        </TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <ReceiptIcon sx={{ mr: 1, color: 'primary.main' }} />
                            <Typography variant="body2" fontWeight="bold">
                              {order.orderNumber}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                              <PersonIcon sx={{ mr: 0.5, fontSize: 16 }} />
                              <Typography variant="body2" fontWeight="medium">
                                {order.customerName}
                              </Typography>
                            </Box>
                            {order.customerPhone && (
                              <Typography variant="caption" color="text.secondary">
                                {order.customerPhone}
                              </Typography>
                            )}
                          </Box>
                        </TableCell>
                        <TableCell>
                          {order.shippingAddress ? (
                            <Box sx={{ display: 'flex', alignItems: 'flex-start' }}>
                              <LocationIcon sx={{ mr: 0.5, mt: 0.5, fontSize: 16 }} />
                              <Typography variant="body2" sx={{ maxWidth: 200, wordBreak: 'break-all' }}>
                                {order.shippingAddress}
                              </Typography>
                            </Box>
                          ) : (
                            <Typography variant="body2" color="text.secondary">
                              주소 없음
                            </Typography>
                          )}
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
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              {filteredOrders.length === 0 && !loading && (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary">
                    배송 가능한 주문이 없습니다.
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* 운송장 정보 입력 */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                운송장 정보 입력
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                선택된 주문: {selectedOrders.size}건
              </Typography>
              {selectedOrders.size > 0 && (
                <Alert severity="info" sx={{ mb: 2 }}>
                  {selectedOrders.size === 1 
                    ? '주문 선택 시 운송장 정보가 자동으로 생성됩니다. 필요시 수정 가능합니다.'
                    : `${selectedOrders.size}개 주문이 선택되었습니다. 배송 시작 시 각 주문마다 고유한 운송장 번호가 자동 생성됩니다.`
                  }
                </Alert>
              )}

              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <TextField
                      fullWidth
                      label={selectedOrders.size > 1 ? "운송장 번호 (자동 생성)" : "운송장 번호"}
                      value={shippingInfo.trackingNumber}
                      onChange={(e) => handleShippingInfoChange('trackingNumber', e.target.value)}
                      placeholder={selectedOrders.size > 1 ? "배송 시작 시 각 주문마다 자동 생성" : "예: 1234567890"}
                      disabled={selectedOrders.size > 1}
                      helperText={selectedOrders.size > 1 ? "여러 주문 선택 시 각각 고유한 번호가 자동 생성됩니다" : ""}
                    />
                    <Tooltip title="새 운송장 번호 생성">
                      <span>
                        <IconButton 
                          color="primary" 
                          onClick={() => handleShippingInfoChange('trackingNumber', generateTrackingNumber())}
                          sx={{ mt: 1 }}
                          disabled={selectedOrders.size > 1}
                        >
                          <RefreshIcon />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </Box>
                </Grid>

                <Grid item xs={12}>
                  <FormControl fullWidth required>
                    <InputLabel>택배사</InputLabel>
                    <Select
                      value={shippingInfo.carrier}
                      onChange={(e) => handleShippingInfoChange('carrier', e.target.value)}
                      label="택배사"
                    >
                      <MenuItem value="CJ대한통운">CJ대한통운</MenuItem>
                      <MenuItem value="한진택배">한진택배</MenuItem>
                      <MenuItem value="롯데택배">롯데택배</MenuItem>
                      <MenuItem value="우체국택배">우체국택배</MenuItem>
                      <MenuItem value="쿠팡">쿠팡</MenuItem>
                      <MenuItem value="기타">기타</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="배송일"
                    type="date"
                    value={shippingInfo.shippingDate}
                    onChange={(e) => handleShippingInfoChange('shippingDate', e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="예상 배송일"
                    type="date"
                    value={shippingInfo.estimatedDelivery}
                    onChange={(e) => handleShippingInfoChange('estimatedDelivery', e.target.value)}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    multiline
                    rows={3}
                    label={selectedOrders.size > 1 ? "판매자 메모 (각 주문별 자동 생성)" : "판매자 메모"}
                    value={shippingInfo.notes}
                    onChange={(e) => handleShippingInfoChange('notes', e.target.value)}
                    placeholder={selectedOrders.size > 1 
                      ? "배송 시작 시 각 주문별로 고객명이 포함된 메모가 생성됩니다" 
                      : "배송 관련 특이사항이나 판매자 메모를 입력하세요"
                    }
                    disabled={selectedOrders.size > 1}
                    helperText={selectedOrders.size > 1 ? "각 주문마다 '{고객명}님 주문 - 안전하게 배송하겠습니다' 형식으로 자동 생성" : "고객 요청사항은 주문 상세에서 확인하세요"}
                  />
                </Grid>
              </Grid>

              <Divider sx={{ my: 3 }} />

              <Button
                fullWidth
                variant="contained"
                size="large"
                startIcon={<ShippingIcon />}
                onClick={handleSubmitShipping}
                disabled={selectedOrders.size === 0}
                sx={{ mb: 2 }}
              >
                배송 시작 ({selectedOrders.size}건)
              </Button>

              <Button
                fullWidth
                variant="outlined"
                onClick={() => navigate('/orders')}
              >
                주문 내역으로 이동
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default ShippingLabel;
