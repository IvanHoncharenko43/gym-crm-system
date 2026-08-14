package org.example.security.service;

import org.example.exception.AccessForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class OwnershipVerifier {
    public void verifyOwnershipByUsername(String targetUsername, String currentUsername){
        if(!targetUsername.equals(currentUsername)){
            throw new AccessForbiddenException("Authorization failed");
        }
    }
}
