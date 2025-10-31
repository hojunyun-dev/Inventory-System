import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  IconButton, Box, CircularProgress, Alert, Chip, TextField, MenuItem, Grid, FormControl, InputLabel, Select, SelectChangeEvent,
} from '@mui/material';
import { 
  Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Search as SearchIcon, DateRange as DateRangeIcon, FilterList as FilterIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { inventoryApi } from '../services/api';
import { Inventory, InventoryStatus } from '../types';

const InventoryList: React.FC = () => {
  const [inventories, setInventories] = useState<Inventory[]>([]);
  const [filteredInventories, setFilteredInventories] = useState<Inventory[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [dateFilter, setDateFilter] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<string>('day');
  const navigate = useNavigate();

  useEffect(() => {
    fetchInventories();
  }, []);

  useEffect(() => {
    filterInventories();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inventories, searchTerm, statusFilter]);

  const fetchInventories = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await inventoryApi.getAll();
      setInventories(response.data);
    } catch (err) {
      setError('재고 목록을 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch inventories:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDateChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setDateFilter(event.target.value);
  };

  const handleTypeChange = (event: SelectChangeEvent) => {
    setTypeFilter(event.target.value);
  };

  const getDateLabel = () => {
    if (!dateFilter) return '전체';
    
    const date = new Date(dateFilter);
    switch (typeFilter) {
      case 'year':
        return `${date.getFullYear()}년`;
      case 'month':
        return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
      case 'day':
      default:
        return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
    }
  };

  const filterInventories = () => {
    let filtered = [...inventories];

    // 검색어 필터링
    if (searchTerm) {
      filtered = filtered.filter(inventory => 
        inventory.productName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        inventory.product?.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        inventory.warehouseLocation?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        inventory.location?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // 상태 필터링
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(inventory => inventory.status === statusFilter);
    }

    setFilteredInventories(filtered);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('정말로 이 재고 항목을 삭제하시겠습니까?')) return;

    try {
      await inventoryApi.delete(id);
      alert('재고 항목이 삭제되었습니다.');
      fetchInventories();
    } catch (err) {
      setError('재고 항목 삭제 중 오류가 발생했습니다.');
      console.error('Failed to delete inventory:', err);
    }
  };

  const getStatusColor = (status: InventoryStatus) => {
    switch (status) {
      case InventoryStatus.IN_STOCK: return 'success';
      case InventoryStatus.LOW_STOCK: return 'warning';
      case InventoryStatus.OUT_OF_STOCK: return 'error';
      case InventoryStatus.DISCONTINUED: return 'default';
      default: return 'default';
    }
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
          재고 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/inventory/new')}
        >
          새 재고 추가
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 검색 및 필터 */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <FilterIcon /> 검색 및 필터
        </Typography>
        
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={3}>
            <TextField
              label="검색 (상품명, 위치)"
              variant="outlined"
              size="small"
              fullWidth
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'action.active' }} />,
              }}
            />
          </Grid>
          
          <Grid item xs={12} sm={2}>
            <TextField
              select
              label="상태 필터"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              size="small"
              fullWidth
            >
              <MenuItem value="ALL">전체</MenuItem>
              {Object.values(InventoryStatus).map((status) => (
                <MenuItem key={status} value={status}>
                  {getStatusLabel(status)}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          
          <Grid item xs={12} sm={2}>
            <FormControl fullWidth size="small">
              <InputLabel>조회 타입</InputLabel>
              <Select
                value={typeFilter}
                label="조회 타입"
                onChange={handleTypeChange}
              >
                <MenuItem value="day">일별</MenuItem>
                <MenuItem value="month">월별</MenuItem>
                <MenuItem value="year">년별</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} sm={3}>
            <TextField
              fullWidth
              type="date"
              label="날짜 선택"
              value={dateFilter}
              onChange={handleDateChange}
              size="small"
              InputLabelProps={{
                shrink: true,
              }}
            />
          </Grid>
          
          <Grid item xs={12} sm={2}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <DateRangeIcon color="primary" />
              <Typography variant="body2" color="primary" fontWeight="bold">
                {getDateLabel()} 조회
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>상품명</TableCell>
              <TableCell>위치</TableCell>
              <TableCell>현재 수량</TableCell>
              <TableCell>최소 재고</TableCell>
              <TableCell>최대 재고</TableCell>
              <TableCell>상태</TableCell>
              <TableCell>최종 업데이트</TableCell>
              <TableCell>작업</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredInventories.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} align="center">
                  <Typography variant="body2" color="textSecondary">
                    {searchTerm || statusFilter !== 'ALL' 
                      ? '검색 조건에 맞는 재고가 없습니다.' 
                      : '등록된 재고가 없습니다.'
                    }
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredInventories.map((inventory) => (
                <TableRow key={inventory.id}>
                  <TableCell>{inventory.id}</TableCell>
                  <TableCell>{inventory.productName || inventory.product?.name || '-'}</TableCell>
                  <TableCell>{inventory.warehouseLocation || inventory.location || '-'}</TableCell>
                  <TableCell>{inventory.quantity?.toLocaleString()}</TableCell>
                  <TableCell>{inventory.minStockLevel?.toLocaleString() || '-'}</TableCell>
                  <TableCell>{inventory.maxStockLevel?.toLocaleString() || '-'}</TableCell>
                  <TableCell>
                    <Chip 
                      label={getStatusLabel(inventory.status || InventoryStatus.IN_STOCK)} 
                      color={getStatusColor(inventory.status || InventoryStatus.IN_STOCK)} 
                      size="small" 
                    />
                  </TableCell>
                  <TableCell>
                    {inventory.updatedAt 
                      ? new Date(inventory.updatedAt).toLocaleDateString()
                      : inventory.lastUpdated 
                      ? new Date(inventory.lastUpdated).toLocaleDateString()
                      : '-'
                    }
                  </TableCell>
                  <TableCell>
                    <IconButton
                      color="primary"
                      onClick={() => navigate(`/inventory/${inventory.id}/edit`)}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(inventory.id!)}
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

export default InventoryList;
