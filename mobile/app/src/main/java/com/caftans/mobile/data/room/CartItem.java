package com.caftans.mobile.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items")
public class CartItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "caftan_id")
    public int caftanId;

    @ColumnInfo(name = "caftan_name")
    public String caftanName;

    @ColumnInfo(name = "caftan_image")
    public String caftanImage;

    @ColumnInfo(name = "price_per_day")
    public double pricePerDay;

    @ColumnInfo(name = "start_date")
    public String startDate;

    @ColumnInfo(name = "end_date")
    public String endDate;

    @ColumnInfo(name = "notes")
    public String notes;

    public CartItem(int caftanId, String caftanName, String caftanImage, double pricePerDay, String startDate,
            String endDate, String notes) {
        this.caftanId = caftanId;
        this.caftanName = caftanName;
        this.caftanImage = caftanImage;
        this.pricePerDay = pricePerDay;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
    }
}
