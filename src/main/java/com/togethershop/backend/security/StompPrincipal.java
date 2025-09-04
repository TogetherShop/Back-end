package com.togethershop.backend.security;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final CustomUserDetails userDetails;

    public StompPrincipal(CustomUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public String getName() {
        return String.valueOf(userDetails.getUserId());
    }

    public CustomUserDetails getUserDetails() {
        return userDetails;
    }
}
