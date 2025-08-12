package ecommerce.ecommercevaldani.service;

public interface EmailService {
    public void sendVerificationEmail(String email, String verificationToken);
}
