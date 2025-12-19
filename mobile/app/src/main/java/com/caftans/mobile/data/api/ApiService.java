package com.caftans.mobile.data.api;

import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.data.models.Category;
import com.caftans.mobile.data.models.Reservation;
import com.caftans.mobile.data.models.User;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Auth endpoints
    @POST("auth/register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("auth/register")
    Call<ApiResponse> register(@Body User user);

    @POST("auth/login")
    Call<ApiResponse> login(@Body LoginRequest request);

    @GET("auth/me")
    Call<ApiResponse> getCurrentUser(@Header("Authorization") String token);

    @POST("auth/logout")
    Call<ApiResponse> logout(@Header("Authorization") String token);

    // Users endpoints
    @GET("users/me")
    Call<ApiResponse> getProfile(@Header("Authorization") String token);

    @PUT("users/me")
    Call<ApiResponse> updateProfile(@Header("Authorization") String token, @Body User user);

    // Categories endpoints
    @GET("categories")
    Call<ApiResponse> getCategories();

    @GET("categories/{id}")
    Call<ApiResponse> getCategory(@Path("id") int categoryId);

    // Caftans endpoints
    @GET("caftans")
    Call<ApiResponse> getCaftans(
            @Query("category_id") Integer categoryId,
            @Query("search") String search,
            @Query("availability") String availability);

    @GET("caftans/{id}")
    Call<ApiResponse> getCaftan(@Path("id") int caftanId);

    @POST("caftans")
    Call<ApiResponse> createCaftan(@Header("Authorization") String token, @Body Caftan caftan);

    @PUT("caftans/{id}")
    Call<ApiResponse> updateCaftan(@Header("Authorization") String token, @Path("id") int caftanId,
            @Body Caftan caftan);

    @DELETE("caftans/{id}")
    Call<ApiResponse> deleteCaftan(@Header("Authorization") String token, @Path("id") int caftanId);

    // Reservations endpoints
    @GET("reservations")
    Call<ApiResponse> getReservations(@Header("Authorization") String token);

    @GET("reservations/{id}")
    Call<ApiResponse> getReservation(@Header("Authorization") String token, @Path("id") int reservationId);

    @POST("reservations")
    Call<ApiResponse> createReservation(@Header("Authorization") String token, @Body ReservationRequest request);

    @PUT("reservations/{id}")
    Call<ApiResponse> updateReservation(@Header("Authorization") String token, @Path("id") int reservationId,
            @Body ReservationRequest request);

    @PATCH("reservations/{id}/status")
    Call<ApiResponse> updateReservationStatus(@Header("Authorization") String token, @Path("id") int reservationId,
            @Body StatusRequest request);

    @Multipart
    @POST("caftans")
    Call<ApiResponse> createCaftanMultipart(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("price_per_day") RequestBody pricePerDay,
            @Part("category_id") RequestBody categoryId,
            @Part("availability_status") RequestBody availabilityStatus,
            @Part MultipartBody.Part image);

    @Multipart
    @PUT("caftans/{id}")
    Call<ApiResponse> updateCaftanMultipart(
            @Header("Authorization") String token,
            @Path("id") int caftanId,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("price_per_day") RequestBody pricePerDay,
            @Part("category_id") RequestBody categoryId,
            @Part("availability_status") RequestBody availabilityStatus,
            @Part MultipartBody.Part image);

    @DELETE("reservations/{id}")
    Call<ApiResponse> deleteReservation(@Header("Authorization") String token, @Path("id") int reservationId);

    // Login request model
    class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
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
    }

    // Register request model
    class RegisterRequest {
        private String email;
        private String password;
        @SerializedName("full_name")
        private String fullName;
        private String phone;
        private String role;

        public RegisterRequest(String email, String password, String fullName, String phone, String role) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
        }
    }

    // Reservation request model
    class ReservationRequest {
        @SerializedName("caftan_id")
        private int caftan_id;

        @SerializedName("start_date")
        private String start_date;

        @SerializedName("end_date")
        private String end_date;

        @SerializedName("notes")
        private String notes;

        @SerializedName("status")
        private String status;

        public ReservationRequest(int caftanId, String startDate, String endDate, String notes) {
            this.caftan_id = caftanId;
            this.start_date = startDate;
            this.end_date = endDate;
            this.notes = notes;
        }

        public ReservationRequest(int caftanId, String startDate, String endDate, String notes, String status) {
            this.caftan_id = caftanId;
            this.start_date = startDate;
            this.end_date = endDate;
            this.notes = notes;
            this.status = status;
        }

        public int getCaftan_id() {
            return caftan_id;
        }

        public void setCaftan_id(int caftan_id) {
            this.caftan_id = caftan_id;
        }

        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public String getEnd_date() {
            return end_date;
        }

        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Status request model for PATCH endpoint
    class StatusRequest {
        @SerializedName("status")
        private String status;

        public StatusRequest(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
