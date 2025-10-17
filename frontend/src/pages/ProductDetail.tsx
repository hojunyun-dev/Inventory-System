import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, Box, CircularProgress, Alert, Grid, Chip, Button, Divider,
  Table, TableBody, TableCell, TableContainer, TableRow, Dialog, DialogTitle, DialogContent, DialogActions,
  List, ListItem, ListItemText, IconButton, ListItemSecondaryAction,
} from '@mui/material';
import {
  ArrowBack as BackIcon, Edit as EditIcon, Delete as DeleteIcon,
  Inventory as InventoryIcon, QrCode as BarcodeIcon, DirectionsCar as CarIcon,
  Add as AddIcon, Remove as RemoveIcon, SwapHoriz as SwapIcon,
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { productApi, inventoryApi } from '../services/api';
import { Product, Inventory, PartType, MovementCategory, InventoryStatus } from '../types';
import axios from 'axios';

const ProductDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [inventory, setInventory] = useState<Inventory | null>(null);
  const [alternativeProducts, setAlternativeProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openAlternativesDialog, setOpenAlternativesDialog] = useState(false);
  const [availableProducts, setAvailableProducts] = useState<Product[]>([]);

  useEffect(() => {
    if (id) {
      fetchProductDetail(parseInt(id));
    }
  }, [id]);

  const fetchProductDetail = async (productId: number) => {
    try {
      setLoading(true);
      setError('');
      
      // 상품 정보 조회
      const productResponse = await productApi.getById(productId);
      setProduct(productResponse.data);

      // 재고 정보 조회
      try {
        const inventoryResponse = await inventoryApi.getByProductId(productId);
        setInventory(inventoryResponse.data);
      } catch (err) {
        setInventory(null);
      }

      // 대체 부품 목록 조회
      try {
        const response = await axios.get(`http://localhost:8080/api/products/${productId}/alternatives`, {
          withCredentials: true
        });
        setAlternativeProducts(response.data);
      } catch (err) {
        console.error('Failed to fetch alternatives:', err);
        setAlternativeProducts([]);
      }
    } catch (err) {
      setError('상품 정보를 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch product detail:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    navigate(`/products/${id}/edit`);
  };

  const handleDelete = async () => {
    if (!window.confirm('정말로 이 상품을 삭제하시겠습니까?')) return;

    try {
      await productApi.delete(parseInt(id!));
      alert('상품이 삭제되었습니다.');
      navigate('/products');
    } catch (err) {
      setError('상품 삭제 중 오류가 발생했습니다.');
      console.error('Failed to delete product:', err);
    }
  };

  const handleOpenAlternativesDialog = async () => {
    try {
      const response = await productApi.getAll();
      // 현재 제품과 이미 추가된 대체품 제외
      const filtered = response.data.filter(
        (p: Product) => p.id !== parseInt(id!) && !alternativeProducts.some(ap => ap.id === p.id)
      );
      setAvailableProducts(filtered);
      setOpenAlternativesDialog(true);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const handleAddAlternative = async (alternativeId: number) => {
    try {
      await axios.post(
        `http://localhost:8080/api/products/${id}/alternatives/${alternativeId}`,
        {},
        { withCredentials: true }
      );
      alert('대체 부품이 추가되었습니다.');
      fetchProductDetail(parseInt(id!));
      setOpenAlternativesDialog(false);
    } catch (err) {
      alert('대체 부품 추가 중 오류가 발생했습니다.');
      console.error('Failed to add alternative:', err);
    }
  };

  const handleRemoveAlternative = async (alternativeId: number) => {
    if (!window.confirm('이 대체 부품을 제거하시겠습니까?')) return;

    try {
      await axios.delete(
        `http://localhost:8080/api/products/${id}/alternatives/${alternativeId}`,
        { withCredentials: true }
      );
      alert('대체 부품이 제거되었습니다.');
      fetchProductDetail(parseInt(id!));
    } catch (err) {
      alert('대체 부품 제거 중 오류가 발생했습니다.');
      console.error('Failed to remove alternative:', err);
    }
  };

  const getInventoryStatusColor = (status: string) => {
    switch (status) {
      case 'IN_STOCK': return 'success';
      case 'LOW_STOCK': return 'warning';
      case 'OUT_OF_STOCK': return 'error';
      case 'DISCONTINUED': return 'default';
      default: return 'default';
    }
  };

  const getInventoryStatusLabel = (status: string) => {
    switch (status) {
      case 'IN_STOCK': return '정상';
      case 'LOW_STOCK': return '부족';
      case 'OUT_OF_STOCK': return '없음';
      case 'DISCONTINUED': return '단종';
      default: return status;
    }
  };

  const getPartTypeLabel = (partType?: PartType): string => {
    const labels: Record<PartType, string> = {
      [PartType.ENGINE]: '엔진',
      [PartType.TRANSMISSION]: '변속기',
      [PartType.BRAKE]: '브레이크',
      [PartType.SUSPENSION]: '서스펜션',
      [PartType.ELECTRICAL]: '전기',
      [PartType.EXHAUST]: '배기',
      [PartType.COOLING]: '냉각',
      [PartType.FUEL]: '연료',
      [PartType.INTERIOR]: '실내',
      [PartType.EXTERIOR]: '외장',
      [PartType.TIRE_WHEEL]: '타이어',
      [PartType.LIGHTING]: '조명',
      [PartType.FILTER]: '필터',
      [PartType.FLUID]: '오일/액',
      [PartType.BODY]: '차체',
      [PartType.CLIMATE_CONTROL]: '공조',
      [PartType.STEERING]: '조향',
      [PartType.SAFETY]: '안전',
      [PartType.ACCESSORY]: '악세서리',
      [PartType.OTHER]: '기타',
    };
    return partType ? labels[partType] : '-';
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

  if (!product) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="error">상품을 찾을 수 없습니다.</Alert>
        <Button startIcon={<BackIcon />} onClick={() => navigate('/products')} sx={{ mt: 2 }}>
          목록으로 돌아가기
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      {/* 헤더 */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button startIcon={<BackIcon />} onClick={() => navigate('/products')}>
            목록으로
          </Button>
          <Typography variant="h4" component="h1">
            상품 상세 정보
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<EditIcon />} onClick={handleEdit}>
            수정
          </Button>
          <Button variant="outlined" color="error" startIcon={<DeleteIcon />} onClick={handleDelete}>
            삭제
          </Button>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={3}>
        {/* 기본 정보 */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <BarcodeIcon /> 기본 정보
            </Typography>
            <Divider sx={{ mb: 2 }} />
            
            <TableContainer>
              <Table>
                <TableBody>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold', width: '30%' }}>상품명</TableCell>
                    <TableCell>
                      <Typography variant="h6" color="primary">{product.name}</Typography>
                      {product.isSerialized && (
                        <Chip label="시리얼 넘버 관리" size="small" color="secondary" sx={{ ml: 1 }} />
                      )}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold' }}>SKU</TableCell>
                    <TableCell>{product.sku}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold' }}>바코드</TableCell>
                    <TableCell>{product.barcode || '-'}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold' }}>가격</TableCell>
                    <TableCell>
                      <Typography variant="h6" color="success.main">
                        {product.price?.toLocaleString()}원
                      </Typography>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell component="th" sx={{ fontWeight: 'bold' }}>상품 설명</TableCell>
                    <TableCell>{product.description || '-'}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          {/* 자동차 부품 정보 */}
          {(product.oemPartNumber || product.partType || product.warrantyMonths) && (
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CarIcon /> 자동차 부품 정보
              </Typography>
              <Divider sx={{ mb: 2 }} />
              
              <TableContainer>
                <Table>
                  <TableBody>
                    {product.oemPartNumber && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold', width: '30%' }}>OEM 부품 번호</TableCell>
                        <TableCell>{product.oemPartNumber}</TableCell>
                      </TableRow>
                    )}
                    {product.manufacturerCode && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>제조사 코드</TableCell>
                        <TableCell>{product.manufacturerCode}</TableCell>
                      </TableRow>
                    )}
                    {product.aftermarketPartNumber && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>애프터마켓 번호</TableCell>
                        <TableCell>{product.aftermarketPartNumber}</TableCell>
                      </TableRow>
                    )}
                    {product.partType && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>부품 유형</TableCell>
                        <TableCell>
                          <Chip label={getPartTypeLabel(product.partType)} color="primary" size="small" />
                        </TableCell>
                      </TableRow>
                    )}
                    {product.movementCategory && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>회전율 분류</TableCell>
                        <TableCell>
                          {product.movementCategory === MovementCategory.FAST_MOVING && (
                            <Chip label="빠른 부품" color="success" size="small" />
                          )}
                          {product.movementCategory === MovementCategory.CRITICAL && (
                            <Chip label="긴급 부품" color="error" size="small" />
                          )}
                          {product.movementCategory === MovementCategory.SLOW_MOVING && (
                            <Chip label="느린 부품" color="default" size="small" />
                          )}
                          {product.movementCategory === MovementCategory.OBSOLETE && (
                            <Chip label="단종 부품" color="warning" size="small" />
                          )}
                        </TableCell>
                      </TableRow>
                    )}
                    {product.minimumOrderQuantity && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>최소 발주 수량</TableCell>
                        <TableCell>{product.minimumOrderQuantity}</TableCell>
                      </TableRow>
                    )}
                    {product.leadTimeDays && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>리드타임</TableCell>
                        <TableCell>{product.leadTimeDays}일</TableCell>
                      </TableRow>
                    )}
                    {product.reorderPoint && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>재발주 시점</TableCell>
                        <TableCell>{product.reorderPoint}</TableCell>
                      </TableRow>
                    )}
                    {product.warrantyMonths && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>보증 기간</TableCell>
                        <TableCell>{product.warrantyMonths}개월</TableCell>
                      </TableRow>
                    )}
                    {product.weight && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>무게</TableCell>
                        <TableCell>{product.weight}kg</TableCell>
                      </TableRow>
                    )}
                    {product.dimensions && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>치수</TableCell>
                        <TableCell>{product.dimensions}</TableCell>
                      </TableRow>
                    )}
                    {product.notes && (
                      <TableRow>
                        <TableCell component="th" sx={{ fontWeight: 'bold' }}>메모</TableCell>
                        <TableCell>{product.notes}</TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Paper>
          )}

          {/* 대체 부품 */}
          <Paper sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SwapIcon /> 대체 가능 부품
              </Typography>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                size="small"
                onClick={handleOpenAlternativesDialog}
              >
                추가
              </Button>
            </Box>
            <Divider sx={{ mb: 2 }} />

            {alternativeProducts.length === 0 ? (
              <Alert severity="info">등록된 대체 부품이 없습니다.</Alert>
            ) : (
              <List>
                {alternativeProducts.map((alt) => (
                  <ListItem key={alt.id} divider>
                    <ListItemText
                      primary={alt.name}
                      secondary={`SKU: ${alt.sku} | OEM: ${alt.oemPartNumber || '-'} | 가격: ${alt.price?.toLocaleString()}원`}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleRemoveAlternative(alt.id!)}>
                        <RemoveIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
            )}
          </Paper>
        </Grid>

        {/* 재고 정보 */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <InventoryIcon /> 재고 정보
            </Typography>
            <Divider sx={{ mb: 2 }} />
            
            {inventory ? (
              <Box>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>재고 상태</Typography>
                  <Chip 
                    label={getInventoryStatusLabel(inventory.status || InventoryStatus.IN_STOCK)} 
                    color={getInventoryStatusColor(inventory.status || InventoryStatus.IN_STOCK)} 
                    sx={{ mt: 0.5 }}
                  />
                </Box>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>현재 수량</Typography>
                  <Typography variant="h4" color="primary.main">
                    {inventory.quantity?.toLocaleString()}
                  </Typography>
                </Box>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>위치</Typography>
                  <Typography variant="body1">{inventory.location}</Typography>
                </Box>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>최소 재고</Typography>
                  <Typography variant="body1">{inventory.minStockLevel?.toLocaleString() || '-'}</Typography>
                </Box>
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>최대 재고</Typography>
                  <Typography variant="body1">{inventory.maxStockLevel?.toLocaleString() || '-'}</Typography>
                </Box>
                <Button
                  fullWidth
                  variant="outlined"
                  startIcon={<EditIcon />}
                  onClick={() => navigate(`/inventory/${inventory.id}/edit`)}
                  sx={{ mt: 2 }}
                >
                  재고 수정
                </Button>
              </Box>
            ) : (
              <Box>
                <Alert severity="info" sx={{ mb: 2 }}>등록된 재고 정보가 없습니다.</Alert>
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<InventoryIcon />}
                  onClick={() => navigate('/inventory/new')}
                >
                  재고 등록
                </Button>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* 대체 부품 추가 다이얼로그 */}
      <Dialog open={openAlternativesDialog} onClose={() => setOpenAlternativesDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>대체 부품 추가</DialogTitle>
        <DialogContent>
          <List>
            {availableProducts.map((p) => (
              <ListItem key={p.id} divider>
                <ListItemText
                  primary={p.name}
                  secondary={`SKU: ${p.sku} | OEM: ${p.oemPartNumber || '-'} | 가격: ${p.price?.toLocaleString()}원`}
                />
                <ListItemSecondaryAction>
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<AddIcon />}
                    onClick={() => handleAddAlternative(p.id!)}
                  >
                    추가
                  </Button>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenAlternativesDialog(false)}>닫기</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ProductDetail;
