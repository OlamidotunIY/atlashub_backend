package com.atlashub.hr.staff.presentation.rest;

import com.atlashub.hr.staff.application.command.AddEmployeeAllowance.AddEmployeeAllowanceHandler;
import com.atlashub.hr.staff.application.command.AddEmployeeBank.AddEmployeeBankCommand;
import com.atlashub.hr.staff.application.command.AddEmployeeBank.AddEmployeeBankHandler;
import com.atlashub.hr.staff.application.command.AddEmployeeBank.AddEmployeeBankResponse;
import com.atlashub.hr.staff.application.command.AddEmployeeDeduction.AddEmployeeDeductionHandler;
import com.atlashub.hr.staff.application.command.AddGradeAllowance.AddGradeAllowanceHandler;
import com.atlashub.hr.staff.application.command.AddGradeDeduction.AddGradeDeductionHandler;
import com.atlashub.hr.staff.application.command.CreateSalaryGrade.CreateSalaryGradeCommand;
import com.atlashub.hr.staff.application.command.CreateSalaryGrade.CreateSalaryGradeHandler;
import com.atlashub.hr.staff.application.command.CreateSalaryGrade.CreateSalaryGradeResponse;
import com.atlashub.hr.staff.application.command.LogInfraction.LogInfractionCommand;
import com.atlashub.hr.staff.application.command.LogInfraction.LogInfractionHandler;
import com.atlashub.hr.staff.application.command.LogInfraction.LogInfractionResponse;
import com.atlashub.hr.staff.application.command.OnboardEmployee.OnboardEmployeeCommand;
import com.atlashub.hr.staff.application.command.OnboardEmployee.OnboardEmployeeHandler;
import com.atlashub.hr.staff.application.command.OnboardEmployee.OnboardEmployeeResponse;
import com.atlashub.hr.staff.application.command.PromoteEmployee.PromoteEmployeeCommand;
import com.atlashub.hr.staff.application.command.PromoteEmployee.PromoteEmployeeHandler;
import com.atlashub.hr.staff.application.command.ReinstateEmployee.ReinstateEmployeeCommand;
import com.atlashub.hr.staff.application.command.ReinstateEmployee.ReinstateEmployeeHandler;
import com.atlashub.hr.staff.application.command.ResolveInfraction.ResolveInfractionHandler;
import com.atlashub.hr.staff.application.command.SuspendEmployee.SuspendEmployeeCommand;
import com.atlashub.hr.staff.application.command.SuspendEmployee.SuspendEmployeeHandler;
import com.atlashub.hr.staff.application.command.TerminateEmployee.TerminateEmployeeCommand;
import com.atlashub.hr.staff.application.command.TerminateEmployee.TerminateEmployeeHandler;
import com.atlashub.hr.staff.application.command.UpdateEmployeeProfile.UpdateEmployeeProfileCommand;
import com.atlashub.hr.staff.application.command.UpdateEmployeeProfile.UpdateEmployeeProfileHandler;
import com.atlashub.hr.staff.application.query.GetEmployeeDetails.EmployeeDetailsResult;
import com.atlashub.hr.staff.application.query.GetEmployeeDetails.GetEmployeeDetailsHandler;
import com.atlashub.hr.staff.application.query.GetEmployeeDetails.GetEmployeeDetailsQuery;
import com.atlashub.hr.staff.application.query.ListEmployees.EmployeeResult;
import com.atlashub.hr.staff.application.query.ListEmployees.ListEmployeesHandler;
import com.atlashub.hr.staff.application.query.ListEmployees.ListEmployeesQuery;
import com.atlashub.hr.staff.application.query.ListSalaryGrades.ListSalaryGradesHandler;
import com.atlashub.hr.staff.application.query.ListSalaryGrades.ListSalaryGradesQuery;
import com.atlashub.hr.staff.application.query.ListSalaryGrades.SalaryGradeResult;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import com.atlashub.hr.staff.presentation.dto.AddEmployeeBankRequest;
import com.atlashub.hr.staff.presentation.dto.CreateSalaryGradeRequest;
import com.atlashub.hr.staff.presentation.dto.LogInfractionRequest;
import com.atlashub.hr.staff.presentation.dto.OnboardEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.PromoteEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.SuspendEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.TerminateEmployeeRequest;
import com.atlashub.hr.staff.presentation.dto.UpdateEmployeeProfileRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HrStaffControllerTest {

    private OnboardEmployeeHandler onboardEmployeeHandler;
    private UpdateEmployeeProfileHandler updateEmployeeProfileHandler;
    private PromoteEmployeeHandler promoteEmployeeHandler;
    private SuspendEmployeeHandler suspendEmployeeHandler;
    private ReinstateEmployeeHandler reinstateEmployeeHandler;
    private TerminateEmployeeHandler terminateEmployeeHandler;
    private AddEmployeeBankHandler addEmployeeBankHandler;
    private AddEmployeeAllowanceHandler addEmployeeAllowanceHandler;
    private AddEmployeeDeductionHandler addEmployeeDeductionHandler;
    private LogInfractionHandler logInfractionHandler;
    private ResolveInfractionHandler resolveInfractionHandler;
    private CreateSalaryGradeHandler createSalaryGradeHandler;
    private AddGradeAllowanceHandler addGradeAllowanceHandler;
    private AddGradeDeductionHandler addGradeDeductionHandler;
    private ListEmployeesHandler listEmployeesHandler;
    private GetEmployeeDetailsHandler getEmployeeDetailsHandler;
    private ListSalaryGradesHandler listSalaryGradesHandler;

    private HrStaffController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        onboardEmployeeHandler = mock(OnboardEmployeeHandler.class);
        updateEmployeeProfileHandler = mock(UpdateEmployeeProfileHandler.class);
        promoteEmployeeHandler = mock(PromoteEmployeeHandler.class);
        suspendEmployeeHandler = mock(SuspendEmployeeHandler.class);
        reinstateEmployeeHandler = mock(ReinstateEmployeeHandler.class);
        terminateEmployeeHandler = mock(TerminateEmployeeHandler.class);
        addEmployeeBankHandler = mock(AddEmployeeBankHandler.class);
        addEmployeeAllowanceHandler = mock(AddEmployeeAllowanceHandler.class);
        addEmployeeDeductionHandler = mock(AddEmployeeDeductionHandler.class);
        logInfractionHandler = mock(LogInfractionHandler.class);
        resolveInfractionHandler = mock(ResolveInfractionHandler.class);
        createSalaryGradeHandler = mock(CreateSalaryGradeHandler.class);
        addGradeAllowanceHandler = mock(AddGradeAllowanceHandler.class);
        addGradeDeductionHandler = mock(AddGradeDeductionHandler.class);
        listEmployeesHandler = mock(ListEmployeesHandler.class);
        getEmployeeDetailsHandler = mock(GetEmployeeDetailsHandler.class);
        listSalaryGradesHandler = mock(ListSalaryGradesHandler.class);

        controller = new HrStaffController(
                onboardEmployeeHandler,
                updateEmployeeProfileHandler,
                promoteEmployeeHandler,
                suspendEmployeeHandler,
                reinstateEmployeeHandler,
                terminateEmployeeHandler,
                addEmployeeBankHandler,
                addEmployeeAllowanceHandler,
                addEmployeeDeductionHandler,
                logInfractionHandler,
                resolveInfractionHandler,
                createSalaryGradeHandler,
                addGradeAllowanceHandler,
                addGradeDeductionHandler,
                listEmployeesHandler,
                getEmployeeDetailsHandler,
                listSalaryGradesHandler
        );

        principal = mock(AuthenticatedPrincipal.class);
        when(principal.activeOrganizationId()).thenReturn(10L);
        when(principal.userId()).thenReturn(100L);
    }

    @Test
    @DisplayName("Should onboard employee and return 201 Created")
    void shouldOnboardEmployee() {
        OnboardEmployeeRequest request = new OnboardEmployeeRequest(
                10L,
                100L,
                "Jane",
                "Smith",
                "jane@atlashub.com",
                "+2348011112222",
                LocalDate.of(1992, 5, 20),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 2, 1),
                "Product Manager",
                "Product",
                null,
                1L,
                new BigDecimal("900000.00"),
                "NGN"
        );

        when(onboardEmployeeHandler.execute(any(OnboardEmployeeCommand.class)))
                .thenReturn(new OnboardEmployeeResponse(42L, "EMP-0042"));

        ResponseEntity<ApiResponse<OnboardEmployeeResponse>> response = controller.onboardEmployee(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(42L, response.getBody().data().employeeId());
        assertEquals("EMP-0042", response.getBody().data().employeeNumber());
    }

    @Test
    @DisplayName("Should list employees with pagination")
    void shouldListEmployees() {
        EmployeeResult employeeResult = new EmployeeResult(
                1L,
                "EMP-0001",
                "John",
                "Doe",
                "john@atlashub.com",
                "+2348012345678",
                "Software Engineer",
                "Engineering",
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 1)
        );

        PageResult<EmployeeResult> pageResult = new PageResult<>(
                List.of(employeeResult), 0, 20, 1, 1
        );

        when(listEmployeesHandler.execute(any(ListEmployeesQuery.class))).thenReturn(pageResult);

        ResponseEntity<ApiResponse<?>> response = controller.getEmployees(null, null, null, 0, 20, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        PageResult<EmployeeResult> data = (PageResult<EmployeeResult>) response.getBody().data();
        assertEquals(1, data.content().size());
        assertEquals("EMP-0001", data.content().getFirst().employeeNumber());
    }

    @Test
    @DisplayName("Should get single employee details when id param is passed")
    void shouldGetEmployeeDetails() {
        EmployeeDetailsResult details = new EmployeeDetailsResult(
                1L,
                10L,
                100L,
                "EMP-0001",
                "John",
                "Doe",
                "john@atlashub.com",
                "+2348012345678",
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 1),
                null,
                "Software Engineer",
                "Engineering",
                null,
                1L,
                new BigDecimal("600000.00"),
                "NGN",
                EmployeeStatus.ACTIVE,
                List.of(),
                List.of(),
                List.of()
        );

        when(getEmployeeDetailsHandler.execute(any(GetEmployeeDetailsQuery.class))).thenReturn(details);

        ResponseEntity<ApiResponse<?>> response = controller.getEmployees(1L, null, null, 0, 20, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        EmployeeDetailsResult data = (EmployeeDetailsResult) response.getBody().data();
        assertEquals(1L, data.id());
        assertEquals("EMP-0001", data.employeeNumber());
    }

    @Test
    @DisplayName("Should update employee profile")
    void shouldUpdateProfile() {
        UpdateEmployeeProfileRequest request = new UpdateEmployeeProfileRequest(
                "Johnny",
                "Doe",
                "+2348099999999",
                "New Address",
                "Nigerian"
        );

        ResponseEntity<ApiResponse<Void>> response = controller.updateEmployeeProfile(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateEmployeeProfileHandler).execute(any(UpdateEmployeeProfileCommand.class));
    }

    @Test
    @DisplayName("Should promote employee")
    void shouldPromoteEmployee() {
        PromoteEmployeeRequest request = new PromoteEmployeeRequest(
                "Lead Dev",
                2L,
                new BigDecimal("800000.00"),
                "NGN"
        );

        ResponseEntity<ApiResponse<Void>> response = controller.promoteEmployee(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(promoteEmployeeHandler).execute(any(PromoteEmployeeCommand.class));
    }

    @Test
    @DisplayName("Should suspend, reinstate, and terminate employee")
    void shouldManageEmployeeLifecycle() {
        SuspendEmployeeRequest suspendReq = new SuspendEmployeeRequest("Investigation");
        ResponseEntity<ApiResponse<Void>> suspendResp = controller.suspendEmployee(1L, suspendReq);
        assertEquals(HttpStatus.OK, suspendResp.getStatusCode());
        verify(suspendEmployeeHandler).execute(any(SuspendEmployeeCommand.class));

        ResponseEntity<ApiResponse<Void>> reinstateResp = controller.reinstateEmployee(1L);
        assertEquals(HttpStatus.OK, reinstateResp.getStatusCode());
        verify(reinstateEmployeeHandler).execute(any(ReinstateEmployeeCommand.class));

        TerminateEmployeeRequest termReq = new TerminateEmployeeRequest(LocalDate.now());
        ResponseEntity<ApiResponse<Void>> termResp = controller.terminateEmployee(1L, termReq);
        assertEquals(HttpStatus.OK, termResp.getStatusCode());
        verify(terminateEmployeeHandler).execute(any(TerminateEmployeeCommand.class));
    }

    @Test
    @DisplayName("Should add employee bank account")
    void shouldAddEmployeeBank() {
        AddEmployeeBankRequest request = new AddEmployeeBankRequest(
                "058",
                "GTB",
                "0123456789",
                true
        );

        when(addEmployeeBankHandler.execute(any(AddEmployeeBankCommand.class)))
                .thenReturn(new AddEmployeeBankResponse(5L, "JOHN DOE"));

        ResponseEntity<ApiResponse<AddEmployeeBankResponse>> response = controller.addEmployeeBank(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().data().bankId());
        assertEquals("JOHN DOE", response.getBody().data().accountName());
    }

    @Test
    @DisplayName("Should log infraction")
    void shouldLogInfraction() {
        LogInfractionRequest request = new LogInfractionRequest(
                "Late submission",
                InfractionSeverity.LOW
        );

        when(logInfractionHandler.execute(any(LogInfractionCommand.class)))
                .thenReturn(new LogInfractionResponse(88L));

        ResponseEntity<ApiResponse<LogInfractionResponse>> response = controller.logInfraction(1L, request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(88L, response.getBody().data().infractionId());
    }

    @Test
    @DisplayName("Should create and list salary grades")
    void shouldCreateAndListSalaryGrades() {
        CreateSalaryGradeRequest request = new CreateSalaryGradeRequest(
                10L,
                "Grade L6",
                new BigDecimal("1200000.00"),
                "NGN"
        );

        when(createSalaryGradeHandler.execute(any(CreateSalaryGradeCommand.class)))
                .thenReturn(new CreateSalaryGradeResponse(9L));

        ResponseEntity<ApiResponse<CreateSalaryGradeResponse>> createResp = controller.createSalaryGrade(request, principal);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        assertNotNull(createResp.getBody());
        assertEquals(9L, createResp.getBody().data().gradeId());

        SalaryGradeResult gradeResult = new SalaryGradeResult(
                9L,
                10L,
                "Grade L6",
                new BigDecimal("1200000.00"),
                "NGN",
                List.of(),
                List.of()
        );
        when(listSalaryGradesHandler.execute(any(ListSalaryGradesQuery.class)))
                .thenReturn(List.of(gradeResult));

        ResponseEntity<ApiResponse<List<SalaryGradeResult>>> listResp = controller.listSalaryGrades(principal);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertNotNull(listResp.getBody());
        assertEquals(1, listResp.getBody().data().size());
    }
}
