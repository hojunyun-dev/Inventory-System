import React, { useState } from 'react';
import {
  Container, TextField, Button, Typography, Box, Alert, CircularProgress,
} from '@mui/material';
import {
  Login as LoginIcon, Inventory as InventoryIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Login: React.FC = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleInputChange = (field: string) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    setError('');
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!formData.username || !formData.password) {
      setError('사용자명과 비밀번호를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await login(formData.username, formData.password);
      navigate('/');
    } catch (err) {
      setError('로그인에 실패했습니다. 사용자명과 비밀번호를 확인해주세요.');
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <InventoryIcon sx={{ m: 1, color: 'primary.main', fontSize: 60 }} />
        <Typography component="h1" variant="h5">
          재고 관리 시스템 로그인
        </Typography>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="username"
            label="사용자명"
            name="username"
            autoComplete="username"
            autoFocus
            value={formData.username}
            onChange={handleInputChange('username')}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="비밀번호"
            type="password"
            id="password"
            autoComplete="current-password"
            value={formData.password}
            onChange={handleInputChange('password')}
          />
          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}
          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <LoginIcon />}
            disabled={loading}
          >
            {loading ? '로그인 중...' : '로그인'}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default Login;
