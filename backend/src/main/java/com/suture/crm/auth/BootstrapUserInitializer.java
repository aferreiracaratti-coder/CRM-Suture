package com.suture.crm.auth;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
class BootstrapUserInitializer implements ApplicationRunner {
    private final SutureAuthProperties properties;
    private final CrmUserRepository users;
    private final PasswordEncoder passwords;

    BootstrapUserInitializer(SutureAuthProperties properties, CrmUserRepository users, PasswordEncoder passwords) {
        this.properties = properties;
        this.users = users;
        this.passwords = passwords;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.getBootstrapPassword()) || users.findByEmailIgnoreCase(properties.getBootstrapEmail()).isPresent()) return;
        users.save(new CrmUser(
                UUID.randomUUID(), properties.getBootstrapTenantId(), properties.getBootstrapEmail(), properties.getBootstrapName(),
                passwords.encode(properties.getBootstrapPassword()), "CRM_ADMIN,SYNA_SALES", true
        ));
    }
}
