import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, Box, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Button, TextField, MenuItem, Alert, CircularProgress,
} from '@mui/material';
import { Add as AddIcon, QrCode2 as SerialIcon, Warning as WarningIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { SerializedInventory, SerializedInventoryStatus } from '../types';

const SerializedInventoryList: React.FC = () => {
  const navigate = useNavigate();
  const [inventories, setInventories] = useState<SerializedInventory[]>([]);
  const [filteredInventories, setFilteredInventories] = useState<SerializedInventory[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSerializedInventories();
  }, []);

  useEffect(() => {
    filterInventories();
  }, [inventories, statusFilter, searchTerm]);

  const fetchSerializedInventories = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/serialized-inventories', {
        withCredentials: true
      });
      setInventories(response.data);
    } catch (err) {
      setError('시리얼 재고 목록을 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch serialized inventories:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterInventories = () => {
    let filtered = [...inventories];

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(inv => inv.status === statusFilter);
    }

    if (searchTerm) {
      filtered = filtered.filter(inv =>
        inv.serialNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (inv.productName && inv.productName.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (inv.batchNumber && inv.batchNumber.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    setFilteredInventories(filtered);
  };

  const getStatusColor = (status: SerializedInventoryStatus) => {
    const colors = {
      [SerializedInventoryStatus.IN_STOCK]: 'success',
      [SerializedInventoryStatus.RESERVED]: 'info',
      [SerializedInventoryStatus.SOLD]: 'default',
      [SerializedInventoryStatus.IN_TRANSIT]: 'warning',
      [SerializedInventoryStatus.RETURNED]: 'error',
      [SerializedInventoryStatus.DEFECTIVE]: 'error',
      [SerializedInventoryStatus.SCRAPPED]: 'default',
      [SerializedInventoryStatus.UNDER_REPAIR]: 'warning',
    };
    return colors[status] || 'default';
  };

  const getStatusLabel = (status: SerializedInventoryStatus) => {
    const labels = {
      [SerializedInventoryStatus.IN_STOCK]: '재고',
      [SerializedInventoryStatus.RESERVED]: '예약',
      [SerializedInventoryStatus.SOLD]: '판매',
      [SerializedInventoryStatus.IN_TRANSIT]: '이동중',
      [SerializedInventoryStatus.RETURNED]: '반품',
      [SerializedInventoryStatus.DEFECTIVE]: '불량',
      [SerializedInventoryStatus.SCRAPPED]: '폐기',
      [SerializedInventoryStatus.UNDER_REPAIR]: '수리중',
    };
    return labels[status] || status;
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
          <SerialIcon fontSize="large" /> 시리얼 넘버 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/serialized-inventory/new')}
        >
          신규 등록
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 필터 */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <TextField
            label="검색 (시리얼번호, 제품명, 배치번호)"
            size="small"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            sx={{ minWidth: 300 }}
          />
          <TextField
            select
            label="상태"
            size="small"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            sx={{ minWidth: 150 }}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value={SerializedInventoryStatus.IN_STOCK}>재고</MenuItem>
            <MenuItem value={SerializedInventoryStatus.RESERVED}>예약</MenuItem>
            <MenuItem value={SerializedInventoryStatus.SOLD}>판매</MenuItem>
            <MenuItem value={SerializedInventoryStatus.IN_TRANSIT}>이동중</MenuItem>
            <MenuItem value={SerializedInventoryStatus.RETURNED}>반품</MenuItem>
            <MenuItem value={SerializedInventoryStatus.DEFECTIVE}>불량</MenuItem>
          </TextField>
          <Chip
            label={`${filteredInventories.length}개 항목`}
            color="primary"
            variant="outlined"
          />
        </Box>
      </Paper>

      {/* 테이블 */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>시리얼 번호</TableCell>
              <TableCell>제품명</TableCell>
              <TableCell>배치번호</TableCell>
              <TableCell>상태</TableCell>
              <TableCell>위치</TableCell>
              <TableCell>보증 상태</TableCell>
              <TableCell>구매/판매일</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredInventories.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">
                  <Typography color="textSecondary">
                    {searchTerm || statusFilter !== 'ALL'
                      ? '검색 조건에 맞는 항목이 없습니다.'
                      : '등록된 시리얼 재고가 없습니다.'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredInventories.map((item) => (
                <TableRow key={item.id} hover>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      {item.serialNumber}
                    </Typography>
                  </TableCell>
                  <TableCell>{item.productName}</TableCell>
                  <TableCell>{item.batchNumber || '-'}</TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusLabel(item.status)}
                      color={getStatusColor(item.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {item.warehouseLocation?.compactLocation || '-'}
                  </TableCell>
                  <TableCell>
                    {item.isUnderWarranty ? (
                      <Chip label={`${item.remainingWarrantyDays}일 남음`} color="success" size="small" />
                    ) : item.warrantyEndDate ? (
                      <Chip label="만료" color="default" size="small" />
                    ) : '-'}
                    {item.isUnderWarranty && item.remainingWarrantyDays! < 30 && (
                      <WarningIcon fontSize="small" color="warning" sx={{ ml: 0.5 }} />
                    )}
                  </TableCell>
                  <TableCell>
                    {item.purchaseDate && (
                      <Typography variant="caption" display="block">
                        구매: {new Date(item.purchaseDate).toLocaleDateString()}
                      </Typography>
                    )}
                    {item.soldDate && (
                      <Typography variant="caption" display="block">
                        판매: {new Date(item.soldDate).toLocaleDateString()}
                      </Typography>
                    )}
                    {!item.purchaseDate && !item.soldDate && '-'}
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

export default SerializedInventoryList;

