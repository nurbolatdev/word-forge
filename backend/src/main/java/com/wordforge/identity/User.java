package com.wordforge.identity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String plan = "free";

    @Column(nullable = false)
    private String uiTheme = "light";

    private String nativeLang;

    @Column(nullable = false)
    private int dailyGoal = 20;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected User() {}

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getUiTheme() { return uiTheme; }
    public void setUiTheme(String uiTheme) { this.uiTheme = uiTheme; }
    public String getNativeLang() { return nativeLang; }
    public void setNativeLang(String nativeLang) { this.nativeLang = nativeLang; }
    public int getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(int dailyGoal) { this.dailyGoal = dailyGoal; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
