package com.subscriptiq.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PrivilegedAccountForm {
    @NotBlank
    @Size(max = 120)
    private String fullName = "";

    @NotBlank
    @Email
    @Size(max = 254)
    private String email = "";

    @NotBlank
    @Size(min = 9, max = 72)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).+$",
            message = "Use an uppercase letter, a lowercase letter, and a special character.")
    private String password = "";

    @NotNull
    private AccountRole role = AccountRole.STAFF;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccountRole getRole() {
        return role;
    }

    public void setRole(AccountRole role) {
        this.role = role;
    }
}
