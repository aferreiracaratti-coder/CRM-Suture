package com.suture.crm.syna;

import java.util.List;

public record CrmSessionPrincipal(String subject, String name, List<String> roles) { }
