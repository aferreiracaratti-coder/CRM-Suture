package com.suture.crm.dashboard;

import java.math.BigDecimal;

public record DashboardResponse(long opportunitiesRequiringAttention, long overdueFollowUps, long proposalsAwaitingResponse, BigDecimal openPipelineValue) { }
