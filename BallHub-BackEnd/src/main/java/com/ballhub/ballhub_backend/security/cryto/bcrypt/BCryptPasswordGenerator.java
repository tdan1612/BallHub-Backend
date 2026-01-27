package com.ballhub.ballhub_backend.security.cryto.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Generate BCrypt hash cho password
 * RUN class này để lấy hash
 */
public class BCryptPasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Password cần hash
        String adminPassword = "admin123";
        String customerPassword = "123456";

        // Generate hash
        String adminHash = encoder.encode(adminPassword);
        String customerHash = encoder.encode(customerPassword);

        // Display results
        System.out.println("\n========================================");
        System.out.println("ADMIN ACCOUNT");
        System.out.println("========================================");
        System.out.println("Password: " + adminPassword);
        System.out.println("BCrypt Hash: " + adminHash);
        System.out.println("Hash Length: " + adminHash.length() + " chars");
        System.out.println("\nSQL INSERT:");
        System.out.println("INSERT INTO Users (FullName, Email, PasswordHash, Role, Status, CreatedAt)");
        System.out.println("VALUES (");
        System.out.println("  N'Admin User',");
        System.out.println("  'admin@ballhub.com',");
        System.out.println("  N'" + adminHash + "',");
        System.out.println("  'ADMIN',");
        System.out.println("  1,");
        System.out.println("  GETDATE()");
        System.out.println(");");

        System.out.println("\n========================================");
        System.out.println("CUSTOMER DEMO ACCOUNT");
        System.out.println("========================================");
        System.out.println("Password: " + customerPassword);
        System.out.println("BCrypt Hash: " + customerHash);
        System.out.println("Hash Length: " + customerHash.length() + " chars");
        System.out.println("\nSQL INSERT:");
        System.out.println("INSERT INTO Users (FullName, Email, PasswordHash, Phone, Role, Status, CreatedAt)");
        System.out.println("VALUES (");
        System.out.println("  N'Customer Demo',");
        System.out.println("  'customer@ballhub.com',");
        System.out.println("  N'" + customerHash + "',");
        System.out.println("  '0912345678',");
        System.out.println("  'CUSTOMER',");
        System.out.println("  1,");
        System.out.println("  GETDATE()");
        System.out.println(");");

        // Verify test
        System.out.println("\n========================================");
        System.out.println("VERIFICATION TEST");
        System.out.println("========================================");
        boolean adminMatches = encoder.matches(adminPassword, adminHash);
        boolean customerMatches = encoder.matches(customerPassword, customerHash);
        System.out.println("Admin password matches: " + adminMatches);
        System.out.println("Customer password matches: " + customerMatches);
        System.out.println("========================================\n");
    }
}
