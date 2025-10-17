import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Card,
  CardContent,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  IconButton,
  Tooltip,
  Divider,
  Alert,
  CircularProgress
} from '@mui/material';
import {
  Search,
  FilterList,
  Clear,
  Save,
  History,
  ExpandMore,
  Close,
  Add,
  Remove
} from '@mui/icons-material';
import { productApi } from '../services/api';
import { Product, PartType, MovementCategory, Channel } from '../types';

interface SearchCriteria {
  // 기본 검색
  keyword?: string;
  sku?: string;
  oemPartNumber?: string;
  manufacturerCode?: string;
  aftermarketPartNumber?: string;
  
  // 분류 필터
  partType?: PartType;
  movementCategory?: MovementCategory;
  categoryId?: number;
  
  // 가격 범위
  minPrice?: number;
  maxPrice?: number;
  
  // 재고 상태
  stockStatus?: 'inStock' | 'lowStock' | 'outOfStock';
  minQuantity?: number;
  maxQuantity?: number;
  
  // 시리얼 관리
  isSerialized?: boolean;
  
  // 채널 관련
  channels?: Channel[];
  
  // 날짜 범위
  createdFrom?: string;
  createdTo?: string;
  updatedFrom?: string;
  updatedTo?: string;
  
  // 정렬
  sortBy?: 'name' | 'sku' | 'price' | 'createdAt' | 'updatedAt';
  sortOrder?: 'asc' | 'desc';
}

interface SavedSearch {
  id: string;
  name: string;
  criteria: SearchCriteria;
  createdAt: string;
}

const AdvancedSearch: React.FC = () => {
  const [searchCriteria, setSearchCriteria] = useState<SearchCriteria>({});
  const [searchResults, setSearchResults] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedSections, setExpandedSections] = useState<string[]>(['basic']);
  const [savedSearches, setSavedSearches] = useState<SavedSearch[]>([]);
  const [showAdvanced, setShowAdvanced] = useState(false);

  // 검색 히스토리 (로컬 스토리지에서 로드)
  const [searchHistory, setSearchHistory] = useState<string[]>([]);

  useEffect(() => {
    loadSavedSearches();
    loadSearchHistory();
  }, []);

  const loadSavedSearches = () => {
    const saved = localStorage.getItem('savedSearches');
    if (saved) {
      setSavedSearches(JSON.parse(saved));
    }
  };

  const loadSearchHistory = () => {
    const history = localStorage.getItem('searchHistory');
    if (history) {
      setSearchHistory(JSON.parse(history));
    }
  };

  const saveSearchHistory = (query: string) => {
    const newHistory = [query, ...searchHistory.filter(h => h !== query)].slice(0, 10);
    setSearchHistory(newHistory);
    localStorage.setItem('searchHistory', JSON.stringify(newHistory));
  };

  const handleSearch = async () => {
    setLoading(true);
    setError(null);

    try {
      // 검색 키워드 히스토리에 저장
      if (searchCriteria.keyword) {
        saveSearchHistory(searchCriteria.keyword);
      }

      // 실제 API 호출 (현재는 더미 데이터)
      const results = await mockSearch(searchCriteria);
      setSearchResults(results);
    } catch (err) {
      setError('검색 중 오류가 발생했습니다.');
      console.error('Search error:', err);
    } finally {
      setLoading(false);
    }
  };

  const mockSearch = async (criteria: SearchCriteria): Promise<Product[]> => {
    // 실제 API 연동 시 교체
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    return [
      {
        id: 1,
        name: '브레이크 패드 (전면)',
        sku: 'BP-FRONT-001',
        oemPartNumber: '12345-67890',
        manufacturerCode: 'HYU',
        partType: PartType.BRAKE,
        movementCategory: MovementCategory.FAST_MOVING,
        price: 45000,
        cost: 30000,
        isSerialized: false,
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      },
      {
        id: 2,
        name: '엔진 오일 필터',
        sku: 'EOF-001',
        oemPartNumber: '98765-43210',
        manufacturerCode: 'KIA',
        partType: PartType.ENGINE,
        movementCategory: MovementCategory.CRITICAL,
        price: 15000,
        cost: 10000,
        isSerialized: false,
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
    ];
  };

  const handleClear = () => {
    setSearchCriteria({});
    setSearchResults([]);
    setError(null);
  };

  const handleSaveSearch = () => {
    const searchName = prompt('검색 조건의 이름을 입력하세요:');
    if (searchName) {
      const newSavedSearch: SavedSearch = {
        id: Date.now().toString(),
        name: searchName,
        criteria: { ...searchCriteria },
        createdAt: new Date().toISOString()
      };
      
      const updatedSearches = [...savedSearches, newSavedSearch];
      setSavedSearches(updatedSearches);
      localStorage.setItem('savedSearches', JSON.stringify(updatedSearches));
    }
  };

  const handleLoadSavedSearch = (savedSearch: SavedSearch) => {
    setSearchCriteria(savedSearch.criteria);
  };

  const handleDeleteSavedSearch = (id: string) => {
    const updatedSearches = savedSearches.filter(s => s.id !== id);
    setSavedSearches(updatedSearches);
    localStorage.setItem('savedSearches', JSON.stringify(updatedSearches));
  };

  const handleHistoryClick = (query: string) => {
    setSearchCriteria(prev => ({ ...prev, keyword: query }));
  };

  const toggleSection = (section: string) => {
    setExpandedSections(prev =>
      prev.includes(section)
        ? prev.filter(s => s !== section)
        : [...prev, section]
    );
  };

  const addChip = (key: keyof SearchCriteria, value: any) => {
    setSearchCriteria(prev => ({ ...prev, [key]: value }));
  };

  const removeChip = (key: keyof SearchCriteria) => {
    setSearchCriteria(prev => {
      const newCriteria = { ...prev };
      delete newCriteria[key];
      return newCriteria;
    });
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        고급 검색
      </Typography>

      <Grid container spacing={3}>
        {/* 검색 폼 */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
            {/* 기본 검색 */}
            <Accordion 
              expanded={expandedSections.includes('basic')}
              onChange={() => toggleSection('basic')}
            >
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6">기본 검색</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <TextField
                    label="키워드 검색"
                    value={searchCriteria.keyword || ''}
                    onChange={(e) => setSearchCriteria(prev => ({ ...prev, keyword: e.target.value }))}
                    fullWidth
                  />
                  <TextField
                    label="SKU"
                    value={searchCriteria.sku || ''}
                    onChange={(e) => setSearchCriteria(prev => ({ ...prev, sku: e.target.value }))}
                    fullWidth
                  />
                  <TextField
                    label="OEM 부품번호"
                    value={searchCriteria.oemPartNumber || ''}
                    onChange={(e) => setSearchCriteria(prev => ({ ...prev, oemPartNumber: e.target.value }))}
                    fullWidth
                  />
                  <TextField
                    label="제조사 코드"
                    value={searchCriteria.manufacturerCode || ''}
                    onChange={(e) => setSearchCriteria(prev => ({ ...prev, manufacturerCode: e.target.value }))}
                    fullWidth
                  />
                </Box>
              </AccordionDetails>
            </Accordion>

            {/* 분류 필터 */}
            <Accordion 
              expanded={expandedSections.includes('category')}
              onChange={() => toggleSection('category')}
            >
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6">분류 필터</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <FormControl fullWidth>
                    <InputLabel>부품 유형</InputLabel>
                    <Select
                      value={searchCriteria.partType || ''}
                      onChange={(e) => setSearchCriteria(prev => ({ ...prev, partType: e.target.value as PartType }))}
                    >
                      <MenuItem value="">전체</MenuItem>
                      <MenuItem value="ENGINE">엔진</MenuItem>
                      <MenuItem value="BRAKE">브레이크</MenuItem>
                      <MenuItem value="SUSPENSION">서스펜션</MenuItem>
                      <MenuItem value="ELECTRICAL">전기</MenuItem>
                      <MenuItem value="BODY">바디</MenuItem>
                    </Select>
                  </FormControl>
                  
                  <FormControl fullWidth>
                    <InputLabel>회전 분류</InputLabel>
                    <Select
                      value={searchCriteria.movementCategory || ''}
                      onChange={(e) => setSearchCriteria(prev => ({ ...prev, movementCategory: e.target.value as MovementCategory }))}
                    >
                      <MenuItem value="">전체</MenuItem>
                      <MenuItem value="FAST_MOVING">빠른 회전</MenuItem>
                      <MenuItem value="MEDIUM_MOVING">보통 회전</MenuItem>
                      <MenuItem value="SLOW_MOVING">느린 회전</MenuItem>
                      <MenuItem value="CRITICAL">중요 부품</MenuItem>
                      <MenuItem value="SEASONAL">계절성</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </AccordionDetails>
            </Accordion>

            {/* 가격 및 재고 필터 */}
            <Accordion 
              expanded={expandedSections.includes('price')}
              onChange={() => toggleSection('price')}
            >
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Typography variant="h6">가격 및 재고</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <TextField
                      label="최소 가격"
                      type="number"
                      value={searchCriteria.minPrice || ''}
                      onChange={(e) => setSearchCriteria(prev => ({ ...prev, minPrice: Number(e.target.value) }))}
                      size="small"
                    />
                    <TextField
                      label="최대 가격"
                      type="number"
                      value={searchCriteria.maxPrice || ''}
                      onChange={(e) => setSearchCriteria(prev => ({ ...prev, maxPrice: Number(e.target.value) }))}
                      size="small"
                    />
                  </Box>
                  
                  <FormControl fullWidth>
                    <InputLabel>재고 상태</InputLabel>
                    <Select
                      value={searchCriteria.stockStatus || ''}
                      onChange={(e) => setSearchCriteria(prev => ({ ...prev, stockStatus: e.target.value as any }))}
                    >
                      <MenuItem value="">전체</MenuItem>
                      <MenuItem value="inStock">재고 있음</MenuItem>
                      <MenuItem value="lowStock">재고 부족</MenuItem>
                      <MenuItem value="outOfStock">품절</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </AccordionDetails>
            </Accordion>

            {/* 검색 버튼들 */}
            <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
              <Button
                variant="contained"
                startIcon={<Search />}
                onClick={handleSearch}
                disabled={loading}
                fullWidth
              >
                {loading ? <CircularProgress size={20} /> : '검색'}
              </Button>
              <Button
                variant="outlined"
                startIcon={<Clear />}
                onClick={handleClear}
              >
                초기화
              </Button>
            </Box>

            <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
              <Button
                variant="outlined"
                startIcon={<Save />}
                onClick={handleSaveSearch}
                size="small"
              >
                저장
              </Button>
              <Button
                variant="outlined"
                startIcon={<FilterList />}
                onClick={() => setShowAdvanced(!showAdvanced)}
                size="small"
              >
                고급
              </Button>
            </Box>
          </Paper>
        </Grid>

        {/* 검색 결과 */}
        <Grid item xs={12} md={8}>
          {/* 검색 히스토리 */}
          {searchHistory.length > 0 && (
            <Paper sx={{ p: 2, mb: 2 }}>
              <Typography variant="h6" gutterBottom>
                최근 검색어
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {searchHistory.map((query, index) => (
                  <Chip
                    key={index}
                    label={query}
                    onClick={() => handleHistoryClick(query)}
                    size="small"
                    variant="outlined"
                  />
                ))}
              </Box>
            </Paper>
          )}

          {/* 저장된 검색 */}
          {savedSearches.length > 0 && (
            <Paper sx={{ p: 2, mb: 2 }}>
              <Typography variant="h6" gutterBottom>
                저장된 검색
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {savedSearches.map((search) => (
                  <Chip
                    key={search.id}
                    label={search.name}
                    onClick={() => handleLoadSavedSearch(search)}
                    onDelete={() => handleDeleteSavedSearch(search.id)}
                    size="small"
                    color="primary"
                  />
                ))}
              </Box>
            </Paper>
          )}

          {/* 활성 필터 */}
          <Paper sx={{ p: 2, mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              활성 필터
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {Object.entries(searchCriteria).map(([key, value]) => {
                if (!value) return null;
                return (
                  <Chip
                    key={key}
                    label={`${key}: ${value}`}
                    onDelete={() => removeChip(key as keyof SearchCriteria)}
                    size="small"
                    color="secondary"
                  />
                );
              })}
            </Box>
          </Paper>

          {/* 검색 결과 */}
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {searchResults.length > 0 ? (
            <Box>
              <Typography variant="h6" gutterBottom>
                검색 결과 ({searchResults.length}개)
              </Typography>
              {searchResults.map((product) => (
                <Card key={product.id} sx={{ mb: 2 }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                      <Box>
                        <Typography variant="h6">{product.name}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          SKU: {product.sku} | OEM: {product.oemPartNumber}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          제조사: {product.manufacturerCode} | 유형: {product.partType}
                        </Typography>
                        <Typography variant="h6" color="primary">
                          {product.price?.toLocaleString()}원
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <Chip 
                          label={product.partType} 
                          size="small" 
                          color="primary" 
                          variant="outlined" 
                        />
                        <Chip 
                          label={product.movementCategory} 
                          size="small" 
                          color="secondary" 
                          variant="outlined" 
                        />
                        {product.isSerialized && (
                          <Chip 
                            label="시리얼" 
                            size="small" 
                            color="warning" 
                            variant="outlined" 
                          />
                        )}
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              ))}
            </Box>
          ) : !loading && (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Typography variant="h6" color="text.secondary">
                검색 조건을 입력하고 검색 버튼을 눌러주세요
              </Typography>
            </Paper>
          )}
        </Grid>
      </Grid>
    </Box>
  );
};

export default AdvancedSearch;
