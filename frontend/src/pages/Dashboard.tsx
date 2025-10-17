import React, { useState, useEffect, useCallback } from 'react';
import {
  Container, Typography, Grid, Card, CardContent, Box, Button,
  Paper, LinearProgress, Chip,
} from '@mui/material';
import {
  Inventory as InventoryIcon, TrendingUp as TrendingUpIcon,
  Store as StoreIcon, ShoppingCart as OrderIcon, DirectionsCar as CarIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { inventoryApi } from '../services/api';

interface ChannelStats {
  name: string;
  icon: string;
  totalOrders: number;
  todayOrders: number;
  revenue: number;
  activeProducts: number;
  status: 'active' | 'pending' | 'error';
}

const Dashboard: React.FC = () => {
  const [totalInventory, setTotalInventory] = useState(0);
  const [lowStockCount, setLowStockCount] = useState(0);
  const [outOfStockCount, setOutOfStockCount] = useState(0);
  const [inStockCount, setInStockCount] = useState(0);
  const [todayOrders, setTodayOrders] = useState(0);
  const [todaySales, setTodaySales] = useState(0);
  const [todayDeliveries, setTodayDeliveries] = useState(0);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      
      // 재고 데이터 직접 조회
      const inventoryResponse = await inventoryApi.getAll();
      const inventories = inventoryResponse.data;
      const total = inventories.length;
      const lowStock = inventories.filter(inv => inv.status === 'LOW_STOCK').length;
      const outOfStock = inventories.filter(inv => inv.status === 'OUT_OF_STOCK').length;
      const inStock = inventories.filter(inv => inv.status === 'IN_STOCK').length;
      
      setTotalInventory(total);
      setLowStockCount(lowStock);
      setOutOfStockCount(outOfStock);
      setInStockCount(inStock);
      
      // 주문 데이터 조회 (오늘 기준)
      try {
        const ordersResponse = await fetch(`http://localhost:8080/api/orders`);
        
        if (ordersResponse.ok) {
          const orders = await ordersResponse.json();
          const today = new Date().toISOString().split('T')[0];
          const todayOrders = orders.filter((order: any) => order.createdAt?.startsWith(today));
          
          setTodayOrders(todayOrders.length);
          setTodaySales(todayOrders.reduce((sum: number, order: any) => sum + (order.totalAmount || 0), 0));
          setTodayDeliveries(todayOrders.filter((order: any) => order.status === 'DELIVERED').length);
        }
      } catch (orderError) {
        console.log('주문 데이터 조회 실패, 기본값 사용:', orderError);
        setTodayOrders(0);
        setTodaySales(0);
        setTodayDeliveries(0);
      }
    } catch (error) {
      console.error('데이터 로드 실패:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);


  const channelStats: ChannelStats[] = [
    { name: '당근마켓', icon: '🥕', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '번개장터', icon: '⚡', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '중고나라', icon: '📱', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '카페24', icon: '🛒', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '네이버', icon: '🟢', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '쿠팡', icon: '🔵', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '옥션', icon: '🟡', totalOrders: 0, todayOrders: 0, revenue: 0, activeProducts: 0, status: 'pending' },
    { name: '직접판매', icon: '🏪', totalOrders: 45, todayOrders: 12, revenue: 2500000, activeProducts: 25, status: 'active' },
  ];

  const getChannelStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'pending': return 'warning';
      case 'error': return 'error';
      default: return 'default';
    }
  };

  const getChannelStatusLabel = (status: string) => {
    switch (status) {
      case 'active': return '활성';
      case 'pending': return '준비중';
      case 'error': return '오류';
      default: return '알 수 없음';
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <LinearProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 0, mb: 4 }}>

      {/* 통합 재고 현황 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <InventoryIcon /> 통합 재고 현황
        </Typography>
        
        <Grid container spacing={2}>
          <Grid item xs={6} sm={3}>
            <Card sx={{ textAlign: 'center', bgcolor: 'primary.light', color: 'white' }}>
              <CardContent>
                <Typography variant="h4" fontWeight="bold">
                  {totalInventory}
                </Typography>
                <Typography variant="body2">전체 재고</Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Card sx={{ textAlign: 'center', bgcolor: 'success.light', color: 'white' }}>
              <CardContent>
                <Typography variant="h4" fontWeight="bold">
                  {inStockCount}
                </Typography>
                <Typography variant="body2">정상</Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Card sx={{ textAlign: 'center', bgcolor: 'warning.light', color: 'white' }}>
              <CardContent>
                <Typography variant="h4" fontWeight="bold">
                  {lowStockCount}
                </Typography>
                <Typography variant="body2">재고 부족</Typography>
              </CardContent>
            </Card>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Card sx={{ textAlign: 'center', bgcolor: 'error.light', color: 'white' }}>
              <CardContent>
                <Typography variant="h4" fontWeight="bold">
                  {outOfStockCount}
                </Typography>
                <Typography variant="body2">품절</Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Paper>

      <Grid container spacing={3}>
        {/* 좌측: 빠른 작업 + 오늘의 통계 */}
        <Grid item xs={12} md={8}>
          {/* 빠른 작업 */}
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <TrendingUpIcon /> 빠른 작업
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={6} sm={3}>
                <Button
                  variant="contained"
                  fullWidth
                  startIcon={<InventoryIcon />}
                  onClick={() => navigate('/products/new')}
                  sx={{ height: 60 }}
                >
                  상품 등록
                </Button>
              </Grid>
              
              <Grid item xs={6} sm={3}>
                <Button
                  variant="contained"
                  fullWidth
                  startIcon={<CarIcon />}
                  onClick={() => navigate('/vehicle-search')}
                  sx={{ height: 60 }}
                >
                  차종 검색
                </Button>
              </Grid>
              
              <Grid item xs={6} sm={3}>
                <Button
                  variant="contained"
                  fullWidth
                  startIcon={<InventoryIcon />}
                  onClick={() => navigate('/inventory')}
                  sx={{ height: 60 }}
                >
                  재고 조회
                </Button>
              </Grid>
              
              <Grid item xs={6} sm={3}>
                <Button
                  variant="contained"
                  fullWidth
                  startIcon={<OrderIcon />}
                  onClick={() => navigate('/orders')}
                  sx={{ height: 60 }}
                >
                  주문 관리
                </Button>
              </Grid>
            </Grid>
          </Paper>

          {/* 기간별 통계 */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <TrendingUpIcon /> 오늘의 통계
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                  <Typography variant="h4" color="primary" fontWeight="bold">
                    {todayOrders}건
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    오늘 주문
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    전체 채널 합계
                  </Typography>
                </Box>
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                  <Typography variant="h4" color="success.main" fontWeight="bold">
                    ₩{todaySales.toLocaleString()}
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    오늘 매출
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    전체 채널 합계
                  </Typography>
                </Box>
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                  <Typography variant="h4" color="info.main" fontWeight="bold">
                    {todayDeliveries}건
                  </Typography>
                  <Typography variant="body2" color="textSecondary">
                    오늘 배송
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    처리 완료
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* 우측: 채널별 현황 */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, height: 'fit-content', maxHeight: '400px' }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <StoreIcon /> 판매 채널
            </Typography>
            
            <Box sx={{ maxHeight: '300px', overflowY: 'auto', '&::-webkit-scrollbar': { width: '6px' }, '&::-webkit-scrollbar-track': { background: '#f1f1f1', borderRadius: '3px' }, '&::-webkit-scrollbar-thumb': { background: '#c1c1c1', borderRadius: '3px' }, '&::-webkit-scrollbar-thumb:hover': { background: '#a8a8a8' } }}>
              {channelStats.map((channel) => (
                <Box key={channel.name} sx={{ mb: 1.5, p: 1.5, bgcolor: 'grey.50', borderRadius: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <Typography fontSize={20}>{channel.icon}</Typography>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="caption" fontWeight="bold">{channel.name}</Typography>
                    </Box>
                    <Chip 
                      label={getChannelStatusLabel(channel.status)} 
                      size="small" 
                      color={getChannelStatusColor(channel.status) as any}
                      sx={{ height: 20, fontSize: '0.65rem' }}
                    />
                  </Box>
                  
                  {channel.status === 'pending' ? (
                    <Typography variant="caption" color="textSecondary" fontSize="0.7rem">
                      API 연동 준비중
                    </Typography>
                  ) : (
                    <Box sx={{ mt: 0.5 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Typography variant="caption" color="textSecondary" fontSize="0.7rem">
                          오늘 {channel.todayOrders}건 | 상품 {channel.activeProducts}개
                        </Typography>
                      </Box>
                    </Box>
                  )}
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* 주요 기능 안내 */}
      <Paper sx={{ p: 3, mt: 3, bgcolor: 'grey.50' }}>
        <Typography variant="h6" gutterBottom>
          주요 기능
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6} md={3}>
            <Box sx={{ textAlign: 'center' }}>
              <InventoryIcon sx={{ fontSize: 40, color: 'primary.main', mb: 1 }} />
              <Typography variant="subtitle1" fontWeight="bold">재고 관리</Typography>
              <Typography variant="body2" color="textSecondary">
                실시간 재고 현황 및 자동 알림
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Box sx={{ textAlign: 'center' }}>
              <CarIcon sx={{ fontSize: 40, color: 'success.main', mb: 1 }} />
              <Typography variant="subtitle1" fontWeight="bold">차종별 검색</Typography>
              <Typography variant="body2" color="textSecondary">
                제조사, 모델, 연식별 부품 검색
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Box sx={{ textAlign: 'center' }}>
              <StoreIcon sx={{ fontSize: 40, color: 'warning.main', mb: 1 }} />
              <Typography variant="subtitle1" fontWeight="bold">멀티채널</Typography>
              <Typography variant="body2" color="textSecondary">
                다양한 판매 채널 통합 관리
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Box sx={{ textAlign: 'center' }}>
              <OrderIcon sx={{ fontSize: 40, color: 'info.main', mb: 1 }} />
              <Typography variant="subtitle1" fontWeight="bold">주문 관리</Typography>
              <Typography variant="body2" color="textSecondary">
                주문 처리 및 배송 추적
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Container>
  );
};

export default Dashboard;