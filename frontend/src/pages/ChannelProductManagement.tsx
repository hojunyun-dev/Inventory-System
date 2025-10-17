import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, Box, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Button, TextField, MenuItem, Alert, CircularProgress,
  Dialog, DialogTitle, DialogContent, DialogActions, Grid, IconButton, Tabs, Tab,
} from '@mui/material';
import {
  Add as AddIcon, Store as StoreIcon, Edit as EditIcon, Delete as DeleteIcon,
  Sync as SyncIcon, Link as LinkIcon,
} from '@mui/icons-material';
import axios from 'axios';
import { Channel, ChannelProduct, ChannelProductStatus, Product } from '../types';
import { productApi } from '../services/api';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

const ChannelProductManagement: React.FC = () => {
  const [currentTab, setCurrentTab] = useState(0);
  const [channelProducts, setChannelProducts] = useState<ChannelProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<number | null>(null);
  const [allocatedQuantity, setAllocatedQuantity] = useState(0);
  const [channelPrice, setChannelPrice] = useState(0);
  const [selectedChannel, setSelectedChannel] = useState<Channel>(Channel.CARROT_MARKET);

  const channels = [
    { value: Channel.CARROT_MARKET, label: '🥕 당근마켓', supportsApi: false },
    { value: Channel.BUNGAE_MARKET, label: '⚡ 번개장터', supportsApi: false },
    { value: Channel.JOONGGONARA, label: '📱 중고나라', supportsApi: false },
    { value: Channel.CAFE24, label: '🛒 카페24', supportsApi: true },
    { value: Channel.NAVER_STORE, label: '🟢 네이버', supportsApi: true },
    { value: Channel.COUPANG, label: '🔵 쿠팡', supportsApi: true },
    { value: Channel.AUCTION, label: '🟡 옥션', supportsApi: true },
    { value: Channel.DIRECT_SALE, label: '🏪 직접판매', supportsApi: false },
  ];

  useEffect(() => {
    fetchChannelProducts();
    fetchProducts();
  }, []);

  const fetchChannelProducts = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/channel-products', {
        withCredentials: true
      });
      setChannelProducts(response.data);
    } catch (err) {
      setError('채널 상품 목록을 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch channel products:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await productApi.getAll();
      setProducts(response.data);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const handleOpenDialog = (channel: Channel) => {
    setSelectedChannel(channel);
    setOpenDialog(true);
  };

  const handleRegisterToChannel = async () => {
    if (!selectedProduct || allocatedQuantity <= 0) {
      alert('상품과 수량을 선택해주세요.');
      return;
    }

    try {
      await axios.post('http://localhost:8080/api/channel-products/register', {
        productId: selectedProduct,
        channel: selectedChannel,
        allocatedQuantity: allocatedQuantity,
        channelPrice: channelPrice || null,
      }, { withCredentials: true });

      alert('채널에 상품이 등록되었습니다!');
      setOpenDialog(false);
      setSelectedProduct(null);
      setAllocatedQuantity(0);
      setChannelPrice(0);
      fetchChannelProducts();
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || '채널 등록 중 오류가 발생했습니다.';
      alert(errorMsg);
      console.error('Failed to register to channel:', err);
    }
  };

  const handleDeleteChannelProduct = async (id: number) => {
    if (!window.confirm('정말 이 채널 상품을 삭제하시겠습니까?')) return;

    try {
      await axios.delete(`http://localhost:8080/api/channel-products/${id}`, {
        withCredentials: true
      });
      alert('삭제되었습니다.');
      fetchChannelProducts();
    } catch (err) {
      alert('삭제 중 오류가 발생했습니다.');
      console.error('Failed to delete:', err);
    }
  };

  const getStatusColor = (status: ChannelProductStatus) => {
    const colors = {
      [ChannelProductStatus.DRAFT]: 'default',
      [ChannelProductStatus.ACTIVE]: 'success',
      [ChannelProductStatus.OUT_OF_STOCK]: 'error',
      [ChannelProductStatus.PAUSED]: 'warning',
      [ChannelProductStatus.DELETED]: 'default',
      [ChannelProductStatus.SYNC_PENDING]: 'info',
      [ChannelProductStatus.SYNC_FAILED]: 'error',
    };
    return colors[status] || 'default';
  };

  const getStatusLabel = (status: ChannelProductStatus) => {
    const labels = {
      [ChannelProductStatus.DRAFT]: '임시저장',
      [ChannelProductStatus.ACTIVE]: '판매중',
      [ChannelProductStatus.OUT_OF_STOCK]: '품절',
      [ChannelProductStatus.PAUSED]: '일시중지',
      [ChannelProductStatus.DELETED]: '삭제됨',
      [ChannelProductStatus.SYNC_PENDING]: '동기화대기',
      [ChannelProductStatus.SYNC_FAILED]: '동기화실패',
    };
    return labels[status] || status;
  };

  const getChannelLabel = (channel: Channel) => {
    return channels.find(c => c.value === channel)?.label || channel;
  };

  const filterByChannel = (channel: Channel) => {
    return channelProducts.filter(cp => cp.channel === channel);
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <StoreIcon fontSize="large" /> 채널별 상품 관리
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 채널 탭 */}
      <Paper sx={{ mb: 2 }}>
        <Tabs value={currentTab} onChange={(e, newValue) => setCurrentTab(newValue)} variant="scrollable" scrollButtons="auto">
          {channels.map((channel, index) => (
            <Tab 
              key={channel.value} 
              label={
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {channel.label}
                  <Chip 
                    label={filterByChannel(channel.value).length} 
                    size="small" 
                    color="primary"
                  />
                </Box>
              }
            />
          ))}
        </Tabs>
      </Paper>

      {/* 각 채널별 탭 패널 */}
      {channels.map((channel, index) => (
        <TabPanel key={channel.value} value={currentTab} index={index}>
          <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="h6">{channel.label} 등록 상품</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleOpenDialog(channel.value)}
            >
              상품 등록
            </Button>
          </Box>

          {channel.supportsApi && (
            <Alert severity="info" sx={{ mb: 2 }}>
              이 채널은 API 자동 동기화를 지원합니다. (4단계에서 구현 예정)
            </Alert>
          )}

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>상품명</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>OEM번호</TableCell>
                  <TableCell>채널 가격</TableCell>
                  <TableCell>할당 재고</TableCell>
                  <TableCell>판매됨</TableCell>
                  <TableCell>남은 재고</TableCell>
                  <TableCell>상태</TableCell>
                  <TableCell>작업</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filterByChannel(channel.value).length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={9} align="center">
                      <Typography color="textSecondary">
                        등록된 상품이 없습니다.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  filterByChannel(channel.value).map((cp) => (
                    <TableRow key={cp.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          {cp.productName}
                        </Typography>
                      </TableCell>
                      <TableCell>{cp.productSku}</TableCell>
                      <TableCell>{cp.productOemNumber || '-'}</TableCell>
                      <TableCell>{cp.channelPrice?.toLocaleString()}원</TableCell>
                      <TableCell>{cp.allocatedQuantity}</TableCell>
                      <TableCell>{cp.soldQuantity}</TableCell>
                      <TableCell>
                        <Typography 
                          variant="body2" 
                          fontWeight="bold"
                          color={cp.availableQuantity! <= 0 ? 'error' : 'success.main'}
                        >
                          {cp.availableQuantity}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={getStatusLabel(cp.status)} 
                          size="small" 
                          color={getStatusColor(cp.status) as any}
                        />
                      </TableCell>
                      <TableCell>
                        <IconButton size="small" color="error" onClick={() => handleDeleteChannelProduct(cp.id!)}>
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>
      ))}

      {/* 상품 등록 다이얼로그 */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {getChannelLabel(selectedChannel)}에 상품 등록
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                select
                fullWidth
                label="상품 선택"
                value={selectedProduct || ''}
                onChange={(e) => setSelectedProduct(Number(e.target.value))}
              >
                <MenuItem value="">선택하세요</MenuItem>
                {products.map((product) => (
                  <MenuItem key={product.id} value={product.id}>
                    {product.name} (SKU: {product.sku})
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                type="number"
                label="할당 재고 수량"
                value={allocatedQuantity}
                onChange={(e) => setAllocatedQuantity(Number(e.target.value))}
                helperText="이 채널에 판매할 수량을 입력하세요"
                InputProps={{ inputProps: { min: 0 } }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                type="number"
                label="채널 판매가 (선택사항)"
                value={channelPrice}
                onChange={(e) => setChannelPrice(Number(e.target.value))}
                helperText="비워두면 기본 가격 사용"
                InputProps={{ inputProps: { min: 0 } }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>취소</Button>
          <Button variant="contained" onClick={handleRegisterToChannel}>
            등록
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ChannelProductManagement;

