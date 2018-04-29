package com.thehoick.bookpila.models

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject


class BookPilaDataSource(context: Context) {
    val TAG = BookPilaDataSource::class.java.simpleName
    val PhotolandiaSqliteHelper = BookPilaSqliteHelper(context)

    private fun open(): SQLiteDatabase {
        return PhotolandiaSqliteHelper.readableDatabase
    }

    private fun close(database: SQLiteDatabase) {
        database.close()
    }

    fun createBook(book: JSONObject) {
        val db = open()

        // Implementation details... maybe don't need to use transactions for simple inserts.
        val bookValues = ContentValues()
        bookValues.put("id", book.get("id").toString())
        bookValues.put("title", book.get("title").toString())
        bookValues.put("author", book.get("author").toString())
        bookValues.put("about", book.get("about").toString())
        bookValues.put("isbn", book.get("isbn").toString())
        bookValues.put("upload", book.get("upload").toString())
        bookValues.put("current_loc", book.get("current_loc").toString())
        bookValues.put("cover", book.get("cover").toString())
        bookValues.put("cover_image", book.get("cover_image").toString())
        bookValues.put("cover_url", book.get("cover_url").toString())
        bookValues.put("local_filename", book.get("local_filename").toString())
        bookValues.put("local_filename", book.get("local_path").toString())
        bookValues.put("created_at", book.get("created_at").toString())
        bookValues.put("updated_at", book.get("updated_at").toString())

        val bookId = db.insert("books", null, bookValues)

        close(db)
    }

    fun getBook(title: String): Book? {
        val db = open()

        val columns = arrayOf<String>(
            "_id",
            "id",
            "title",
            "about",
            "author",
            "isbn",
            "upload",
            "current_loc",
            "cover",
            "cover_image",
            "cover_url",
            "local_filename",
            "local_path",
            "created_at",
            "updated_at"
        )

        val cursor = db.rawQuery("select * from books where title = \"$title\";", null)
        var book: Book? = null
        if (cursor.moveToFirst()) {
            do {
                book = Book(
                    cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("author")),
                    cursor.getString(cursor.getColumnIndex("about")),
                    cursor.getString(cursor.getColumnIndex("isbn")),
                    cursor.getString(cursor.getColumnIndex("upload")),
                    cursor.getString(cursor.getColumnIndex("current_loc")),
                    cursor.getString(cursor.getColumnIndex("cover")),
                    cursor.getString(cursor.getColumnIndex("cover_image")),
                    cursor.getString(cursor.getColumnIndex("cover_url")),
                    cursor.getString(cursor.getColumnIndex("local_filename")),
                    cursor.getString(cursor.getColumnIndex("local_path")),
                    cursor.getString(cursor.getColumnIndex("created_at")),
                    cursor.getString(cursor.getColumnIndex("updated_at"))
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        close(db)
        return book
    }

    fun getBooks(): List<Book> {
        val db = open()

        val columns = arrayOf<String>(
                "_id",
                "id",
                "title",
                "about",
                "author",
                "isbn",
                "upload",
                "current_loc",
                "cover",
                "cover_image",
                "cover_url",
                "local_filename",
                "local_path",
                "created_at",
                "updated_at"
        )
        val cursor = db.query(
                "books",
                columns,
                null,
                null,
                null,
                null,
                null
        )

        val books = arrayListOf<Book>()
        if (cursor.moveToFirst()) {
            do {
                val book = Book(
                    cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("author")),
                    cursor.getString(cursor.getColumnIndex("about")),
                    cursor.getString(cursor.getColumnIndex("isbn")),
                    cursor.getString(cursor.getColumnIndex("upload")),
                    cursor.getString(cursor.getColumnIndex("current_loc")),
                    cursor.getString(cursor.getColumnIndex("cover")),
                    cursor.getString(cursor.getColumnIndex("cover_image")),
                    cursor.getString(cursor.getColumnIndex("cover_url")),
                    cursor.getString(cursor.getColumnIndex("local_filename")),
                    cursor.getString(cursor.getColumnIndex("local_path")),
                    cursor.getString(cursor.getColumnIndex("created_at")),
                    cursor.getString(cursor.getColumnIndex("updated_at"))
                )
                books.add(book)
            } while (cursor.moveToNext())
        }
        cursor.close()
        close(db)
        return books
    }


    fun updateBook(book: Book) {
        val db = open()

        val updateBookValues = ContentValues()
        updateBookValues.put("title", book.title)
        updateBookValues.put("author", book.author)
        updateBookValues.put("about", book.about)
        updateBookValues.put("isbn", book.isbn)
        updateBookValues.put("upload", book.upload)
        updateBookValues.put("cover", book.cover)
        updateBookValues.put("cover_image", book.cover_image)
        updateBookValues.put("cover_url", book.cover_url)
        updateBookValues.put("current_loc", book.local_path)
        updateBookValues.put("local_filename", book.local_filename)
        updateBookValues.put("local_path", book.local_path)
        updateBookValues.put("created_at", book.created_at)
        updateBookValues.put("updated_at", book.updated_at)

        db.update(
                "books",
                updateBookValues,
                "title = \"${book.title}\"",
                null
        )

        close(db)
    }

    fun deleteBook(title: String) {
        val db = open()

        db.delete("books", "title = ${title}", null)

        close(db)
    }
}