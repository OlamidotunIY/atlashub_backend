package com.atlashub.hr.leave.application.command.RejectLeave;

public record RejectLeaveCommand(Long leaveApplicationId, String reason) {}
