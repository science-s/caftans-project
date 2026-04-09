package com.caftans.mobile.data.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart_items")
    List<CartItem> getAll();

    @Insert
    void insert(CartItem item);

    @Delete
    void delete(CartItem item);

    @Query("DELETE FROM cart_items")
    void clear();
}
