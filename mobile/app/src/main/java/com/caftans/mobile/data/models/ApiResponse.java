package com.caftans.mobile.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("user")
    private User user;

    @SerializedName("categories")
    private List<Category> categories;

    @SerializedName("caftans")
    private List<Caftan> caftans;

    @SerializedName("caftan")
    private Caftan caftan;

    @SerializedName("reservations")
    private List<Reservation> reservations;

    @SerializedName("reservation")
    private Reservation reservation;

    @SerializedName("error")
    private String error;

    @SerializedName("image_url")
    private String imageUrl;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Caftan> getCaftans() {
        return caftans;
    }

    public void setCaftans(List<Caftan> caftans) {
        this.caftans = caftans;
    }

    public Caftan getCaftan() {
        return caftan;
    }

    public void setCaftan(Caftan caftan) {
        this.caftan = caftan;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
