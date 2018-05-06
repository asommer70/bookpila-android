package com.thehoick.bookpila.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

val DB_NAME = "bookpila.db"
val DB_VERSION = 1

class BookPilaSqliteHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createBooks = """create table books (
                _id integer primary key autoincrement,
                id integer,
                title text,
                about text,
                author text,
                isbn text,
                upload text,
                current_loc text,
                current_loc_folio text,
                cover text,
                cover_image text,
                cover_url text,
                local_filename text,
                local_path text,
                created_at text,
                updated_at text
            );
        """
        db?.execSQL(createBooks)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}