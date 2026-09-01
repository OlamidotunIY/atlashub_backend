package com.atlashub.audit.adapter.in.web.controller;

import com.atlashub.audit.adapter.in.web.request.GetActivitiesRequestDto;
import com.atlashub.audit.application.query.ListOrgActivityQuery;
import com.atlashub.audit.application.result.ActivityLogDto;
import com.atlashub.audit.application.usecase.ListOrgActivityUseCase;
import com.atlashub.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/activity")
@Tag(name = "Audit", description = "Organization activity audit logs")
public class ActivityController {

    private final ListOrgActivityUseCase listOrgActivityUseCase;

    public ActivityController(ListOrgActivityUseCase listOrgActivityUseCase) {
        this.listOrgActivityUseCase = listOrgActivityUseCase;
    }

    @GetMapping
    @Operation(summary = "List Organization Activity")
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> listActivity(
            @ModelAttribute GetActivitiesRequestDto request) {
        
        List<ActivityLogDto> result = listOrgActivityUseCase.execute(
            new ListOrgActivityQuery(request.organizationId(), request.page(), request.size())
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Activity logs retrieved", result, null));
    }
}
