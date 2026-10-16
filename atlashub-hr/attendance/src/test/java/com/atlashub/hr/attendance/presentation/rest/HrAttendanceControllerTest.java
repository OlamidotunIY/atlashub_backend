package com.atlashub.hr.attendance.presentation.rest;

import com.atlashub.hr.attendance.application.command.ClockIn.ClockInCommand;
import com.atlashub.hr.attendance.application.command.ClockIn.ClockInHandler;
import com.atlashub.hr.attendance.application.command.ClockOut.ClockOutCommand;
import com.atlashub.hr.attendance.application.command.ClockOut.ClockOutHandler;
import com.atlashub.hr.attendance.application.query.GetAttendanceSummary.AttendanceSummaryResult;
import com.atlashub.hr.attendance.application.query.GetAttendanceSummary.GetAttendanceSummaryHandler;
import com.atlashub.hr.attendance.application.query.GetAttendanceSummary.GetAttendanceSummaryQuery;
import com.atlashub.hr.attendance.presentation.dto.ClockInRequest;
import com.atlashub.hr.attendance.presentation.dto.ClockOutRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HrAttendanceControllerTest {

    private ClockInHandler clockInHandler;
    private ClockOutHandler clockOutHandler;
    private GetAttendanceSummaryHandler getAttendanceSummaryHandler;
    private HrAttendanceController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        clockInHandler = Mockito.mock(ClockInHandler.class);
        clockOutHandler = Mockito.mock(ClockOutHandler.class);
        getAttendanceSummaryHandler = Mockito.mock(GetAttendanceSummaryHandler.class);

        controller = new HrAttendanceController(clockInHandler, clockOutHandler, getAttendanceSummaryHandler);

        principal = new AuthenticatedPrincipal(
                100L,
                10L,
                "TEST",
                "session-1",
                "token-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("POST /attendance/clock-in should invoke ClockInHandler")
    void shouldClockIn() {
        ClockInRequest request = new ClockInRequest(100L, null);

        ResponseEntity<ApiResponse<Void>> response = controller.clockIn(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(clockInHandler).execute(new ClockInCommand(100L, 10L));
    }

    @Test
    @DisplayName("POST /attendance/clock-out should invoke ClockOutHandler")
    void shouldClockOut() {
        ClockOutRequest request = new ClockOutRequest(100L, null);

        ResponseEntity<ApiResponse<Void>> response = controller.clockOut(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(clockOutHandler).execute(new ClockOutCommand(100L, 10L));
    }

    @Test
    @DisplayName("GET /attendance/summary should invoke GetAttendanceSummaryHandler")
    void shouldGetAttendanceSummary() {
        AttendanceSummaryResult result = new AttendanceSummaryResult(
                31, 20, 2, 1,
                new BigDecimal("160.00"),
                BigDecimal.ZERO,
                List.of()
        );
        when(getAttendanceSummaryHandler.execute(any(GetAttendanceSummaryQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<AttendanceSummaryResult>> response =
                controller.getAttendanceSummary(null, 100L, 7, 2026, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().totalDays()).isEqualTo(31);
        verify(getAttendanceSummaryHandler).execute(new GetAttendanceSummaryQuery(10L, 100L, 7, 2026));
    }
}
