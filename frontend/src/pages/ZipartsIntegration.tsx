import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Chip,
  Alert,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tabs,
  Tab,
  IconButton,
  Tooltip
} from '@mui/material';
import {
  Sync as SyncIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  Refresh as RefreshIcon,
  Download as DownloadIcon,
  FilterList as FilterListIcon
} from '@mui/icons-material';
import axios from 'axios';

interface SyncResult {
  totalProcessed: number;
  successCount: number;
  errorCount: number;
  errors: string[];
  message: string;
}

interface SyncHistory {
  id: string;
  timestamp: string;
  status: 'success' | 'error' | 'running';
  totalProcessed: number;
  successCount: number;
  errorCount: number;
  duration: number;
}

const GpartsIntegration: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [syncResult, setSyncResult] = useState<SyncResult | null>(null);
  const [apiStatus, setApiStatus] = useState<string>('unknown');
  const [syncHistory, setSyncHistory] = useState<SyncHistory[]>([]);
  const [selectedTab, setSelectedTab] = useState(0);
  const [openSyncDialog, setOpenSyncDialog] = useState(false);
  const [syncType, setSyncType] = useState<'full' | 'manufacturer' | 'category'>('full');
  const [manufacturerCode, setManufacturerCode] = useState('');
  const [categoryName, setCategoryName] = useState('');

  useEffect(() => {
    checkApiStatus();
    fetchSyncHistory();
  }, []);

  const checkApiStatus = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/gparts/status', {
        withCredentials: true
      });
      setApiStatus(response.data.status);
    } catch (error) {
      setApiStatus('error');
      console.error('API 상태 확인 실패:', error);
    }
  };

  const fetchSyncHistory = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/gparts/sync/history', {
        withCredentials: true
      });
      setSyncHistory(response.data.history || []);
    } catch (error) {
      console.error('동기화 이력 조회 실패:', error);
    }
  };

  const handleFullSync = async () => {
    setLoading(true);
    setSyncResult(null);
    
    try {
      const response = await axios.post('http://localhost:8080/api/gparts/sync', {}, {
        withCredentials: true
      });
      setSyncResult(response.data);
      fetchSyncHistory();
    } catch (error: any) {
      setSyncResult({
        totalProcessed: 0,
        successCount: 0,
        errorCount: 1,
        errors: [error.response?.data?.error || '동기화 중 오류가 발생했습니다'],
        message: '동기화 실패'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSelectiveSync = async () => {
    setLoading(true);
    setSyncResult(null);
    
    try {
      let response;
      if (syncType === 'manufacturer') {
        response = await axios.post(
          `http://localhost:8080/api/gparts/sync/manufacturer/${manufacturerCode}`,
          {},
          { withCredentials: true }
        );
      } else if (syncType === 'category') {
        response = await axios.post(
          `http://localhost:8080/api/gparts/sync/category/${categoryName}`,
          {},
          { withCredentials: true }
        );
      }
      
      if (response) {
        setSyncResult(response.data);
        fetchSyncHistory();
      }
    } catch (error: any) {
      setSyncResult({
        totalProcessed: 0,
        successCount: 0,
        errorCount: 1,
        errors: [error.response?.data?.error || '선택적 동기화 중 오류가 발생했습니다'],
        message: '선택적 동기화 실패'
      });
    } finally {
      setLoading(false);
      setOpenSyncDialog(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'connected': return 'success';
      case 'error': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'connected': return <CheckCircleIcon />;
      case 'error': return <ErrorIcon />;
      default: return <InfoIcon />;
    }
  };

  const TabPanel: React.FC<{ children: React.ReactNode; value: number; index: number }> = ({ children, value, index }) => (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        🚗 Gparts 자동차 부품 카탈로그 연동
      </Typography>
      
      <Tabs value={selectedTab} onChange={(e, newValue) => setSelectedTab(newValue)} sx={{ mb: 3 }}>
        <Tab label="동기화 관리" />
        <Tab label="동기화 이력" />
        <Tab label="API 상태" />
      </Tabs>

      <TabPanel value={selectedTab} index={0}>
        <Grid container spacing={3}>
          {/* API 상태 카드 */}
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" mb={2}>
                  {getStatusIcon(apiStatus)}
                  <Typography variant="h6" sx={{ ml: 1 }}>
                    API 연결 상태
                  </Typography>
                </Box>
                <Chip 
                  label={apiStatus === 'connected' ? '연결됨' : apiStatus === 'error' ? '연결 실패' : '확인 중'}
                  color={getStatusColor(apiStatus) as any}
                  size="small"
                />
                <Button
                  startIcon={<RefreshIcon />}
                  onClick={checkApiStatus}
                  size="small"
                  sx={{ ml: 2 }}
                >
                  새로고침
                </Button>
              </CardContent>
            </Card>
          </Grid>

          {/* 전체 동기화 카드 */}
          <Grid item xs={12} md={8}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  전체 카탈로그 동기화
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Gparts에서 모든 자동차 부품 카탈로그 데이터를 가져와 시스템에 동기화합니다.
                </Typography>
                <Box display="flex" gap={2}>
                  <Button
                    variant="contained"
                    startIcon={<SyncIcon />}
                    onClick={handleFullSync}
                    disabled={loading}
                    color="primary"
                  >
                    {loading ? '동기화 중...' : '전체 동기화 시작'}
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<FilterListIcon />}
                    onClick={() => setOpenSyncDialog(true)}
                    disabled={loading}
                  >
                    선택적 동기화
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* 동기화 결과 */}
          {syncResult && (
            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    동기화 결과
                  </Typography>
                  
                  <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid item xs={3}>
                      <Box textAlign="center">
                        <Typography variant="h4" color="primary">
                          {syncResult.totalProcessed}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          전체 처리
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box textAlign="center">
                        <Typography variant="h4" color="success.main">
                          {syncResult.successCount}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          성공
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box textAlign="center">
                        <Typography variant="h4" color="error.main">
                          {syncResult.errorCount}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          실패
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box textAlign="center">
                        <Typography variant="h4" color="info.main">
                          {syncResult.totalProcessed > 0 
                            ? Math.round((syncResult.successCount / syncResult.totalProcessed) * 100)
                            : 0}%
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          성공률
                        </Typography>
                      </Box>
                    </Grid>
                  </Grid>

                  {syncResult.errors && syncResult.errors.length > 0 && (
                    <Alert severity="error" sx={{ mb: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        오류 목록:
                      </Typography>
                      {syncResult.errors.slice(0, 5).map((error, index) => (
                        <Typography key={index} variant="body2">
                          • {error}
                        </Typography>
                      ))}
                      {syncResult.errors.length > 5 && (
                        <Typography variant="body2" color="text.secondary">
                          ... 및 {syncResult.errors.length - 5}개 추가 오류
                        </Typography>
                      )}
                    </Alert>
                  )}

                  <Alert severity={syncResult.errorCount === 0 ? "success" : "warning"}>
                    {syncResult.message}
                  </Alert>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* 로딩 인디케이터 */}
          {loading && (
            <Grid item xs={12}>
              <Box display="flex" justifyContent="center" alignItems="center" py={4}>
                <CircularProgress size={40} />
                <Typography variant="body1" sx={{ ml: 2 }}>
                  Gparts 데이터를 동기화하고 있습니다...
                </Typography>
              </Box>
            </Grid>
          )}
        </Grid>
      </TabPanel>

      <TabPanel value={selectedTab} index={1}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              동기화 이력
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>시간</TableCell>
                    <TableCell>상태</TableCell>
                    <TableCell align="right">처리</TableCell>
                    <TableCell align="right">성공</TableCell>
                    <TableCell align="right">실패</TableCell>
                    <TableCell align="right">소요시간</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {syncHistory.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography color="text.secondary">
                          동기화 이력이 없습니다.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    syncHistory.map((history) => (
                      <TableRow key={history.id}>
                        <TableCell>{new Date(history.timestamp).toLocaleString()}</TableCell>
                        <TableCell>
                          <Chip
                            label={history.status === 'success' ? '성공' : history.status === 'error' ? '실패' : '진행중'}
                            color={history.status === 'success' ? 'success' : history.status === 'error' ? 'error' : 'default'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell align="right">{history.totalProcessed}</TableCell>
                        <TableCell align="right">{history.successCount}</TableCell>
                        <TableCell align="right">{history.errorCount}</TableCell>
                        <TableCell align="right">{history.duration}ms</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      </TabPanel>

      <TabPanel value={selectedTab} index={2}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              API 연결 정보
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle2" gutterBottom>
                  기본 URL
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  https://api.ziparts.co.kr
                </Typography>
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle2" gutterBottom>
                  연결 상태
                </Typography>
                <Chip 
                  label={apiStatus === 'connected' ? '연결됨' : apiStatus === 'error' ? '연결 실패' : '확인 중'}
                  color={getStatusColor(apiStatus) as any}
                  size="small"
                />
              </Grid>
              <Grid item xs={12}>
                <Alert severity="info">
                <Typography variant="body2">
                  Gparts API 키와 시크릿 키는 환경 변수에서 설정됩니다.
                  <br />
                  GPARTS_API_KEY, GPARTS_SECRET_KEY 환경 변수를 확인해주세요.
                </Typography>
                </Alert>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </TabPanel>

      {/* 선택적 동기화 다이얼로그 */}
      <Dialog open={openSyncDialog} onClose={() => setOpenSyncDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>선택적 동기화</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>동기화 유형</InputLabel>
            <Select
              value={syncType}
              onChange={(e) => setSyncType(e.target.value as any)}
              label="동기화 유형"
            >
              <MenuItem value="manufacturer">제조사별</MenuItem>
              <MenuItem value="category">카테고리별</MenuItem>
            </Select>
          </FormControl>

          {syncType === 'manufacturer' && (
            <TextField
              fullWidth
              label="제조사 코드"
              value={manufacturerCode}
              onChange={(e) => setManufacturerCode(e.target.value)}
              placeholder="예: HYU, KIA, BMW"
              sx={{ mb: 2 }}
            />
          )}

          {syncType === 'category' && (
            <TextField
              fullWidth
              label="카테고리명"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
              placeholder="예: 엔진부품, 브레이크부품"
              sx={{ mb: 2 }}
            />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenSyncDialog(false)}>취소</Button>
          <Button 
            onClick={handleSelectiveSync} 
            variant="contained"
            disabled={loading}
          >
            동기화 시작
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default GpartsIntegration;
