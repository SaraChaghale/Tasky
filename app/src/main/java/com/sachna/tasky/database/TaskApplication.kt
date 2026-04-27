package com.sachna.tasky.database

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


class TaskApplication : Application() {


    companion object {

        lateinit var database: TaskDatabase
    }


    override fun onCreate() {
        super.onCreate()


        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("ALTER TABLE StoreEntity ADD TYPE ''")
            }
        }


        database = Room.databaseBuilder(
            this,
            TaskDatabase::class.java,
            "Tasky-db"
        ).addMigrations(MIGRATION_1_2)
            .build()
    }
}


