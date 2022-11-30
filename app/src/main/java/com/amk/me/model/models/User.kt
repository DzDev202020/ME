package com.amk.me.model.models

class User(
    var full_name: String,
    var email: String,
    var password: String
) {
    lateinit var id: String
}