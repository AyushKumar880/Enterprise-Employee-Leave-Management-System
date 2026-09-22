package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.NotificationDTO;
import com.algoworks.leavemanagement.entity.Notification;
import com.algoworks.leavemanagement.exception.ResourceNotFoundException;
import com.algoworks.leavemanagement.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void sendNotification(String recipientEmail, String recipientName, String title, String message, String type) {
        Notification notification = new Notification(null, recipientEmail, recipientName, title, message, type);
        notificationRepository.save(notification);
        log.info("[EMAIL SIMULATION] Sent to: {} <{}> | Subject: [{}] | Body: {}", recipientName, recipientEmail, title, message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications() {
        return notificationRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationDTO.fromEntity(saved);
    }

    @Override
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findAll()
                .stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }
}
