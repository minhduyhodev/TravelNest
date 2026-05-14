package com.travelnest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.seed.default-admin")
public class DefaultAdminProperties {

    private boolean enabled = true;
    private String email = "admin@travelnest.local";
    private String password = "Admin@123";
    private String fullName = "TravelNest Admin";
    private String phone = "0900000000";
    private String preferredLang = "vi";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPreferredLang() {
        return preferredLang;
    }

    public void setPreferredLang(String preferredLang) {
        this.preferredLang = preferredLang;
    }
}
