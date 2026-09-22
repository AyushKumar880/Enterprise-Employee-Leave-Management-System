package com.algoworks.leavemanagement.controller;

import com.algoworks.leavemanagement.dto.NotificationDTO;
import com.algoworks.leavemanagement.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET /api/notifications - Should return recent notifications")
    void testGetRecentNotifications() throws Exception {
        NotificationDTO notif = new NotificationDTO(
                1L, "hr@company.com", "HR Dept", "New Leave", "Leave submitted", "LEAVE_SUBMITTED", false, LocalDateTime.now()
        );

        when(notificationService.getRecentNotifications()).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("New Leave"));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read - Should mark as read")
    void testMarkAsRead() throws Exception {
        NotificationDTO notif = new NotificationDTO(
                1L, "hr@company.com", "HR Dept", "New Leave", "Leave submitted", "LEAVE_SUBMITTED", true, LocalDateTime.now()
        );

        when(notificationService.markAsRead(1L)).thenReturn(notif);

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }
}
