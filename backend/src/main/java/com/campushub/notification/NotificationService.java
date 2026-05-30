package com.campushub.notification;

import com.campushub.common.BusinessException;
import com.campushub.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Long receiverId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    public PageResult<Notification> getUserNotifications(Long userId, int pageNum, int pageSize, Boolean unreadOnly) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        var page = (unreadOnly != null && unreadOnly)
                ? notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreateTimeDesc(userId, pageRequest)
                : notificationRepository.findByReceiverIdOrderByCreateTimeDesc(userId, pageRequest);
        return PageResult.of(page);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作此通知");
        }
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadTime(java.time.LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }
}