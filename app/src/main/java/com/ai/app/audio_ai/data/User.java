package com.ai.app.audio_ai.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 用户实体类，用于存储用户信息
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String username;
    private String password;
    private int age;
    private String gender;
    private String preferences; // 存储用户喜好，可以是JSON格式
    private boolean isLoggedIn; // 标记用户是否已登录

    public User(String username, String password, int age, String gender, String preferences) {
        this.username = username;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.preferences = preferences;
        this.isLoggedIn = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
