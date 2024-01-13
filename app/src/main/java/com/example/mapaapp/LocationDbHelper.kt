package com.example.mapaapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class LocationDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "UbicacionesMapa.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${LocationContract.LocationEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${LocationContract.LocationEntry.COLUMN_LATITUD} REAL," +
                    "${LocationContract.LocationEntry.COLUMN_LONGITUD} REAL," +
                    "${LocationContract.LocationEntry.COLUMN_DESCRIPCION} TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${LocationContract.LocationEntry.TABLE_NAME}"
    }
}