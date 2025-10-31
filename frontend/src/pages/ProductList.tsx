import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  IconButton, Box, CircularProgress, Alert, TextField, MenuItem, Chip,
} from '@mui/material';
import { 
  Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Search as SearchIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { productApi, categoryApi } from '../services/api';
import { Product, Category, PartType, MovementCategory } from '../types';

const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [priceRange, setPriceRange] = useState<{ min: string; max: string }>({ min: '', max: '' });
  const [partTypeFilter, setPartTypeFilter] = useState<string>('ALL');
  const [movementCategoryFilter, setMovementCategoryFilter] = useState<string>('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  useEffect(() => {
    filterProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [products, searchTerm, categoryFilter, priceRange, partTypeFilter, movementCategoryFilter]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await productApi.getAll();
      setProducts(response.data);
    } catch (err) {
      setError('상품 목록을 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch products:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await categoryApi.getAll();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  const filterProducts = () => {
    let filtered = [...products];

    // 검색어 필터링 (OEM 번호, 제조사 코드 포함)
    if (searchTerm) {
      filtered = filtered.filter(product =>
        product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.sku.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (product.barcode && product.barcode.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (product.description && product.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (product.oemPartNumber && product.oemPartNumber.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (product.manufacturerCode && product.manufacturerCode.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    // 카테고리 필터링
    if (categoryFilter !== 'ALL') {
      filtered = filtered.filter(product => product.categoryId?.toString() === categoryFilter);
    }

    // 가격 범위 필터링
    if (priceRange.min) {
      const minPrice = parseFloat(priceRange.min);
      filtered = filtered.filter(product => product.price >= minPrice);
    }
    if (priceRange.max) {
      const maxPrice = parseFloat(priceRange.max);
      filtered = filtered.filter(product => product.price <= maxPrice);
    }

    // 부품 유형 필터링
    if (partTypeFilter !== 'ALL') {
      filtered = filtered.filter(product => product.partType === partTypeFilter);
    }

    // 회전율 분류 필터링
    if (movementCategoryFilter !== 'ALL') {
      filtered = filtered.filter(product => product.movementCategory === movementCategoryFilter);
    }

    setFilteredProducts(filtered);
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

  const handleDelete = async (id: number) => {
    if (!window.confirm('정말로 이 상품을 삭제하시겠습니까?')) return;

    try {
      await productApi.delete(id);
      alert('상품이 삭제되었습니다.');
      fetchProducts();
    } catch (err) {
      setError('상품 삭제 중 오류가 발생했습니다.');
      console.error('Failed to delete product:', err);
    }
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
        <Typography variant="h4" component="h1">
          상품 목록
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/products/new')}
        >
          새 상품 추가
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 검색 및 필터 */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
          <TextField
            label="검색 (상품명, SKU, OEM번호, 제조사)"
            variant="outlined"
            size="small"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: <SearchIcon sx={{ mr: 1, color: 'action.active' }} />,
            }}
            sx={{ minWidth: 300 }}
          />
          <TextField
            select
            label="카테고리"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            size="small"
            sx={{ minWidth: 150 }}
          >
            <MenuItem value="ALL">전체</MenuItem>
            {categories.map((category) => (
              <MenuItem key={category.id} value={category.id?.toString()}>
                {category.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="부품 유형"
            value={partTypeFilter}
            onChange={(e) => setPartTypeFilter(e.target.value)}
            size="small"
            sx={{ minWidth: 150 }}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value={PartType.BRAKE}>브레이크</MenuItem>
            <MenuItem value={PartType.ENGINE}>엔진</MenuItem>
            <MenuItem value={PartType.TRANSMISSION}>변속기</MenuItem>
            <MenuItem value={PartType.SUSPENSION}>서스펜션</MenuItem>
            <MenuItem value={PartType.ELECTRICAL}>전기</MenuItem>
            <MenuItem value={PartType.FILTER}>필터</MenuItem>
            <MenuItem value={PartType.FLUID}>오일/액</MenuItem>
            <MenuItem value={PartType.OTHER}>기타</MenuItem>
          </TextField>
          <TextField
            select
            label="회전율"
            value={movementCategoryFilter}
            onChange={(e) => setMovementCategoryFilter(e.target.value)}
            size="small"
            sx={{ minWidth: 130 }}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value={MovementCategory.FAST_MOVING}>빠름</MenuItem>
            <MenuItem value={MovementCategory.CRITICAL}>긴급</MenuItem>
            <MenuItem value={MovementCategory.SLOW_MOVING}>느림</MenuItem>
            <MenuItem value={MovementCategory.OBSOLETE}>단종</MenuItem>
          </TextField>
          <TextField
            label="최소 가격"
            type="number"
            size="small"
            value={priceRange.min}
            onChange={(e) => setPriceRange(prev => ({ ...prev, min: e.target.value }))}
            InputProps={{
              inputProps: { min: 0, step: 0.01 }
            }}
            sx={{ width: 120 }}
          />
          <TextField
            label="최대 가격"
            type="number"
            size="small"
            value={priceRange.max}
            onChange={(e) => setPriceRange(prev => ({ ...prev, max: e.target.value }))}
            InputProps={{
              inputProps: { min: 0, step: 0.01 }
            }}
            sx={{ width: 120 }}
          />
          {(searchTerm || categoryFilter !== 'ALL' || priceRange.min || priceRange.max || partTypeFilter !== 'ALL' || movementCategoryFilter !== 'ALL') && (
            <Chip
              label={`${filteredProducts.length}개 상품`}
              color="primary"
              variant="outlined"
            />
          )}
        </Box>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>이미지</TableCell>
              <TableCell>상품명</TableCell>
              <TableCell>SKU</TableCell>
              <TableCell>OEM 번호</TableCell>
              <TableCell>제조사</TableCell>
              <TableCell>부품 유형</TableCell>
              <TableCell>회전율</TableCell>
              <TableCell>가격</TableCell>
              <TableCell>재고</TableCell>
              <TableCell>작업</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredProducts.length === 0 ? (
              <TableRow>
                <TableCell colSpan={11} align="center">
                  <Typography variant="body2" color="textSecondary">
                    {searchTerm || categoryFilter !== 'ALL' || priceRange.min || priceRange.max || partTypeFilter !== 'ALL' || movementCategoryFilter !== 'ALL'
                      ? '검색 조건에 맞는 상품이 없습니다.'
                      : '등록된 상품이 없습니다.'
                    }
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredProducts.map((product) => (
                <TableRow key={product.id}>
                  <TableCell>{product.id}</TableCell>
                  <TableCell>
                    {product.firstImageUrl ? (
                      <Box
                        component="img"
                        src={`http://localhost:8080${product.firstImageUrl}`}
                        alt={product.name}
                        sx={{
                          width: 50,
                          height: 50,
                          objectFit: 'cover',
                          borderRadius: 1,
                          border: '1px solid #e0e0e0'
                        }}
                        onError={(e) => {
                          e.currentTarget.style.display = 'none';
                        }}
                      />
                    ) : (
                      <Box
                        sx={{
                          width: 50,
                          height: 50,
                          backgroundColor: '#f5f5f5',
                          borderRadius: 1,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          border: '1px solid #e0e0e0'
                        }}
                      >
                        <Typography variant="caption" color="textSecondary">
                          {product.imageCount || 0}
                        </Typography>
                      </Box>
                    )}
                  </TableCell>
                  <TableCell>
                    <Box>
                      <Typography variant="body2" fontWeight="bold">{product.name}</Typography>
                      {product.isSerialized && (
                        <Chip label="시리얼관리" size="small" color="secondary" sx={{ mt: 0.5 }} />
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>{product.sku}</TableCell>
                  <TableCell>{product.oemPartNumber || '-'}</TableCell>
                  <TableCell>
                    <Box>
                      <Typography variant="body2" fontWeight="bold">
                        {product.manufacturerCode || '-'}
                      </Typography>
                      {product.vehicleManufacturer && (
                        <Typography variant="caption" color="textSecondary" display="block">
                          {product.vehicleManufacturer}
                          {product.vehicleModel && ` ${product.vehicleModel}`}
                          {product.vehicleYear && ` (${product.vehicleYear})`}
                        </Typography>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {product.partType ? (
                      <Chip label={getPartTypeLabel(product.partType)} size="small" color="default" />
                    ) : '-'}
                  </TableCell>
                  <TableCell>
                    {product.movementCategory === MovementCategory.FAST_MOVING && (
                      <Chip label="빠름" size="small" color="success" />
                    )}
                    {product.movementCategory === MovementCategory.CRITICAL && (
                      <Chip label="긴급" size="small" color="error" />
                    )}
                    {product.movementCategory === MovementCategory.SLOW_MOVING && (
                      <Chip label="느림" size="small" color="default" />
                    )}
                    {product.movementCategory === MovementCategory.OBSOLETE && (
                      <Chip label="단종" size="small" color="warning" />
                    )}
                    {product.movementCategory === MovementCategory.SEASONAL && (
                      <Chip label="계절" size="small" color="info" />
                    )}
                    {!product.movementCategory && '-'}
                  </TableCell>
                  <TableCell>{product.price?.toLocaleString()}원</TableCell>
                  <TableCell>
                    {product.reorderPoint && (
                      <Typography variant="caption" color="textSecondary">
                        재발주: {product.reorderPoint}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <IconButton
                      color="info"
                      onClick={() => navigate(`/products/${product.id}`)}
                      title="조회"
                      size="small"
                    >
                      <ViewIcon />
                    </IconButton>
                    <IconButton
                      color="primary"
                      onClick={() => navigate(`/products/${product.id}/edit`)}
                      title="수정"
                      size="small"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(product.id!)}
                      title="삭제"
                      size="small"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Container>
  );
};

export default ProductList;
