package com.thehoick.bookpila.models

class Book (
        val _id: Int?,
        val id: Int?,
        val title: String?,
        val about: String?,
        val author: String?,
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
            "about": "$about",
            "author": "$author",
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
}