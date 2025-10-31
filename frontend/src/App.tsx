import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Box, CircularProgress } from '@mui/material';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ProductList from './pages/ProductList';
import ProductDetail from './pages/ProductDetail';
import ProductForm from './pages/ProductForm';
import InventoryList from './pages/InventoryList';
import InventoryForm from './pages/InventoryForm';
import MessageList from './pages/MessageList';
import OrderHistory from './pages/OrderHistory';
import ShippingLabel from './pages/ShippingLabel';
import VehiclePartSearch from './pages/VehiclePartSearch';
import SerializedInventoryList from './pages/SerializedInventoryList';
import VehicleCompatibilityList from './pages/VehicleCompatibilityList';
import ChannelProductManagement from './pages/ChannelProductManagement';
import AdvancedDashboard from './components/AdvancedDashboard';
import AdvancedSearch from './components/AdvancedSearch';
import Cafe24Integration from './pages/Cafe24Integration';
import NaverIntegration from './pages/NaverIntegration';
import CoupangIntegration from './pages/CoupangIntegration';
import GpartsIntegration from './pages/GpartsIntegration';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const AppContent: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const handleSidebarClose = () => {
    setSidebarOpen(false);
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {isAuthenticated && <Sidebar open={sidebarOpen} onClose={handleSidebarClose} />}
      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1, minHeight: '100vh' }}>
        {isAuthenticated && <Navbar onMenuClick={handleSidebarToggle} />}
        <Box component="main" sx={{ flexGrow: 1, p: isAuthenticated ? 3 : 0, mt: isAuthenticated ? 8 : 0 }}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
            
            {/* 상품 관리 라우트 */}
            <Route path="/products" element={<ProtectedRoute><ProductList /></ProtectedRoute>} />
            <Route path="/products/new" element={<ProtectedRoute><ProductForm /></ProtectedRoute>} />
            <Route path="/products/:id" element={<ProtectedRoute><ProductDetail /></ProtectedRoute>} />
            <Route path="/products/:id/edit" element={<ProtectedRoute><ProductForm /></ProtectedRoute>} />
            
            {/* 재고 관리 라우트 */}
            <Route path="/inventory" element={<ProtectedRoute><InventoryList /></ProtectedRoute>} />
            <Route path="/inventory/new" element={<ProtectedRoute><InventoryForm /></ProtectedRoute>} />
            <Route path="/inventory/:id/edit" element={<ProtectedRoute><InventoryForm /></ProtectedRoute>} />
            
            {/* 주문 관리 라우트 */}
            <Route path="/orders" element={<ProtectedRoute><OrderHistory /></ProtectedRoute>} />
            <Route path="/orders/shipping" element={<ProtectedRoute><ShippingLabel /></ProtectedRoute>} />
            <Route path="/orders/:id" element={<ProtectedRoute><div>주문 상세 페이지</div></ProtectedRoute>} />
            
            {/* 문의 관리 라우트 */}
            <Route path="/messages" element={<ProtectedRoute><MessageList /></ProtectedRoute>} />
            
            {/* 자동차 부품 특화 라우트 */}
            <Route path="/vehicle-search" element={<ProtectedRoute><VehiclePartSearch /></ProtectedRoute>} />
            <Route path="/serialized-inventory" element={<ProtectedRoute><SerializedInventoryList /></ProtectedRoute>} />
            <Route path="/vehicle-compatibility" element={<ProtectedRoute><VehicleCompatibilityList /></ProtectedRoute>} />
            
            {/* 채널 관리 라우트 */}
            <Route path="/channel-products" element={<ProtectedRoute><ChannelProductManagement /></ProtectedRoute>} />
            
                    {/* 고급 기능 라우트 */}
                    <Route path="/advanced-dashboard" element={<ProtectedRoute><AdvancedDashboard /></ProtectedRoute>} />
                    <Route path="/advanced-search" element={<ProtectedRoute><AdvancedSearch /></ProtectedRoute>} />
                    
                    {/* API 연동 라우트 */}
                    <Route path="/cafe24-integration" element={<ProtectedRoute><Cafe24Integration /></ProtectedRoute>} />
                    <Route path="/naver-integration" element={<ProtectedRoute><NaverIntegration /></ProtectedRoute>} />
                    <Route path="/coupang-integration" element={<ProtectedRoute><CoupangIntegration /></ProtectedRoute>} />
                    <Route path="/gparts-integration" element={<ProtectedRoute><GpartsIntegration /></ProtectedRoute>} />
                    
          </Routes>
        </Box>
      </Box>
    </Box>
  );
};

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <AppContent />
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;