package org.example.security.service;

import org.example.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class OwnershipVerifier {
    public void verifyOwnershipByUsername(String targetUsername, String currentUsername){
        if(!targetUsername.equals(currentUsername)){
            throw new AccessDeniedException("Authorization failed");
        }
    }
}
