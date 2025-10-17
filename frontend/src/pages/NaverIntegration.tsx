import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Grid, Card, CardContent, Box, Button, 
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Paper, Chip, IconButton, Dialog, DialogTitle, DialogContent, 
  DialogActions, TextField, Alert, Snackbar, CircularProgress,
  Tabs, Tab, FormControl, InputLabel, Select, MenuItem
} from '@mui/material';
import {
  Sync as SyncIcon, Add as AddIcon, Edit as EditIcon, 
  Delete as DeleteIcon, CheckCircle as CheckCircleIcon,
  Error as ErrorIcon, Warning as WarningIcon
} from '@mui/icons-material';
import { Product, ChannelProduct } from '../types';

interface NaverProduct {
  id: number;
  productName: string;
  channelProductId: string;
  status: string;
  channelPrice: number;
  allocatedQuantity: number;
  lastSyncAt: string;
  syncStatus: string;
  syncErrorMessage?: string;
}

const NaverIntegration: React.FC = () => {
  const [products, setProducts] = useState<NaverProduct[]>([]);
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedTab, setSelectedTab] = useState(0);
  const [registerDialogOpen, setRegisterDialogOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [naverData, setNaverData] = useState({
    title: '',
    description: '',
    price: 0,
    brand: '자동차부품',
    origin: '한국',
    warranty: '1년'
  });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });
  const [connectionStatus, setConnectionStatus] = useState<'connected' | 'disconnected' | 'checking'>('checking');

  useEffect(() => {
    fetchNaverProducts();
    fetchAllProducts();
    checkConnectionStatus();
  }, []);

  const fetchNaverProducts = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/naver/products');
      const data = await response.json();
      if (data.success) {
        setProducts(data.data);
      }
    } catch (error) {
      console.error('네이버 스토어 상품 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAllProducts = async () => {
    try {
      const response = await fetch('/api/products');
      const data = await response.json();
      setAllProducts(data);
    } catch (error) {
      console.error('상품 조회 실패:', error);
    }
  };

  const checkConnectionStatus = async () => {
    try {
      const response = await fetch('/api/naver/status');
      const data = await response.json();
      setConnectionStatus(data.success ? 'connected' : 'disconnected');
    } catch (error) {
      setConnectionStatus('disconnected');
    }
  };

  const handleRegisterProduct = async () => {
    if (!selectedProduct) return;

    try {
      setLoading(true);
      const response = await fetch(`/api/naver/products/${selectedProduct.id}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(naverData)
      });
      
      const data = await response.json();
      if (data.success) {
        setSnackbar({ open: true, message: '네이버 스토어에 상품이 등록되었습니다.', severity: 'success' });
        fetchNaverProducts();
        setRegisterDialogOpen(false);
        setNaverData({ 
          title: '', 
          description: '', 
          price: 0, 
          brand: '자동차부품', 
          origin: '한국', 
          warranty: '1년' 
        });
      } else {
        setSnackbar({ open: true, message: data.message, severity: 'error' });
      }
    } catch (error) {
      setSnackbar({ open: true, message: '등록 실패: ' + error, severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleSyncInventory = async (productId: number) => {
    try {
      setLoading(true);
      const response = await fetch(`/api/naver/products/${productId}/sync-inventory`, {
        method: 'POST'
      });
      
      const data = await response.json();
      if (data.success) {
        setSnackbar({ open: true, message: '재고 동기화 완료', severity: 'success' });
        fetchNaverProducts();
      } else {
        setSnackbar({ open: true, message: data.message, severity: 'error' });
      }
    } catch (error) {
      setSnackbar({ open: true, message: '동기화 실패: ' + error, severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (productId: number) => {
    if (!window.confirm('정말로 이 상품을 네이버 스토어에서 비활성화하시겠습니까?')) return;

    try {
      setLoading(true);
      const response = await fetch(`/api/naver/products/${productId}`, {
        method: 'DELETE'
      });
      
      const data = await response.json();
      if (data.success) {
        setSnackbar({ open: true, message: '상품이 비활성화되었습니다.', severity: 'success' });
        fetchNaverProducts();
      } else {
        setSnackbar({ open: true, message: data.message, severity: 'error' });
      }
    } catch (error) {
      setSnackbar({ open: true, message: '비활성화 실패: ' + error, severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const getStatusChip = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return <Chip icon={<CheckCircleIcon />} label="정상" color="success" size="small" />;
      case 'FAILED':
        return <Chip icon={<ErrorIcon />} label="실패" color="error" size="small" />;
      case 'PENDING':
        return <Chip icon={<WarningIcon />} label="대기" color="warning" size="small" />;
      default:
        return <Chip label={status} size="small" />;
    }
  };

  const getConnectionStatusChip = () => {
    switch (connectionStatus) {
      case 'connected':
        return <Chip icon={<CheckCircleIcon />} label="연결됨" color="success" />;
      case 'disconnected':
        return <Chip icon={<ErrorIcon />} label="연결 안됨" color="error" />;
      case 'checking':
        return <Chip icon={<CircularProgress size={16} />} label="확인 중" color="default" />;
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          🟢 네이버 스토어 연동 관리
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          {getConnectionStatusChip()}
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setRegisterDialogOpen(true)}
            disabled={connectionStatus !== 'connected'}
          >
            상품 등록
          </Button>
        </Box>
      </Box>

      {connectionStatus === 'disconnected' && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          네이버 스토어 API 연결이 필요합니다. 환경변수에 NAVER_CLIENT_ID, NAVER_CLIENT_SECRET, NAVER_ACCESS_TOKEN을 설정해주세요.
        </Alert>
      )}

      <Paper sx={{ mb: 3 }}>
        <Tabs value={selectedTab} onChange={(e, v) => setSelectedTab(v)}>
          <Tab label="등록된 상품" />
          <Tab label="전체 상품" />
          <Tab label="연동 설정" />
        </Tabs>
      </Paper>

      {selectedTab === 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>상품명</TableCell>
                <TableCell>네이버 ID</TableCell>
                <TableCell>가격</TableCell>
                <TableCell>재고</TableCell>
                <TableCell>상태</TableCell>
                <TableCell>마지막 동기화</TableCell>
                <TableCell>작업</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {products.map((product) => (
                <TableRow key={product.id}>
                  <TableCell>{product.productName}</TableCell>
                  <TableCell>{product.channelProductId}</TableCell>
                  <TableCell>₩{product.channelPrice.toLocaleString()}</TableCell>
                  <TableCell>{product.allocatedQuantity}</TableCell>
                  <TableCell>{getStatusChip(product.syncStatus)}</TableCell>
                  <TableCell>
                    {product.lastSyncAt ? new Date(product.lastSyncAt).toLocaleString() : '-'}
                  </TableCell>
                  <TableCell>
                    <IconButton
                      onClick={() => handleSyncInventory(product.id)}
                      disabled={loading}
                      color="primary"
                    >
                      <SyncIcon />
                    </IconButton>
                    <IconButton
                      onClick={() => handleDeleteProduct(product.id)}
                      disabled={loading}
                      color="error"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {selectedTab === 1 && (
        <Grid container spacing={2}>
          {allProducts.map((product) => (
            <Grid item xs={12} sm={6} md={4} key={product.id}>
              <Card>
                <CardContent>
                  <Typography variant="h6">{product.name}</Typography>
                  <Typography variant="body2" color="textSecondary">
                    SKU: {product.sku}
                  </Typography>
                  <Typography variant="h6" color="primary">
                    ₩{product.price?.toLocaleString()}
                  </Typography>
                  <Box sx={{ mt: 2 }}>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => {
                        setSelectedProduct(product);
                        setNaverData({
                          title: product.name || '',
                          description: product.description || '',
                          price: product.price || 0,
                          brand: '자동차부품',
                          origin: '한국',
                          warranty: '1년'
                        });
                        setRegisterDialogOpen(true);
                      }}
                      disabled={connectionStatus !== 'connected'}
                    >
                      네이버 등록
                    </Button>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {selectedTab === 2 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              네이버 스토어 API 연동 설정
            </Typography>
            <Typography variant="body2" color="textSecondary" paragraph>
              네이버 스토어 API 연동을 위해서는 다음 환경변수를 설정해야 합니다:
            </Typography>
            <Box component="pre" sx={{ bgcolor: 'grey.100', p: 2, borderRadius: 1, overflow: 'auto' }}>
{`NAVER_CLIENT_ID=your_client_id
NAVER_CLIENT_SECRET=your_client_secret
NAVER_ACCESS_TOKEN=your_access_token`}
            </Box>
            <Typography variant="body2" color="textSecondary" sx={{ mt: 2 }}>
              네이버 커머스 개발자 센터에서 앱을 등록하고 인증 정보를 발급받으세요.
            </Typography>
          </CardContent>
        </Card>
      )}

      {/* 상품 등록 다이얼로그 */}
      <Dialog open={registerDialogOpen} onClose={() => setRegisterDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>네이버 스토어 상품 등록</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <TextField
              fullWidth
              label="상품명"
              value={naverData.title}
              onChange={(e) => setNaverData({ ...naverData, title: e.target.value })}
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="상품 설명"
              value={naverData.description}
              onChange={(e) => setNaverData({ ...naverData, description: e.target.value })}
              multiline
              rows={3}
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="가격"
              type="number"
              value={naverData.price}
              onChange={(e) => setNaverData({ ...naverData, price: Number(e.target.value) })}
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="브랜드"
              value={naverData.brand}
              onChange={(e) => setNaverData({ ...naverData, brand: e.target.value })}
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="원산지"
              value={naverData.origin}
              onChange={(e) => setNaverData({ ...naverData, origin: e.target.value })}
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="보증기간"
              value={naverData.warranty}
              onChange={(e) => setNaverData({ ...naverData, warranty: e.target.value })}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRegisterDialogOpen(false)}>취소</Button>
          <Button onClick={handleRegisterProduct} variant="contained" disabled={loading}>
            {loading ? <CircularProgress size={20} /> : '등록'}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default NaverIntegration;
