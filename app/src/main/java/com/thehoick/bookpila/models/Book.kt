package com.thehoick.bookpila.models

class Book (
        val _id: Int?,
        val id: Int?,
        val title: String?,
        val author: String?,
        val about: String?,
        val isbn: String?,
        val upload: String?,
        val current_loc: String?,
        var current_loc_folio: String?,
        val cover: String?,
        val cover_image: String?,
        val cover_url: String?,
        var local_filename: String?,
        var local_path: String?,
        val created_at: String?,
        var updated_at: String?
) {
    override fun toString(): String {
        return """{
            "_id": $_id,
            "id": $id,
            "title": "$title",
            "author": "$author",
            "about": "$about",
            "isbn": "$isbn",
            "upload": "$upload",
            "current_loc": "$current_loc",
            "current_loc_folio": $current_loc_folio,
            "cover": "$cover",
            "cover_image": "$cover_image",
            "cover_url": "$cover_url",
            "local_filename": "$local_filename",
            "local_path": "$local_path",
            "created_at": "$created_at",
            "updated_at": "$updated_at"
        }"""
    }

    fun toList(): List<Pair<String, String>> {
        return listOf(
                "id" to this.id.toString(),
                "title" to this.title.toString(),
                "author" to this.author.toString(),
                "about" to this.author.toString(),
                "isbn" to this.isbn.toString(),
                "current_loc" to this.current_loc.toString(),
                "current_loc_folio" to this.current_loc_folio.toString(),
                "upadted_at" to this.updated_at.toString()
        )
    }
}