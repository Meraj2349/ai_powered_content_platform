package com.example.skillmateai.admin.dtos;

import lombok.Data;

@Data
public class ManageAdminPrivilegeRequest {
    private String userId;
    private String action; // "GRANT" or "REVOKE"
}
