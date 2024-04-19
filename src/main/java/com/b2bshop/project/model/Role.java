package com.b2bshop.project.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_SYSTEM_OWNER("SYSTEM_OWNER"),
    ROLE_SHOP_OWNER("SHOP_OWNER"),
    ROLE_CUSTOMER_USER("CUSTOMER_USER"),
    ROLE_ADMIN("ADMIN");

    private String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
