import React, { useState, useEffect } from 'react';
import {
  Container, Typography, Grid, Card, CardContent, Box, Button, 
  Paper, Chip, Alert, CircularProgress, Accordion, AccordionSummary,
  AccordionDetails, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Dialog, DialogTitle, DialogContent,
  DialogActions, Snackbar
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon, CheckCircle as CheckCircleIcon,
  Error as ErrorIcon, PlayArrow as PlayArrowIcon,
  Refresh as RefreshIcon, Storage as StorageIcon,
  Api as ApiIcon
} from '@mui/icons-material';

interface TestResult {
  name: string;
  status: 'success' | 'failed' | 'pending' | 'running';
  message: string;
  duration?: number;
  details?: any;
}

interface ChannelStatus {
  channel: string;
  icon: string;
  connected: boolean;
  message: string;
  lastCheck: string;
}

const APITestPage: React.FC = () => {
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [channelStatuses, setChannelStatuses] = useState<ChannelStatus[]>([]);
  const [loading, setLoading] = useState(false);
  const [overallStatus, setOverallStatus] = useState<'success' | 'failed' | 'pending'>('pending');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });
  const [testProductId, setTestProductId] = useState<number>(1);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [selectedTestDetails, setSelectedTestDetails] = useState<any>(null);

  useEffect(() => {
    checkAllChannelStatuses();
  }, []);

  const checkAllChannelStatuses = async () => {
    try {
      setLoading(true);
      const channels = [
        { name: 'cafe24', label: '카페24', icon: '🛒' },
        { name: 'naver', label: '네이버 스토어', icon: '🟢' },
        { name: 'coupang', label: '쿠팡', icon: '🔵' }
      ];

      const statuses: ChannelStatus[] = [];

      for (const channel of channels) {
        try {
          const response = await fetch(`/api/${channel.name}/status`);
          const data = await response.json();
          statuses.push({
            channel: channel.label,
            icon: channel.icon,
            connected: data.success && data.status === 'connected',
            message: data.message,
            lastCheck: data.lastCheck
          });
        } catch (error) {
          statuses.push({
            channel: channel.label,
            icon: channel.icon,
            connected: false,
            message: '연결 실패: ' + error,
            lastCheck: new Date().toISOString()
          });
        }
      }

      setChannelStatuses(statuses);
    } catch (error) {
      console.error('채널 상태 확인 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const runTest = async (testName: string, testFn: () => Promise<any>) => {
    const startTime = Date.now();
    
    setTestResults(prev => [
      ...prev.filter(r => r.name !== testName),
      { name: testName, status: 'running', message: '테스트 실행 중...' }
    ]);

    try {
      const result = await testFn();
      const duration = Date.now() - startTime;
      
      setTestResults(prev => [
        ...prev.filter(r => r.name !== testName),
        { 
          name: testName, 
          status: 'success', 
          message: '테스트 성공', 
          duration,
          details: result
        }
      ]);
      
      return { success: true, result };
    } catch (error: any) {
      const duration = Date.now() - startTime;
      
      setTestResults(prev => [
        ...prev.filter(r => r.name !== testName),
        { 
          name: testName, 
          status: 'failed', 
          message: error.message || '테스트 실패', 
          duration,
          details: error
        }
      ]);
      
      return { success: false, error };
    }
  };

  const runAllTests = async () => {
    setLoading(true);
    setTestResults([]);
    setOverallStatus('pending');

    const tests = [
      {
        name: '데이터베이스 연결',
        fn: async () => {
          const response = await fetch('/api/products');
          if (!response.ok) throw new Error('상품 조회 실패');
          const data = await response.json();
          return { productCount: data.length };
        }
      },
      {
        name: '카페24 API 연결',
        fn: async () => {
          const response = await fetch('/api/cafe24/status');
          if (!response.ok) throw new Error('API 호출 실패');
          const data = await response.json();
          if (!data.success) throw new Error(data.message);
          return data;
        }
      },
      {
        name: '네이버 스토어 API 연결',
        fn: async () => {
          const response = await fetch('/api/naver/status');
          if (!response.ok) throw new Error('API 호출 실패');
          const data = await response.json();
          if (!data.success) throw new Error(data.message);
          return data;
        }
      },
      {
        name: '쿠팡 API 연결',
        fn: async () => {
          const response = await fetch('/api/coupang/status');
          if (!response.ok) throw new Error('API 호출 실패');
          const data = await response.json();
          if (!data.success) throw new Error(data.message);
          return data;
        }
      },
      {
        name: '카페24 상품 목록 조회',
        fn: async () => {
          const response = await fetch('/api/cafe24/products');
          if (!response.ok) throw new Error('상품 목록 조회 실패');
          const data = await response.json();
          return { count: data.count, products: data.data };
        }
      },
      {
        name: '네이버 스토어 상품 목록 조회',
        fn: async () => {
          const response = await fetch('/api/naver/products');
          if (!response.ok) throw new Error('상품 목록 조회 실패');
          const data = await response.json();
          return { count: data.count, products: data.data };
        }
      },
      {
        name: '쿠팡 상품 목록 조회',
        fn: async () => {
          const response = await fetch('/api/coupang/products');
          if (!response.ok) throw new Error('상품 목록 조회 실패');
          const data = await response.json();
          return { count: data.count, products: data.data };
        }
      }
    ];

    let successCount = 0;
    for (const test of tests) {
      const result = await runTest(test.name, test.fn);
      if (result.success) successCount++;
      await new Promise(resolve => setTimeout(resolve, 500)); // 각 테스트 간 딜레이
    }

    setOverallStatus(successCount === tests.length ? 'success' : 'failed');
    setLoading(false);
    setSnackbar({
      open: true,
      message: `테스트 완료: ${successCount}/${tests.length} 성공`,
      severity: successCount === tests.length ? 'success' : 'error'
    });
  };

  const testProductRegistration = async (channel: string) => {
    await runTest(`${channel} 상품 등록 테스트`, async () => {
      const response = await fetch(`/api/${channel}/products/${testProductId}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: '테스트 상품',
          description: 'API 연동 테스트용 상품입니다.',
          price: 10000,
          brand: '테스트브랜드',
          origin: '한국',
          warranty: '1년'
        })
      });
      
      if (!response.ok) throw new Error('상품 등록 실패');
      const data = await response.json();
      if (!data.success) throw new Error(data.message);
      return data;
    });
  };

  const getStatusChip = (status: 'success' | 'failed' | 'pending' | 'running') => {
    switch (status) {
      case 'success':
        return <Chip icon={<CheckCircleIcon />} label="성공" color="success" size="small" />;
      case 'failed':
        return <Chip icon={<ErrorIcon />} label="실패" color="error" size="small" />;
      case 'running':
        return <Chip icon={<CircularProgress size={16} />} label="실행 중" color="primary" size="small" />;
      case 'pending':
        return <Chip label="대기" color="default" size="small" />;
    }
  };

  const showDetails = (test: TestResult) => {
    setSelectedTestDetails(test);
    setDetailsDialogOpen(true);
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          🧪 API 연동 테스트
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={checkAllChannelStatuses}
            disabled={loading}
          >
            상태 새로고침
          </Button>
          <Button
            variant="contained"
            startIcon={<PlayArrowIcon />}
            onClick={runAllTests}
            disabled={loading}
          >
            전체 테스트 실행
          </Button>
        </Box>
      </Box>

      {/* 전체 상태 */}
      {overallStatus !== 'pending' && (
        <Alert 
          severity={overallStatus === 'success' ? 'success' : 'error'} 
          sx={{ mb: 3 }}
        >
          {overallStatus === 'success' 
            ? '✅ 모든 테스트가 성공적으로 완료되었습니다!' 
            : '❌ 일부 테스트가 실패했습니다. 아래 결과를 확인해주세요.'}
        </Alert>
      )}

      {/* 채널 연결 상태 */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {channelStatuses.map((status, index) => (
          <Grid item xs={12} md={4} key={index}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                  <Typography variant="h6">
                    {status.icon} {status.channel}
                  </Typography>
                  <Chip
                    icon={status.connected ? <CheckCircleIcon /> : <ErrorIcon />}
                    label={status.connected ? '연결됨' : '연결 안됨'}
                    color={status.connected ? 'success' : 'error'}
                    size="small"
                  />
                </Box>
                <Typography variant="body2" color="textSecondary">
                  {status.message}
                </Typography>
                <Typography variant="caption" color="textSecondary">
                  마지막 확인: {status.lastCheck}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* 테스트 결과 */}
      <Accordion defaultExpanded>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="h6">
            📊 테스트 결과 ({testResults.filter(r => r.status === 'success').length}/{testResults.length})
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>테스트명</TableCell>
                  <TableCell>상태</TableCell>
                  <TableCell>메시지</TableCell>
                  <TableCell>소요 시간</TableCell>
                  <TableCell>작업</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {testResults.map((test, index) => (
                  <TableRow key={index}>
                    <TableCell>{test.name}</TableCell>
                    <TableCell>{getStatusChip(test.status)}</TableCell>
                    <TableCell>{test.message}</TableCell>
                    <TableCell>{test.duration ? `${test.duration}ms` : '-'}</TableCell>
                    <TableCell>
                      {test.details && (
                        <Button size="small" onClick={() => showDetails(test)}>
                          상세보기
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </AccordionDetails>
      </Accordion>

      {/* 수동 테스트 */}
      <Accordion sx={{ mt: 2 }}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography variant="h6">
            🔧 수동 테스트
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box sx={{ mb: 3 }}>
            <TextField
              label="테스트할 상품 ID"
              type="number"
              value={testProductId}
              onChange={(e) => setTestProductId(Number(e.target.value))}
              size="small"
              sx={{ mb: 2 }}
            />
            <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
              상품 ID를 입력하고 아래 버튼을 클릭하여 각 채널에 상품 등록을 테스트할 수 있습니다.
            </Typography>
          </Box>

          <Grid container spacing={2}>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                onClick={() => testProductRegistration('cafe24')}
                disabled={loading}
              >
                🛒 카페24 상품 등록 테스트
              </Button>
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                onClick={() => testProductRegistration('naver')}
                disabled={loading}
              >
                🟢 네이버 상품 등록 테스트
              </Button>
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                onClick={() => testProductRegistration('coupang')}
                disabled={loading}
              >
                🔵 쿠팡 상품 등록 테스트
              </Button>
            </Grid>
          </Grid>
        </AccordionDetails>
      </Accordion>

      {/* 시스템 정보 */}
      <Card sx={{ mt: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            <StorageIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            시스템 정보
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <Typography variant="body2">
                <strong>백엔드 URL:</strong> http://localhost:8080
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2">
                <strong>프론트엔드 URL:</strong> http://localhost:3000
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2">
                <strong>데이터베이스:</strong> H2 (파일 기반)
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Typography variant="body2">
                <strong>테스트 시간:</strong> {new Date().toLocaleString()}
              </Typography>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* 상세 정보 다이얼로그 */}
      <Dialog 
        open={detailsDialogOpen} 
        onClose={() => setDetailsDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>테스트 상세 정보</DialogTitle>
        <DialogContent>
          {selectedTestDetails && (
            <Box component="pre" sx={{ 
              bgcolor: 'grey.100', 
              p: 2, 
              borderRadius: 1, 
              overflow: 'auto',
              fontSize: '0.875rem'
            }}>
              {JSON.stringify(selectedTestDetails.details, null, 2)}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>닫기</Button>
        </DialogActions>
      </Dialog>

      {/* 스낵바 */}
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

export default APITestPage;
