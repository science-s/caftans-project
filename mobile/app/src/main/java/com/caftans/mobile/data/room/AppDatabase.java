package com.caftans.mobile.data.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { CartItem.class }, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CartDao cartDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "caftans-db")
                            .allowMainThreadQueries() // Allowing main thread for simplicity in this refactor, ideally
                                                      // should be async
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
