import React, { useState, useEffect } from 'react';
import {
  AppBar, Toolbar, Typography, Button, Box, IconButton, Badge, Popover, List,
  ListItem, ListItemText, ListItemIcon, Divider, Alert,
} from '@mui/material';
import {
  Logout as LogoutIcon, Menu as MenuIcon, Notifications as NotificationsIcon,
  Warning as WarningIcon, Error as ErrorIcon, CheckCircle as CheckCircleIcon,
  Message as MessageIcon, Inventory as InventoryIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { inventoryApi, messageApi } from '../services/api';

interface NavbarProps {
  onMenuClick: () => void;
}

interface NotificationItem {
  id: string;
  type: 'message' | 'inventory' | 'info';
  title: string;
  message: string;
  priority: 'high' | 'medium' | 'low';
  onClick?: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ onMenuClick }) => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [notificationAnchor, setNotificationAnchor] = useState<null | HTMLElement>(null);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    fetchNotifications();
    // 30초마다 알림 갱신
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchNotifications = async () => {
    try {
      const notifs: NotificationItem[] = [];

      // 재고 부족 알림
      try {
        const inventoryResponse = await inventoryApi.getAll();
        const inventories = inventoryResponse.data;
        const lowStockCount = inventories.filter(inv => inv.status === 'LOW_STOCK').length;
        const outOfStockCount = inventories.filter(inv => inv.status === 'OUT_OF_STOCK').length;

        if (lowStockCount > 0) {
          notifs.push({
            id: 'low-stock',
            type: 'inventory',
            title: `재고 부족 ${lowStockCount}건`,
            message: '재발주가 필요합니다',
            priority: 'high',
            onClick: () => {
              navigate('/inventory');
              setNotificationAnchor(null);
            }
          });
        }

        if (outOfStockCount > 0) {
          notifs.push({
            id: 'out-of-stock',
            type: 'inventory',
            title: `품절 ${outOfStockCount}건`,
            message: '즉시 재입고가 필요합니다',
            priority: 'high',
            onClick: () => {
              navigate('/inventory');
              setNotificationAnchor(null);
            }
          });
        }
      } catch (error) {
        console.error('재고 데이터 로드 실패:', error);
      }

      // 새 메시지 알림
      try {
        const messageResponse = await messageApi.getNewCount();
        const newMessageCount = messageResponse.data.count;

        if (newMessageCount > 0) {
          notifs.push({
            id: 'new-messages',
            type: 'message',
            title: `새 메시지 ${newMessageCount}건`,
            message: '고객 문의가 있습니다',
            priority: 'medium',
            onClick: () => {
              navigate('/messages');
              setNotificationAnchor(null);
            }
          });
        }
      } catch (error) {
        console.error('메시지 데이터 로드 실패:', error);
      }

      setNotifications(notifs);
      setUnreadCount(notifs.length);
    } catch (error) {
      console.error('알림 로드 실패:', error);
    }
  };

  const handleNotificationClick = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationAnchor(event.currentTarget);
  };

  const handleNotificationClose = () => {
    setNotificationAnchor(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'inventory':
        return <InventoryIcon />;
      case 'message':
        return <MessageIcon />;
      default:
        return <CheckCircleIcon />;
    }
  };

  const getNotificationColor = (priority: string) => {
    switch (priority) {
      case 'high':
        return 'error';
      case 'medium':
        return 'warning';
      default:
        return 'info';
    }
  };

  return (
    <>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            onClick={onMenuClick}
            edge="start"
            sx={{ mr: 2 }}
          >
            <MenuIcon />
          </IconButton>

          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            재고 관리 시스템
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            {/* 알림 아이콘 */}
            <IconButton
              color="inherit"
              onClick={handleNotificationClick}
            >
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>

            {/* 사용자 정보 */}
            <Typography variant="body2" sx={{ opacity: 0.8, ml: 1 }}>
              {user?.username}님
            </Typography>

            {/* 로그아웃 버튼 */}
            <Button
              color="inherit"
              startIcon={<LogoutIcon />}
              onClick={handleLogout}
              sx={{
                border: '1px solid rgba(255,255,255,0.3)',
                borderRadius: 1,
                px: 2,
                ml: 1,
              }}
            >
              로그아웃
            </Button>
          </Box>
        </Toolbar>
      </AppBar>

      {/* 알림 팝업 */}
      <Popover
        open={Boolean(notificationAnchor)}
        anchorEl={notificationAnchor}
        onClose={handleNotificationClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        <Box sx={{ width: 350, maxHeight: 400 }}>
          <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="h6">알림</Typography>
          </Box>
          
          <Box sx={{ maxHeight: 300, overflowY: 'auto' }}>
            {notifications.length === 0 ? (
              <Box sx={{ p: 2, textAlign: 'center', color: 'text.secondary' }}>
                알림이 없습니다.
              </Box>
            ) : (
              <List>
                {notifications.map((notification) => (
                  <ListItem
                    key={notification.id}
                    button
                    onClick={notification.onClick}
                    sx={{
                      borderLeft: '3px solid',
                      borderLeftColor: getNotificationColor(notification.priority) + '.main',
                    }}
                  >
                    <ListItemIcon>
                      {getNotificationIcon(notification.type)}
                    </ListItemIcon>
                    <ListItemText
                      primary={notification.title}
                      secondary={notification.message}
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </Box>
        </Box>
      </Popover>
    </>
  );
};

export default Navbar;