import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  Chip,
  IconButton,
  Tooltip,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Tabs,
  Tab,
  Alert,
  CircularProgress
} from '@mui/material';
import {
  TrendingUp,
  TrendingDown,
  Inventory,
  ShoppingCart,
  AttachMoney,
  Warning,
  CheckCircle,
  Refresh,
  Download,
  FilterList
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';
import { dashboardApi } from '../services/api';

// 차트 데이터 타입 정의
interface ChartData {
  name: string;
  value: number;
  color?: string;
  [key: string]: any; // Index signature 추가
}

interface TimeSeriesData {
  date: string;
  inventory: number;
  sales: number;
  orders: number;
}

interface ChannelPerformanceData {
  channel: string;
  orders: number;
  sales: number;
  products: number;
  color: string;
}

const AdvancedDashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState('7d');
  const [activeTab, setActiveTab] = useState(0);
  const [refreshKey, setRefreshKey] = useState(0);

  // 차트 데이터 상태
  const [inventoryTrend, setInventoryTrend] = useState<TimeSeriesData[]>([]);
  const [salesTrend, setSalesTrend] = useState<TimeSeriesData[]>([]);
  const [channelPerformance, setChannelPerformance] = useState<ChannelPerformanceData[]>([]);
  const [stockStatus, setStockStatus] = useState<ChartData[]>([]);
  const [partTypeDistribution, setPartTypeDistribution] = useState<ChartData[]>([]);

  // 더미 데이터 생성 (실제 API 연동 시 교체)
  const generateDummyData = () => {
    // 재고 추이 데이터 (7일)
    const inventoryData: TimeSeriesData[] = [];
    const salesData: TimeSeriesData[] = [];
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];
      
      inventoryData.push({
        date: dateStr,
        inventory: Math.floor(Math.random() * 1000) + 500,
        sales: Math.floor(Math.random() * 50) + 20,
        orders: Math.floor(Math.random() * 30) + 10
      });
      
      salesData.push({
        date: dateStr,
        inventory: Math.floor(Math.random() * 100) + 50,
        sales: Math.floor(Math.random() * 10000) + 5000,
        orders: Math.floor(Math.random() * 20) + 5
      });
    }
    
    setInventoryTrend(inventoryData);
    setSalesTrend(salesData);
    
    // 채널 성과 데이터
    setChannelPerformance([
      { channel: '당근마켓', orders: 45, sales: 125000, products: 23, color: '#FF6B35' },
      { channel: '번개장터', orders: 32, sales: 98000, products: 18, color: '#4ECDC4' },
      { channel: '카페24', orders: 28, sales: 87000, products: 15, color: '#45B7D1' },
      { channel: '네이버스토어', orders: 35, sales: 156000, products: 31, color: '#96CEB4' },
      { channel: '쿠팡', orders: 41, sales: 189000, products: 27, color: '#FFEAA7' },
      { channel: '옥션', orders: 19, sales: 67000, products: 12, color: '#DDA0DD' }
    ]);
    
    // 재고 상태 분포
    setStockStatus([
      { name: '정상 재고', value: 65, color: '#4CAF50' },
      { name: '재고 부족', value: 20, color: '#FF9800' },
      { name: '품절', value: 15, color: '#F44336' }
    ]);
    
    // 부품 유형 분포
    setPartTypeDistribution([
      { name: '엔진', value: 25, color: '#2196F3' },
      { name: '브레이크', value: 20, color: '#FF5722' },
      { name: '서스펜션', value: 18, color: '#9C27B0' },
      { name: '전기', value: 15, color: '#FFC107' },
      { name: '기타', value: 22, color: '#607D8B' }
    ]);
  };

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      
      try {
        // 실제 API 호출 (현재는 더미 데이터 사용)
        generateDummyData();
        
        // 실제 API 연동 시 아래 주석 해제
        // const [stats, channelStats] = await Promise.all([
        //   dashboardApi.getStats(),
        //   dashboardApi.getChannelStats()
        // ]);
        
      } catch (err) {
        setError('데이터를 불러오는 중 오류가 발생했습니다.');
        console.error('Dashboard data loading error:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [refreshKey, timeRange]);

  const handleRefresh = () => {
    setRefreshKey(prev => prev + 1);
  };

  const handleTimeRangeChange = (event: any) => {
    setTimeRange(event.target.value);
  };

  const handleTabChange = (event: any, newValue: number) => {
    setActiveTab(newValue);
  };

  const handleExport = () => {
    // 데이터 내보내기 기능 (Excel, PDF 등)
    console.log('Exporting dashboard data...');
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
        <Typography variant="h6" sx={{ ml: 2 }}>데이터를 불러오는 중...</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" action={
        <Button color="inherit" size="small" onClick={handleRefresh}>
          다시 시도
        </Button>
      }>
        {error}
      </Alert>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* 헤더 */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          고급 대시보드
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>기간</InputLabel>
            <Select value={timeRange} onChange={handleTimeRangeChange}>
              <MenuItem value="1d">1일</MenuItem>
              <MenuItem value="7d">7일</MenuItem>
              <MenuItem value="30d">30일</MenuItem>
              <MenuItem value="90d">90일</MenuItem>
            </Select>
          </FormControl>
          <Tooltip title="새로고침">
            <IconButton onClick={handleRefresh}>
              <Refresh />
            </IconButton>
          </Tooltip>
          <Tooltip title="데이터 내보내기">
            <IconButton onClick={handleExport}>
              <Download />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {/* 탭 네비게이션 */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={activeTab} onChange={handleTabChange}>
          <Tab label="재고 현황" />
          <Tab label="판매 분석" />
          <Tab label="채널 성과" />
          <Tab label="부품 분석" />
        </Tabs>
      </Box>

      {/* 탭 콘텐츠 */}
      {activeTab === 0 && (
        <Grid container spacing={3}>
          {/* 재고 추이 차트 */}
          <Grid item xs={12} lg={8}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  재고 추이 (7일)
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={inventoryTrend}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip />
                    <Legend />
                    <Area type="monotone" dataKey="inventory" stackId="1" stroke="#8884d8" fill="#8884d8" />
                    <Area type="monotone" dataKey="sales" stackId="2" stroke="#82ca9d" fill="#82ca9d" />
                  </AreaChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          {/* 재고 상태 분포 */}
          <Grid item xs={12} lg={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  재고 상태 분포
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={stockStatus}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }: any) => `${name} ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {stockStatus.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {activeTab === 1 && (
        <Grid container spacing={3}>
          {/* 판매 추이 차트 */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  판매 추이 (7일)
                </Typography>
                <ResponsiveContainer width="100%" height={400}>
                  <LineChart data={salesTrend}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip formatter={(value, name) => [value.toLocaleString(), name]} />
                    <Legend />
                    <Line type="monotone" dataKey="sales" stroke="#8884d8" strokeWidth={2} />
                    <Line type="monotone" dataKey="orders" stroke="#82ca9d" strokeWidth={2} />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {activeTab === 2 && (
        <Grid container spacing={3}>
          {/* 채널별 주문 수 */}
          <Grid item xs={12} lg={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  채널별 주문 수
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={channelPerformance}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="channel" />
                    <YAxis />
                    <RechartsTooltip />
                    <Bar dataKey="orders" fill="#8884d8" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          {/* 채널별 매출 */}
          <Grid item xs={12} lg={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  채널별 매출
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={channelPerformance}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="channel" />
                    <YAxis />
                    <RechartsTooltip formatter={(value) => [value.toLocaleString() + '원', '매출']} />
                    <Bar dataKey="sales" fill="#82ca9d" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {activeTab === 3 && (
        <Grid container spacing={3}>
          {/* 부품 유형 분포 */}
          <Grid item xs={12} lg={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  부품 유형 분포
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={partTypeDistribution}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }: any) => `${name} ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {partTypeDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>

          {/* 채널별 상품 수 */}
          <Grid item xs={12} lg={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  채널별 상품 수
                </Typography>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={channelPerformance}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="channel" />
                    <YAxis />
                    <RechartsTooltip />
                    <Bar dataKey="products" fill="#ffc658" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default AdvancedDashboard;
