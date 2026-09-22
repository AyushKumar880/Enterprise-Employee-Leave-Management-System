package com.algoworks.leavemanagement.service;

import com.algoworks.leavemanagement.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    void sendNotification(String recipientEmail, String recipientName, String title, String message, String type);

    List<NotificationDTO> getRecentNotifications();

    NotificationDTO markAsRead(Long id);

    void markAllAsRead();

    long getUnreadCount();
}
