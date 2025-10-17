import React, { useState, useEffect } from 'react';
import {
  Container, Paper, TextField, Button, Typography, Box, Alert, CircularProgress,
  FormControl, InputLabel, Select, MenuItem, Grid, Divider, FormControlLabel,
  Checkbox, Accordion, AccordionSummary, AccordionDetails,
} from '@mui/material';
import {
  Save as SaveIcon, Cancel as CancelIcon, ArrowBack as BackIcon,
  ExpandMore as ExpandMoreIcon,
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { productApi, categoryApi } from '../services/api';
import { Product, Category, PartType, MovementCategory } from '../types';

const ProductForm: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);

  const [formData, setFormData] = useState<Partial<Product>>({
    name: '',
    description: '',
    price: 0,
    sku: '',
    barcode: '',
    categoryId: undefined,
    // 자동차 부품 필드
    oemPartNumber: '',
    manufacturerCode: '',
    aftermarketPartNumber: '',
    partType: undefined,
    movementCategory: undefined,
    minimumOrderQuantity: undefined,
    leadTimeDays: undefined,
    reorderPoint: undefined,
    warrantyMonths: undefined,
    weight: undefined,
    dimensions: '',
    isSerialized: false,
    imageUrl: '',
    technicalDrawingUrl: '',
    notes: '',
  });

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    fetchCategories();
    if (isEdit && id) {
      fetchProduct(parseInt(id));
    }
  }, [isEdit, id]);

  const fetchCategories = async () => {
    try {
      const response = await categoryApi.getAll();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  const fetchProduct = async (productId: number) => {
    try {
      setLoading(true);
      const response = await productApi.getById(productId);
      const product = response.data;
      setFormData({
        name: product.name,
        description: product.description || '',
        price: product.price,
        sku: product.sku,
        barcode: product.barcode || '',
        categoryId: product.categoryId,
        // 자동차 부품 필드
        oemPartNumber: product.oemPartNumber || '',
        manufacturerCode: product.manufacturerCode || '',
        aftermarketPartNumber: product.aftermarketPartNumber || '',
        partType: product.partType,
        movementCategory: product.movementCategory,
        minimumOrderQuantity: product.minimumOrderQuantity,
        leadTimeDays: product.leadTimeDays,
        reorderPoint: product.reorderPoint,
        warrantyMonths: product.warrantyMonths,
        weight: product.weight,
        dimensions: product.dimensions || '',
        isSerialized: product.isSerialized || false,
        imageUrl: product.imageUrl || '',
        technicalDrawingUrl: product.technicalDrawingUrl || '',
        notes: product.notes || '',
      });
    } catch (err) {
      setError('상품 정보를 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch product:', err);
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.name?.trim()) {
      errors.name = '상품명을 입력해주세요.';
    }

    if (!formData.sku?.trim()) {
      errors.sku = 'SKU를 입력해주세요.';
    }

    if (!formData.price || formData.price <= 0) {
      errors.price = '올바른 가격을 입력해주세요.';
    }

    if (!formData.categoryId) {
      errors.categoryId = '카테고리를 선택해주세요.';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const validateBarcode = async (barcode: string | undefined): Promise<boolean> => {
    if (!barcode || !barcode.trim()) return true; // 바코드는 선택사항

    try {
      const response = await productApi.checkBarcode(barcode);
      if (response.data.exists) {
        setValidationErrors(prev => ({
          ...prev,
          barcode: '이미 사용 중인 바코드입니다.',
        }));
        return false;
      }
      return true;
    } catch (err) {
      console.error('Barcode validation error:', err);
      return true; // 검증 실패 시 통과
    }
  };

  const handleInputChange = (field: keyof Product) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = field === 'price' ? parseFloat(event.target.value) || 0 : event.target.value;
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

  const handleCategoryChange = (event: any) => {
    setFormData(prev => ({
      ...prev,
      categoryId: event.target.value,
    }));
    
    if (validationErrors.categoryId) {
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors.categoryId;
        return newErrors;
      });
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    // 바코드 중복 검사 (신규 등록 시에만)
    if (!isEdit && !(await validateBarcode(formData.barcode))) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      if (isEdit && id) {
        await productApi.update(parseInt(id), formData);
        alert('상품이 수정되었습니다.');
      } else {
        await productApi.create(formData as Omit<Product, 'id' | 'createdAt' | 'updatedAt'>);
        alert('상품이 등록되었습니다.');
      }
      navigate('/products');
    } catch (err) {
      setError(isEdit ? '상품 수정 중 오류가 발생했습니다.' : '상품 등록 중 오류가 발생했습니다.');
      console.error('Product save error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/products');
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
          onClick={() => navigate('/products')}
          sx={{ mr: 2 }}
        >
          목록으로
        </Button>
        <Typography variant="h4" component="h1">
          {isEdit ? '상품 수정' : '신규 상품 등록'}
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
              <TextField
                required
                fullWidth
                label="상품명"
                value={formData.name || ''}
                onChange={handleInputChange('name')}
                error={!!validationErrors.name}
                helperText={validationErrors.name}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                required
                fullWidth
                label="SKU"
                value={formData.sku || ''}
                onChange={handleInputChange('sku')}
                error={!!validationErrors.sku}
                helperText={validationErrors.sku}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                required
                fullWidth
                label="가격"
                type="number"
                value={formData.price || ''}
                onChange={handleInputChange('price')}
                error={!!validationErrors.price}
                helperText={validationErrors.price}
                InputProps={{
                  inputProps: { min: 0, step: 0.01 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth required error={!!validationErrors.categoryId}>
                <InputLabel>카테고리</InputLabel>
                <Select
                  value={formData.categoryId || ''}
                  onChange={handleCategoryChange}
                  label="카테고리"
                >
                  {categories.map((category) => (
                    <MenuItem key={category.id} value={category.id}>
                      {category.name}
                    </MenuItem>
                  ))}
                </Select>
                {validationErrors.categoryId && (
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                    {validationErrors.categoryId}
                  </Typography>
                )}
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="바코드 (선택사항)"
                value={formData.barcode || ''}
                onChange={handleInputChange('barcode')}
                error={!!validationErrors.barcode}
                helperText={validationErrors.barcode}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="상품 설명"
                multiline
                rows={4}
                value={formData.description || ''}
                onChange={handleInputChange('description')}
              />
            </Grid>

            {/* 자동차 부품 특화 필드 */}
            <Grid item xs={12}>
              <Divider sx={{ my: 2 }}>
                <Typography variant="h6" color="primary">
                  🚗 자동차 부품 정보
                </Typography>
              </Divider>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="OEM 부품 번호"
                value={formData.oemPartNumber || ''}
                onChange={handleInputChange('oemPartNumber')}
                helperText="제조사 순정 부품 번호"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="제조사 코드"
                value={formData.manufacturerCode || ''}
                onChange={handleInputChange('manufacturerCode')}
                helperText="예: HYU, KIA, BMW"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="애프터마켓 부품 번호"
                value={formData.aftermarketPartNumber || ''}
                onChange={handleInputChange('aftermarketPartNumber')}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>부품 유형</InputLabel>
                <Select
                  value={formData.partType || ''}
                  onChange={(e) => setFormData(prev => ({ ...prev, partType: e.target.value as PartType }))}
                  label="부품 유형"
                >
                  <MenuItem value="">선택 안 함</MenuItem>
                  <MenuItem value={PartType.ENGINE}>엔진</MenuItem>
                  <MenuItem value={PartType.TRANSMISSION}>변속기</MenuItem>
                  <MenuItem value={PartType.BRAKE}>브레이크</MenuItem>
                  <MenuItem value={PartType.SUSPENSION}>서스펜션</MenuItem>
                  <MenuItem value={PartType.ELECTRICAL}>전기 부품</MenuItem>
                  <MenuItem value={PartType.EXHAUST}>배기 시스템</MenuItem>
                  <MenuItem value={PartType.COOLING}>냉각 시스템</MenuItem>
                  <MenuItem value={PartType.FUEL}>연료 시스템</MenuItem>
                  <MenuItem value={PartType.INTERIOR}>실내 부품</MenuItem>
                  <MenuItem value={PartType.EXTERIOR}>외장 부품</MenuItem>
                  <MenuItem value={PartType.TIRE_WHEEL}>타이어/휠</MenuItem>
                  <MenuItem value={PartType.LIGHTING}>조명</MenuItem>
                  <MenuItem value={PartType.FILTER}>필터류</MenuItem>
                  <MenuItem value={PartType.FLUID}>오일/액류</MenuItem>
                  <MenuItem value={PartType.BODY}>차체</MenuItem>
                  <MenuItem value={PartType.CLIMATE_CONTROL}>공조 시스템</MenuItem>
                  <MenuItem value={PartType.STEERING}>조향 장치</MenuItem>
                  <MenuItem value={PartType.SAFETY}>안전 부품</MenuItem>
                  <MenuItem value={PartType.ACCESSORY}>악세서리</MenuItem>
                  <MenuItem value={PartType.OTHER}>기타</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>회전율 분류</InputLabel>
                <Select
                  value={formData.movementCategory || ''}
                  onChange={(e) => setFormData(prev => ({ ...prev, movementCategory: e.target.value as MovementCategory }))}
                  label="회전율 분류"
                >
                  <MenuItem value="">선택 안 함</MenuItem>
                  <MenuItem value={MovementCategory.FAST_MOVING}>빠른 부품 (자주 판매)</MenuItem>
                  <MenuItem value={MovementCategory.SLOW_MOVING}>느린 부품</MenuItem>
                  <MenuItem value={MovementCategory.CRITICAL}>긴급 부품</MenuItem>
                  <MenuItem value={MovementCategory.OBSOLETE}>단종 부품</MenuItem>
                  <MenuItem value={MovementCategory.SEASONAL}>계절성 부품</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="최소 발주 수량 (MOQ)"
                type="number"
                value={formData.minimumOrderQuantity || ''}
                onChange={handleInputChange('minimumOrderQuantity')}
                InputProps={{
                  inputProps: { min: 1 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="리드타임 (일)"
                type="number"
                value={formData.leadTimeDays || ''}
                onChange={handleInputChange('leadTimeDays')}
                InputProps={{
                  inputProps: { min: 0 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="재발주 시점"
                type="number"
                value={formData.reorderPoint || ''}
                onChange={handleInputChange('reorderPoint')}
                helperText="재고가 이 수량 이하면 재발주"
                InputProps={{
                  inputProps: { min: 0 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="보증 기간 (개월)"
                type="number"
                value={formData.warrantyMonths || ''}
                onChange={handleInputChange('warrantyMonths')}
                InputProps={{
                  inputProps: { min: 0 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="무게 (kg)"
                type="number"
                value={formData.weight || ''}
                onChange={handleInputChange('weight')}
                InputProps={{
                  inputProps: { min: 0, step: 0.1 }
                }}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="치수"
                value={formData.dimensions || ''}
                onChange={handleInputChange('dimensions')}
                helperText="예: 20x15x10 cm"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="부품 이미지 URL"
                value={formData.imageUrl || ''}
                onChange={handleInputChange('imageUrl')}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="기술 도면 URL"
                value={formData.technicalDrawingUrl || ''}
                onChange={handleInputChange('technicalDrawingUrl')}
              />
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.isSerialized || false}
                    onChange={(e) => setFormData(prev => ({ ...prev, isSerialized: e.target.checked }))}
                  />
                }
                label="시리얼 넘버 관리 (고가 부품)"
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="추가 메모"
                multiline
                rows={3}
                value={formData.notes || ''}
                onChange={handleInputChange('notes')}
              />
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

export default ProductForm;

