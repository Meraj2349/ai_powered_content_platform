package com.example.skillmateai.admin.controllers;

import com.example.skillmateai.admin.services.AdminService;
import com.example.skillmateai.admin.utilities.CreateAdminResponseUtil;
import com.example.skillmateai.admin.dtos.SearchUserRequest;
import com.example.skillmateai.user.utilities.MatchTextPatternUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerEmailValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CreateAdminResponseUtil createAdminResponseUtil;

    @MockBean
    private MatchTextPatternUtil matchTextPatternUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSearchUserByEmail_ValidEmail() throws Exception {
        // Arrange
        SearchUserRequest request = new SearchUserRequest();
        request.setEmail("test@example.com");
        
        when(matchTextPatternUtil.isValidEmail("test@example.com")).thenReturn(true);
        when(adminService.searchUserByEmail(anyString())).thenReturn(null);
        when(createAdminResponseUtil.basic(true, "No user found with the provided email"))
            .thenReturn(java.util.Map.of("success", true, "message", "No user found with the provided email"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchUserByEmail_InvalidEmailFormat() throws Exception {
        // Arrange
        SearchUserRequest request = new SearchUserRequest();
        request.setEmail("invalid-email");
        
        when(matchTextPatternUtil.isValidEmail("invalid-email")).thenReturn(false);
        when(createAdminResponseUtil.basic(false, "Invalid email format"))
            .thenReturn(java.util.Map.of("success", false, "message", "Invalid email format"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email format"));
    }

    @Test
    public void testSearchUserByEmail_EmptyEmail() throws Exception {
        // Arrange
        SearchUserRequest request = new SearchUserRequest();
        request.setEmail("");
        
        when(createAdminResponseUtil.basic(false, "Email is required"))
            .thenReturn(java.util.Map.of("success", false, "message", "Email is required"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/users/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is required"));
    }
}
