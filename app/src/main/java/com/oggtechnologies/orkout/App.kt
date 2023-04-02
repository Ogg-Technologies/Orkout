package com.oggtechnologies.orkout

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oggtechnologies.orkout.model.database.AppDatabase
import com.oggtechnologies.orkout.model.database.prepopulateData

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        contextOrNull = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var contextOrNull: Context? = null
            private set

        val context: Context
            get() {
                checkNotNull(contextOrNull) { "Context is not initialized. Context cannot be accessed before Application has started" }
                return contextOrNull!!
            }

        val prefs: SharedPreferences by lazy {
            context.getSharedPreferences(
                "main",
                Context.MODE_PRIVATE
            )
        }

        val db: AppDatabase by lazy {
            Room.databaseBuilder(context, AppDatabase::class.java, "orkout.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        prepopulateData()
                        println("Database created")
                    }
                })
                .build()
        }
    }
}