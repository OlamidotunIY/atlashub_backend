package com.atlashub.accounting.gl.presentation.rest;

import com.atlashub.accounting.gl.application.command.ApproveJournalEntry.ApproveJournalEntryCommand;
import com.atlashub.accounting.gl.application.command.ApproveJournalEntry.ApproveJournalEntryHandler;
import com.atlashub.accounting.gl.application.command.ApproveJournalEntry.ApproveJournalEntryResult;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.CreateJournalEntryCommand;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.CreateJournalEntryHandler;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.CreateJournalEntryResult;
import com.atlashub.accounting.gl.application.command.VoidJournalEntry.VoidJournalEntryCommand;
import com.atlashub.accounting.gl.application.command.VoidJournalEntry.VoidJournalEntryHandler;
import com.atlashub.accounting.gl.application.command.VoidJournalEntry.VoidJournalEntryResult;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.GetJournalEntryByIdHandler;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.GetJournalEntryByIdQuery;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.JournalEntryResult;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.JournalLineResult;
import com.atlashub.accounting.gl.application.query.ListJournalEntries.JournalEntrySummaryResult;
import com.atlashub.accounting.gl.application.query.ListJournalEntries.ListJournalEntriesHandler;
import com.atlashub.accounting.gl.application.query.ListJournalEntries.ListJournalEntriesQuery;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.accounting.gl.presentation.dto.CreateJournalEntryRequest;
import com.atlashub.accounting.gl.presentation.dto.CreateJournalEntryResponse;
import com.atlashub.accounting.gl.presentation.dto.JournalEntryResponse;
import com.atlashub.accounting.gl.presentation.dto.JournalLineRequest;
import com.atlashub.accounting.gl.presentation.dto.VoidJournalEntryRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JournalEntryControllerTest {

    private CreateJournalEntryHandler createHandler;
    private GetJournalEntryByIdHandler getByIdHandler;
    private ListJournalEntriesHandler listHandler;
    private ApproveJournalEntryHandler approveHandler;
    private VoidJournalEntryHandler voidHandler;
    private JournalEntryController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            1001L, 10L, "TEST", "session-1", "token-1", ZonedDateTime.now());

    @BeforeEach
    void setUp() {
        createHandler = mock(CreateJournalEntryHandler.class);
        getByIdHandler = mock(GetJournalEntryByIdHandler.class);
        listHandler = mock(ListJournalEntriesHandler.class);
        approveHandler = mock(ApproveJournalEntryHandler.class);
        voidHandler = mock(VoidJournalEntryHandler.class);
        controller = new JournalEntryController(
                createHandler, getByIdHandler, listHandler, approveHandler, voidHandler);
    }

    @Test
    void recordJournalEntry_delegatesToHandler() {
        List<JournalLineRequest> lineRequests = List.of(
                new JournalLineRequest(1L, new BigDecimal("100.00"), "NGN", EntryType.DEBIT),
                new JournalLineRequest(2L, new BigDecimal("100.00"), "NGN", EntryType.CREDIT)
        );
        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                "JE-001", LocalDate.now(), "REF-1", "Description", lineRequests);
        CreateJournalEntryResult result = new CreateJournalEntryResult(
                100L, "JE-001", JournalEntryStatus.DRAFT, 2);

        when(createHandler.execute(any(CreateJournalEntryCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<CreateJournalEntryResponse>> response = controller.recordJournalEntry(principal, request);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("JE-001", response.getBody().data().entryNumber());
        verify(createHandler).execute(any(CreateJournalEntryCommand.class));
    }

    @Test
    void getJournalEntryById_delegatesToHandler() {
        Money total = Money.of(new BigDecimal("100.00"), CurrencyCode.NGN);
        JournalLineResult lineResult = new JournalLineResult(1L, 10L, total, EntryType.DEBIT);
        JournalEntryResult result = new JournalEntryResult(
                100L, 10L, "JE-001", LocalDate.now(), "REF-1", "Desc",
                List.of(lineResult), JournalEntryStatus.DRAFT, EntrySource.MANUAL,
                total, total, 1001L, null, null, ZonedDateTime.now());

        when(getByIdHandler.execute(any(GetJournalEntryByIdQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<JournalEntryResponse>> response = controller.getJournalEntryById(principal, 100L);

        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().data().id());
        assertEquals("JE-001", response.getBody().data().entryNumber());
        assertEquals(1, response.getBody().data().lines().size());
        verify(getByIdHandler).execute(new GetJournalEntryByIdQuery(100L, 10L));
    }

    @Test
    void listJournalEntries_delegatesToHandler() {
        JournalEntrySummaryResult summary = new JournalEntrySummaryResult(
                100L, 10L, "JE-001", LocalDate.now(), "REF-1", "Desc", 2,
                JournalEntryStatus.DRAFT, EntrySource.MANUAL,
                Money.of(new BigDecimal("100.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("100.00"), CurrencyCode.NGN),
                1001L, null, null, ZonedDateTime.now());

        when(listHandler.execute(any(ListJournalEntriesQuery.class))).thenReturn(List.of(summary));

        ResponseEntity<ApiResponse<List<JournalEntrySummaryResult>>> response = controller.listJournalEntries(principal, null);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        verify(listHandler).execute(new ListJournalEntriesQuery(10L, null));
    }

    @Test
    void approveJournalEntry_delegatesToHandler() {
        ApproveJournalEntryResult result = new ApproveJournalEntryResult(
                100L, "JE-001", JournalEntryStatus.POSTED, 1001L, ZonedDateTime.now());
        when(approveHandler.execute(any(ApproveJournalEntryCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<Void>> response = controller.approveJournalEntry(principal, 100L);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        verify(approveHandler).execute(new ApproveJournalEntryCommand(100L, 10L, 1001L));
    }

    @Test
    void voidJournalEntry_delegatesToHandler() {
        VoidJournalEntryResult result = new VoidJournalEntryResult(100L, "JE-001", JournalEntryStatus.VOIDED);
        when(voidHandler.execute(any(VoidJournalEntryCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<Void>> response = controller.voidJournalEntry(
                principal, 100L, new VoidJournalEntryRequest("Duplicate error"));

        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        verify(voidHandler).execute(new VoidJournalEntryCommand(100L, 10L, 1001L, "Duplicate error"));
    }
}
