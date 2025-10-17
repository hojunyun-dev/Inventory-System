import React, { useState } from 'react';
import {
  Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider, Box, Typography,
  Collapse,
} from '@mui/material';
import {
  Dashboard as DashboardIcon, Inventory as InventoryIcon, ShoppingCart as ProductIcon,
  Assessment as ReportIcon, Settings as SettingsIcon, Email as EmailIcon,
  ExpandLess, ExpandMore, Add as AddIcon, List as ListIcon,
  Receipt as ReceiptIcon, DirectionsCar as CarIcon, QrCode2 as SerialIcon,
  Search as SearchIcon, Store as StoreIcon, Sync as SyncIcon,
  LocalShipping as ShippingIcon, Analytics as AnalyticsIcon, BarChart as BarChartIcon,
  Api as ApiIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [productMenuOpen, setProductMenuOpen] = useState(false);
  const [inventoryMenuOpen, setInventoryMenuOpen] = useState(false);
  const [orderMenuOpen, setOrderMenuOpen] = useState(false);
  const [channelMenuOpen, setChannelMenuOpen] = useState(false);
  const [advancedMenuOpen, setAdvancedMenuOpen] = useState(false);

  const handleNavigation = (path: string) => {
    navigate(path);
    onClose();
  };

  const handleProductMenuToggle = () => {
    setProductMenuOpen(!productMenuOpen);
  };

  const handleInventoryMenuToggle = () => {
    setInventoryMenuOpen(!inventoryMenuOpen);
  };

  const handleOrderMenuToggle = () => {
    setOrderMenuOpen(!orderMenuOpen);
  };

  const handleChannelMenuToggle = () => {
    setChannelMenuOpen(!channelMenuOpen);
  };

  const handleAdvancedMenuToggle = () => {
    setAdvancedMenuOpen(!advancedMenuOpen);
  };

  const isProductPath = location.pathname.startsWith('/products') || 
                        location.pathname.startsWith('/vehicle-search') || 
                        location.pathname.startsWith('/vehicle-compatibility');
  const isInventoryPath = location.pathname.startsWith('/inventory') || 
                          location.pathname.startsWith('/serialized-');
  const isOrderPath = location.pathname.startsWith('/orders');
  const isChannelPath = location.pathname.startsWith('/channels');

  return (
    <Drawer
      anchor="left"
      open={open}
      onClose={onClose}
      variant="temporary"
      sx={{
        width: 280,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 280,
          boxSizing: 'border-box',
        },
      }}
    >
      <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', bgcolor: 'primary.main', color: 'white' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
          <CarIcon sx={{ mr: 1 }} />
          <Typography variant="h6">자동차 부품</Typography>
        </Box>
        <Typography variant="caption">멀티채널 통합관리</Typography>
      </Box>
      <Divider />
      <List>
        {/* 메인 화면 */}
        <ListItem disablePadding>
          <ListItemButton
            onClick={() => handleNavigation('/')}
            selected={location.pathname === '/'}
          >
            <ListItemIcon><DashboardIcon /></ListItemIcon>
            <ListItemText primary="메인 화면" />
          </ListItemButton>
        </ListItem>

        <Divider sx={{ my: 1 }} />

        {/* 상품 관리 */}
        <ListItem disablePadding>
          <ListItemButton onClick={handleProductMenuToggle} selected={isProductPath}>
            <ListItemIcon><ProductIcon /></ListItemIcon>
            <ListItemText primary="상품 관리" />
            {productMenuOpen ? <ExpandLess /> : <ExpandMore />}
          </ListItemButton>
        </ListItem>
        <Collapse in={productMenuOpen} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/products')}
              selected={location.pathname === '/products'}
            >
              <ListItemIcon><ListIcon /></ListItemIcon>
              <ListItemText primary="상품 목록" />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/products/new')}
              selected={location.pathname === '/products/new'}
            >
              <ListItemIcon><AddIcon /></ListItemIcon>
              <ListItemText primary="신규 상품 등록" />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/vehicle-search')}
              selected={location.pathname === '/vehicle-search'}
            >
              <ListItemIcon><SearchIcon /></ListItemIcon>
              <ListItemText primary="차종별 검색" />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/vehicle-compatibility')}
              selected={location.pathname === '/vehicle-compatibility'}
            >
              <ListItemIcon><CarIcon /></ListItemIcon>
              <ListItemText primary="차량 호환성" />
            </ListItemButton>
          </List>
        </Collapse>

        {/* 재고 관리 */}
        <ListItem disablePadding>
          <ListItemButton onClick={handleInventoryMenuToggle} selected={isInventoryPath}>
            <ListItemIcon><InventoryIcon /></ListItemIcon>
            <ListItemText primary="재고 관리" />
            {inventoryMenuOpen ? <ExpandLess /> : <ExpandMore />}
          </ListItemButton>
        </ListItem>
        <Collapse in={inventoryMenuOpen} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/inventory')}
              selected={location.pathname === '/inventory'}
            >
              <ListItemIcon><ListIcon /></ListItemIcon>
              <ListItemText primary="통합 재고 현황" />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/serialized-inventory')}
              selected={location.pathname === '/serialized-inventory'}
            >
              <ListItemIcon><SerialIcon /></ListItemIcon>
              <ListItemText primary="시리얼 넘버 관리" />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/inventory/new')}
              selected={location.pathname === '/inventory/new'}
            >
              <ListItemIcon><AddIcon /></ListItemIcon>
              <ListItemText primary="재고 조정" />
            </ListItemButton>
          </List>
        </Collapse>

        {/* 채널 관리 */}
        <ListItem disablePadding>
          <ListItemButton onClick={handleChannelMenuToggle} selected={isChannelPath}>
            <ListItemIcon><StoreIcon /></ListItemIcon>
            <ListItemText 
              primary="채널 관리" 
              secondary="멀티채널 통합"
              secondaryTypographyProps={{ variant: 'caption' }}
            />
            {channelMenuOpen ? <ExpandLess /> : <ExpandMore />}
          </ListItemButton>
        </ListItem>
        <Collapse in={channelMenuOpen} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/channel-products')}
              selected={location.pathname === '/channel-products'}
            >
              <ListItemIcon><SyncIcon /></ListItemIcon>
              <ListItemText 
                primary="채널 통합 관리"
              />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/channel-products')}
            >
              <ListItemIcon>🥕</ListItemIcon>
              <ListItemText primary="당근마켓" secondary="수동 관리" secondaryTypographyProps={{ variant: 'caption' }} />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/channel-products')}
            >
              <ListItemIcon>⚡</ListItemIcon>
              <ListItemText primary="번개장터" secondary="수동 관리" secondaryTypographyProps={{ variant: 'caption' }} />
            </ListItemButton>
                  <ListItemButton 
                    sx={{ pl: 4 }} 
                    onClick={() => handleNavigation('/cafe24-integration')}
                    selected={location.pathname === '/cafe24-integration'}
                  >
                    <ListItemIcon>🛒</ListItemIcon>
                    <ListItemText primary="카페24" secondary="API 연동 완료" secondaryTypographyProps={{ variant: 'caption' }} />
                  </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/naver-integration')}
              selected={location.pathname === '/naver-integration'}
            >
              <ListItemIcon>🟢</ListItemIcon>
              <ListItemText primary="네이버 스토어" secondary="API 연동 완료" secondaryTypographyProps={{ variant: 'caption' }} />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/coupang-integration')}
              selected={location.pathname === '/coupang-integration'}
            >
              <ListItemIcon>🔵</ListItemIcon>
              <ListItemText primary="쿠팡" secondary="API 연동 완료" secondaryTypographyProps={{ variant: 'caption' }} />
            </ListItemButton>
          </List>
        </Collapse>

          {/* API 테스트 */}
          <ListItem disablePadding>
            <ListItemButton onClick={() => handleNavigation('/api-test')} selected={location.pathname === '/api-test'}>
              <ListItemIcon><ApiIcon /></ListItemIcon>
              <ListItemText primary="🧪 API 테스트" />
            </ListItemButton>
          </ListItem>


          {/* 고급 기능 */}
          <ListItem disablePadding>
            <ListItemButton onClick={handleAdvancedMenuToggle} selected={location.pathname.startsWith('/advanced')}>
              <ListItemIcon><AnalyticsIcon /></ListItemIcon>
              <ListItemText primary="📊 고급 기능" />
              {advancedMenuOpen ? <ExpandLess /> : <ExpandMore />}
            </ListItemButton>
          </ListItem>
        <Collapse in={advancedMenuOpen} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/advanced-dashboard')}
              selected={location.pathname === '/advanced-dashboard'}
            >
              <ListItemIcon><BarChartIcon /></ListItemIcon>
              <ListItemText primary="고급 대시보드" />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/advanced-search')}
              selected={location.pathname === '/advanced-search'}
            >
              <ListItemIcon><SearchIcon /></ListItemIcon>
              <ListItemText primary="고급 검색" />
            </ListItemButton>
          </List>
        </Collapse>

        {/* 주문 관리 */}
        <ListItem disablePadding>
          <ListItemButton onClick={handleOrderMenuToggle} selected={isOrderPath}>
            <ListItemIcon><ReceiptIcon /></ListItemIcon>
            <ListItemText primary="주문 관리" />
            {orderMenuOpen ? <ExpandLess /> : <ExpandMore />}
          </ListItemButton>
        </ListItem>
        <Collapse in={orderMenuOpen} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/orders')}
              selected={location.pathname === '/orders'}
            >
              <ListItemIcon><ListIcon /></ListItemIcon>
              <ListItemText primary="통합 주문 내역" secondary="모든 채널" secondaryTypographyProps={{ variant: 'caption' }} />
            </ListItemButton>
            <ListItemButton 
              sx={{ pl: 4 }} 
              onClick={() => handleNavigation('/orders/shipping')}
              selected={location.pathname === '/orders/shipping'}
            >
              <ListItemIcon><ShippingIcon /></ListItemIcon>
              <ListItemText primary="배송 관리" />
            </ListItemButton>
          </List>
        </Collapse>

        {/* 문의 관리 */}
        <ListItem disablePadding>
          <ListItemButton
            onClick={() => handleNavigation('/messages')}
            selected={location.pathname === '/messages'}
          >
            <ListItemIcon><EmailIcon /></ListItemIcon>
            <ListItemText primary="문의 관리" />
          </ListItemButton>
        </ListItem>
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon><ReportIcon /></ListItemIcon>
            <ListItemText primary="리포트" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon><SettingsIcon /></ListItemIcon>
            <ListItemText primary="설정" />
          </ListItemButton>
        </ListItem>
      </List>
    </Drawer>
  );
};

export default Sidebar;
