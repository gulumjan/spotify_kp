package com.example.spotify_kp.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.spotify_kp.data.local.dao.AlbumDao;
import com.example.spotify_kp.data.local.dao.FavoriteDao;
import com.example.spotify_kp.data.local.dao.UserDao;
import com.example.spotify_kp.data.local.entity.AlbumEntity;
import com.example.spotify_kp.data.local.entity.FavoriteEntity;
import com.example.spotify_kp.data.local.entity.UserEntity;
import com.example.spotify_kp.utils.Constants;

@Database(
        entities = {UserEntity.class, AlbumEntity.class, FavoriteEntity.class},
        version = Constants.DATABASE_VERSION,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract AlbumDao albumDao();
    public abstract FavoriteDao favoriteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            Constants.DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // ✅ Разрешаем работу в main thread для офлайн работы
                    .setJournalMode(JournalMode.TRUNCATE) // ✅ БЕЗ WAL! Прямая запись на диск!
                    .build();
        }
        return instance;
    }
}