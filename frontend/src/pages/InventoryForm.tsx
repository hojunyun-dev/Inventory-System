import React, { useState, useEffect } from 'react';
import {
  Container, Paper, TextField, Button, Typography, Box, Alert, CircularProgress,
  FormControl, InputLabel, Select, MenuItem, Grid,
} from '@mui/material';
import {
  Save as SaveIcon, Cancel as CancelIcon, ArrowBack as BackIcon,
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { inventoryApi, productApi } from '../services/api';
import { Inventory, Product, InventoryStatus } from '../types';

const InventoryForm: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);

  const [formData, setFormData] = useState<Partial<Inventory>>({
    productId: 0,
    quantity: 0,
    location: '',
    minStockLevel: 0,
    maxStockLevel: 0,
    status: InventoryStatus.IN_STOCK,
  });

  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    fetchProducts();
    if (isEdit && id) {
      fetchInventory(parseInt(id));
    }
  }, [isEdit, id]);

  const fetchProducts = async () => {
    try {
      const response = await productApi.getAll();
      setProducts(response.data);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const fetchInventory = async (inventoryId: number) => {
    try {
      setLoading(true);
      const response = await inventoryApi.getById(inventoryId);
      const inventory = response.data;
      setFormData({
        productId: inventory.productId,
        quantity: inventory.quantity,
        location: inventory.location,
        minStockLevel: inventory.minStockLevel || 0,
        maxStockLevel: inventory.maxStockLevel || 0,
        status: inventory.status,
      });
    } catch (err) {
      setError('재고 정보를 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch inventory:', err);
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.productId || formData.productId === 0) {
      errors.productId = '상품을 선택해주세요.';
    }

    if (!formData.location?.trim()) {
      errors.location = '위치를 입력해주세요.';
    }

    if (formData.quantity === undefined || formData.quantity < 0) {
      errors.quantity = '올바른 수량을 입력해주세요.';
    }

    if (formData.minStockLevel !== undefined && formData.minStockLevel < 0) {
      errors.minStockLevel = '최소 재고는 0 이상이어야 합니다.';
    }

    if (formData.maxStockLevel !== undefined && formData.maxStockLevel < 0) {
      errors.maxStockLevel = '최대 재고는 0 이상이어야 합니다.';
    }

    if (
      formData.minStockLevel !== undefined && 
      formData.maxStockLevel !== undefined && 
      formData.minStockLevel > formData.maxStockLevel
    ) {
      errors.maxStockLevel = '최대 재고는 최소 재고보다 크거나 같아야 합니다.';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleInputChange = (field: keyof Inventory) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = ['quantity', 'minStockLevel', 'maxStockLevel'].includes(field)
      ? parseInt(event.target.value) || 0
      : event.target.value;
    
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
    
    // 실시간 검증 에러 제거
    if (validationErrors[field]) {
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleSelectChange = (field: keyof Inventory) => (event: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    
    if (validationErrors[field]) {
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      if (isEdit && id) {
        await inventoryApi.update(parseInt(id), formData);
        alert('재고가 수정되었습니다.');
      } else {
        await inventoryApi.create(formData as Omit<Inventory, 'id' | 'lastUpdated'>);
        alert('재고가 등록되었습니다.');
      }
      navigate('/inventory');
    } catch (err) {
      setError(isEdit ? '재고 수정 중 오류가 발생했습니다.' : '재고 등록 중 오류가 발생했습니다.');
      console.error('Inventory save error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/inventory');
  };

  const getStatusLabel = (status: InventoryStatus) => {
    switch (status) {
      case InventoryStatus.IN_STOCK: return '정상';
      case InventoryStatus.LOW_STOCK: return '부족';
      case InventoryStatus.OUT_OF_STOCK: return '없음';
      case InventoryStatus.DISCONTINUED: return '단종';
      default: return status;
    }
  };

  if (loading && isEdit) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button
          startIcon={<BackIcon />}
          onClick={() => navigate('/inventory')}
          sx={{ mr: 2 }}
        >
          목록으로
        </Button>
        <Typography variant="h4" component="h1">
          {isEdit ? '재고 수정' : '신규 재고 등록'}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 4 }}>
        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth required error={!!validationErrors.productId}>
                <InputLabel>상품</InputLabel>
                <Select
                  value={formData.productId || ''}
                  onChange={handleSelectChange('productId')}
                  label="상품"
                  disabled={isEdit} // 수정 시에는 상품 변경 불가
                >
                  {products.map((product) => (
                    <MenuItem key={product.id} value={product.id}>
                      {product.name} ({product.sku})
                    </MenuItem>
                  ))}
                </Select>
                {validationErrors.productId && (
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                    {validationErrors.productId}
                  </Typography>
                )}
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                required
                fullWidth
                label="위치"
                value={formData.location || ''}
                onChange={handleInputChange('location')}
                error={!!validationErrors.location}
                helperText={validationErrors.location}
                placeholder="예: 창고A-1층-A1"
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                required
                fullWidth
                label="현재 수량"
                type="number"
                value={formData.quantity || ''}
                onChange={handleInputChange('quantity')}
                error={!!validationErrors.quantity}
                helperText={validationErrors.quantity}
                InputProps={{
                  inputProps: { min: 0 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="최소 재고"
                type="number"
                value={formData.minStockLevel || ''}
                onChange={handleInputChange('minStockLevel')}
                error={!!validationErrors.minStockLevel}
                helperText={validationErrors.minStockLevel}
                InputProps={{
                  inputProps: { min: 0 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="최대 재고"
                type="number"
                value={formData.maxStockLevel || ''}
                onChange={handleInputChange('maxStockLevel')}
                error={!!validationErrors.maxStockLevel}
                helperText={validationErrors.maxStockLevel}
                InputProps={{
                  inputProps: { min: 0 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>상태</InputLabel>
                <Select
                  value={formData.status || InventoryStatus.IN_STOCK}
                  onChange={handleSelectChange('status')}
                  label="상태"
                >
                  {Object.values(InventoryStatus).map((status) => (
                    <MenuItem key={status} value={status}>
                      {getStatusLabel(status)}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <Button
                  variant="outlined"
                  startIcon={<CancelIcon />}
                  onClick={handleCancel}
                  disabled={loading}
                >
                  취소
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                  disabled={loading}
                >
                  {loading ? '저장 중...' : (isEdit ? '수정' : '등록')}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </Box>
      </Paper>
    </Container>
  );
};

export default InventoryForm;

