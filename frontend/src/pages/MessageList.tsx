import React, { useState, useEffect, useCallback } from 'react';
import {
  Container, Paper, Typography, Box, Chip, TextField, MenuItem, IconButton, Alert,
  List, ListItem, Divider, CircularProgress, Button,
} from '@mui/material';
import {
  Delete as DeleteIcon, Send as SendIcon,
} from '@mui/icons-material';
import { messageApi } from '../services/api';
import { Message, MessageStatus, MessageCategory } from '../types';

const MessageList: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [selectedMessage, setSelectedMessage] = useState<Message | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [filterCategory, setFilterCategory] = useState<string>('ALL');
  const [replyContent, setReplyContent] = useState('');

  // 카테고리를 한국어로 변환하는 함수
  const getCategoryDisplayName = (category: MessageCategory): string => {
    const categoryMap: Record<MessageCategory, string> = {
      [MessageCategory.PRODUCT_INQUIRY]: '상품 문의',
      [MessageCategory.ORDER_INQUIRY]: '주문 문의',
      [MessageCategory.INVENTORY_INQUIRY]: '재고 문의',
      [MessageCategory.TECHNICAL_SUPPORT]: '기술 지원',
      [MessageCategory.SUGGESTION]: '건의',
      [MessageCategory.LOW_STOCK]: '재고 부족',
      [MessageCategory.OUT_OF_STOCK]: '품절',
      [MessageCategory.ORDER]: '주문',
      [MessageCategory.DELIVERY]: '배송',
      [MessageCategory.URGENT]: '긴급',
      [MessageCategory.ETC]: '기타',
    };
    return categoryMap[category] || category;
  };

  // 상태를 한국어로 변환하는 함수
  const getStatusDisplayName = (status: MessageStatus): string => {
    const statusMap: Record<MessageStatus, string> = {
      [MessageStatus.NEW]: '신규',
      [MessageStatus.IN_PROGRESS]: '진행 중',
      [MessageStatus.REPLIED]: '답변 완료',
      [MessageStatus.CLOSED]: '완료',
      [MessageStatus.READ]: '읽음',
      [MessageStatus.UNREAD]: '미읽음',
    };
    return statusMap[status] || status;
  };

  const fetchMessages = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const response = await messageApi.getAll();
      let filteredMessages = response.data;

      if (filterStatus !== 'ALL') {
        filteredMessages = filteredMessages.filter(msg => msg.status === filterStatus);
      }
      if (filterCategory !== 'ALL') {
        filteredMessages = filteredMessages.filter(msg => msg.category === filterCategory);
      }
      setMessages(filteredMessages);
    } catch (err) {
      setError('메시지를 불러오는 중 오류가 발생했습니다.');
      console.error('Failed to fetch messages:', err);
    } finally {
      setLoading(false);
    }
  }, [filterStatus, filterCategory]);

  useEffect(() => {
    fetchMessages();
  }, [fetchMessages]);

  const handleMessageClick = (message: Message) => {
    setSelectedMessage(message);
    setReplyContent(message.reply || '');
  };

  const handleDeleteMessage = async (id: number | undefined) => {
    if (id === undefined) return;
    if (!window.confirm('정말로 이 메시지를 삭제하시겠습니까?')) return;

    try {
      await messageApi.delete(id);
      alert('메시지가 삭제되었습니다.');
      setSelectedMessage(null);
      fetchMessages();
    } catch (err) {
      setError('메시지 삭제 중 오류가 발생했습니다.');
      console.error('Failed to delete message:', err);
    }
  };

  const handleReplySubmit = async () => {
    if (!selectedMessage || !replyContent.trim()) return;

    try {
      await messageApi.reply(selectedMessage.id!, replyContent, 'admin');
      await messageApi.updateStatus(selectedMessage.id!, MessageStatus.REPLIED);
      alert('답변이 전송되었습니다.');
      fetchMessages();
    } catch (err) {
      setError('답변 전송 중 오류가 발생했습니다.');
      console.error('Failed to send reply:', err);
    }
  };

  const getStatusColor = (status: MessageStatus) => {
    switch (status) {
      case MessageStatus.NEW: return 'error';
      case MessageStatus.IN_PROGRESS: return 'warning';
      case MessageStatus.REPLIED: return 'success';
      case MessageStatus.CLOSED: return 'default';
      default: return 'default';
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        문의 관리
      </Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Box sx={{ display: 'flex', gap: 3, height: 'calc(100vh - 180px)' }}>
        {/* Left Panel: Message List (1/3 width) */}
        <Paper sx={{ flex: '1 1 30%', p: 2, overflowY: 'auto' }}>
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <TextField
              select
              label="상태 필터"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              size="small"
              sx={{ minWidth: 120 }}
            >
              <MenuItem value="ALL">전체</MenuItem>
              {Object.values(MessageStatus).map((status) => (
                <MenuItem key={status} value={status}>{getStatusDisplayName(status)}</MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="카테고리 필터"
              value={filterCategory}
              onChange={(e) => setFilterCategory(e.target.value)}
              size="small"
              sx={{ minWidth: 120 }}
            >
              <MenuItem value="ALL">전체</MenuItem>
              {Object.values(MessageCategory).map((category) => (
                <MenuItem key={category} value={category}>{getCategoryDisplayName(category)}</MenuItem>
              ))}
            </TextField>
          </Box>
          <Divider sx={{ mb: 2 }} />
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <List>
              {messages.length === 0 ? (
                <Typography variant="body2" color="textSecondary" sx={{ textAlign: 'center', mt: 4 }}>
                  표시할 메시지가 없습니다.
                </Typography>
              ) : (
                messages.map((message) => (
                  <ListItem
                    key={message.id}
                    button
                    onClick={() => handleMessageClick(message)}
                    selected={selectedMessage?.id === message.id}
                    sx={{
                      borderBottom: '1px solid #eee',
                      '&:last-child': { borderBottom: 'none' },
                      flexDirection: 'column',
                      alignItems: 'flex-start',
                    }}
                  >
                    <Box sx={{ width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                        {message.subject}
                      </Typography>
                      <Chip label={getStatusDisplayName(message.status)} color={getStatusColor(message.status)} size="small" />
                    </Box>
                    <Typography variant="body2" color="textSecondary" sx={{ width: '100%', mt: 0.5 }}>
                      {message.senderName} ({message.senderEmail})
                    </Typography>
                    <Typography variant="caption" color="textSecondary" sx={{ width: '100%', mt: 0.5 }}>
                      {new Date(message.createdAt!).toLocaleString()}
                    </Typography>
                  </ListItem>
                ))
              )}
            </List>
          )}
        </Paper>

        {/* Right Panel: Message Detail (2/3 width) */}
        <Paper sx={{ flex: '1 1 70%', p: 3, overflowY: 'auto' }}>
          {selectedMessage ? (
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" component="h2" sx={{ fontWeight: 'bold' }}>
                  {selectedMessage.subject}
                </Typography>
                <Box>
                  <Chip label={getStatusDisplayName(selectedMessage.status)} color={getStatusColor(selectedMessage.status)} sx={{ mr: 1 }} />
                  <Chip label={getCategoryDisplayName(selectedMessage.category)} size="small" />
                  <IconButton color="error" onClick={() => handleDeleteMessage(selectedMessage.id)}>
                    <DeleteIcon />
                  </IconButton>
                </Box>
              </Box>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="subtitle1" gutterBottom>
                **발신자:** {selectedMessage.senderName} ({selectedMessage.senderEmail})
                {selectedMessage.senderPhone && ` | ${selectedMessage.senderPhone}`}
              </Typography>
              <Typography variant="body2" color="textSecondary" gutterBottom>
                **수신일:** {new Date(selectedMessage.createdAt!).toLocaleString()}
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Typography variant="h6" gutterBottom>
                문의 내용:
              </Typography>
              <Typography variant="body1" paragraph sx={{ whiteSpace: 'pre-wrap' }}>
                {selectedMessage.content}
              </Typography>

              {selectedMessage.reply && (
                <Box sx={{ mt: 4, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                  <Typography variant="h6" gutterBottom>
                    답변:
                  </Typography>
                  <Typography variant="body1" paragraph sx={{ whiteSpace: 'pre-wrap' }}>
                    {selectedMessage.reply}
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    답변일: {new Date(selectedMessage.repliedAt!).toLocaleString()} (by {selectedMessage.repliedBy})
                  </Typography>
                </Box>
              )}

              {selectedMessage.status !== MessageStatus.REPLIED && selectedMessage.status !== MessageStatus.CLOSED && (
                <Box sx={{ mt: 4 }}>
                  <Typography variant="h6" gutterBottom>
                    답변 작성:
                  </Typography>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    variant="outlined"
                    placeholder="답변 내용을 입력하세요..."
                    value={replyContent}
                    onChange={(e) => setReplyContent(e.target.value)}
                    sx={{ mb: 2 }}
                  />
                  <Button
                    variant="contained"
                    startIcon={<SendIcon />}
                    onClick={handleReplySubmit}
                    disabled={!replyContent.trim()}
                  >
                    답변 전송
                  </Button>
                </Box>
              )}
            </Box>
          ) : (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
              <Typography variant="h6" color="textSecondary">
                좌측에서 메시지를 선택해주세요.
              </Typography>
            </Box>
          )}
        </Paper>
      </Box>
    </Container>
  );
};

export default MessageList;
