package com.example.trading.Models;

public class UserProfileData {

    private String profile_photo;
    private String name;
    private String description;
    private String email;

    public UserProfileData(String profile_photo, String name, String description, String email) {
        this.profile_photo = profile_photo;
        this.name = name;
        this.description = description;
        this.email = email;
    }

    public UserProfileData() {

    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserProfileData{" +
                "profile_photo='" + profile_photo + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
