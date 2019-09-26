package com.dasharath.chittichat2.ui.findfriends

data class ContactsModel(
    var name: String,
    var status: String,
    var image: String,
    var uid: String
){
    constructor() : this("","","","")
}