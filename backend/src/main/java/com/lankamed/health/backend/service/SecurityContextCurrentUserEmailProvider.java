package com.lankamed.health.backend.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentUserEmailProvider implements CurrentUserEmailProvider {
    @Override
    public String getCurrentUserEmail() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("SecurityContextCurrentUserEmailProvider: Authentication object: " + auth);
            System.out.println("SecurityContextCurrentUserEmailProvider: Authentication name: " + (auth != null ? auth.getName() : "null"));

            if (auth == null) {
                System.err.println("SecurityContextCurrentUserEmailProvider: No authentication found");
                return null;
            }

            String authName = auth.getName();
            System.out.println("SecurityContextCurrentUserEmailProvider: Authentication name: " + authName);

            if (authName == null || authName.equals("anonymousUser")) {
                System.err.println("SecurityContextCurrentUserEmailProvider: Anonymous user detected");
                return null;
            }

            return authName;
        } catch (Exception e) {
            System.err.println("SecurityContextCurrentUserEmailProvider: Error getting current user email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
