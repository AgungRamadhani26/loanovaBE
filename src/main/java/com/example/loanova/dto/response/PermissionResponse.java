package com.example.loanova.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private Integer id;
    private String permissionName;
    private String permissionDescription;
}
