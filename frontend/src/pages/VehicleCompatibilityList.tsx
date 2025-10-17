import React, { useState, useEffect } from 'react';
import {
  Container, Paper, Typography, Box, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Button, TextField, Alert, CircularProgress, IconButton,
} from '@mui/material';
import { Add as AddIcon, DirectionsCar as CarIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import axios from 'axios';
import { VehicleCompatibility } from '../types';

const VehicleCompatibilityList: React.FC = () => {
  const [compatibilities, setCompatibilities] = useState<VehicleCompatibility[]>([]);
  const [filteredCompatibilities, setFilteredCompatibilities] = useState<VehicleCompatibility[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchCompatibilities();
  }, []);

  useEffect(() => {
    filterCompatibilities();
  }, [compatibilities, searchTerm]);

  const fetchCompatibilities = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/vehicle-compatibilities', {
        withCredentials: true
      });
      setCompatibilities(response.data);
    } catch (err) {
      setError('차량 호환성 목록을 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch compatibilities:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterCompatibilities = () => {
    if (!searchTerm) {
      setFilteredCompatibilities(compatibilities);
      return;
    }

    const filtered = compatibilities.filter(vc =>
      vc.manufacturer.toLowerCase().includes(searchTerm.toLowerCase()) ||
      vc.model.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (vc.engineType && vc.engineType.toLowerCase().includes(searchTerm.toLowerCase()))
    );
    setFilteredCompatibilities(filtered);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('정말로 이 차량 정보를 삭제하시겠습니까?')) return;

    try {
      await axios.delete(`http://localhost:8080/api/vehicle-compatibilities/${id}`, {
        withCredentials: true
      });
      alert('차량 정보가 삭제되었습니다.');
      fetchCompatibilities();
    } catch (err) {
      alert('삭제 중 오류가 발생했습니다.');
      console.error('Failed to delete:', err);
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
        <Typography variant="h4" component="h1" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <CarIcon fontSize="large" /> 차량 호환성 관리
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => alert('차량 등록 기능은 추후 구현 예정입니다.')}
        >
          신규 등록
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 검색 */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <TextField
          fullWidth
          label="검색 (제조사, 모델, 엔진)"
          size="small"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </Paper>

      {/* 테이블 */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>제조사</TableCell>
              <TableCell>모델</TableCell>
              <TableCell>연식</TableCell>
              <TableCell>엔진 타입</TableCell>
              <TableCell>변속기</TableCell>
              <TableCell>트림</TableCell>
              <TableCell>호환 제품 수</TableCell>
              <TableCell>작업</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredCompatibilities.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography color="textSecondary">
                    {searchTerm ? '검색 결과가 없습니다.' : '등록된 차량 정보가 없습니다.'}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredCompatibilities.map((vc) => (
                <TableRow key={vc.id} hover>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      {vc.manufacturer}
                    </Typography>
                  </TableCell>
                  <TableCell>{vc.model}</TableCell>
                  <TableCell>{vc.yearRange}</TableCell>
                  <TableCell>{vc.engineType || '-'}</TableCell>
                  <TableCell>{vc.transmission || '-'}</TableCell>
                  <TableCell>{vc.trim || '-'}</TableCell>
                  <TableCell>{vc.productIds?.length || 0}개</TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      color="primary"
                      onClick={() => alert('수정 기능은 추후 구현 예정입니다.')}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleDelete(vc.id!)}
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

export default VehicleCompatibilityList;

