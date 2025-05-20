package com.example.femobile.model.request.AuthRequest;

public class VerificationRequest {

    private String email;
    private String verificationCode;

    public VerificationRequest(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }
}
