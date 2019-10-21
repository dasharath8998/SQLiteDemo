package com.dasharath.chittichat2.models

data class ContactsModel(
    var name: String,
    var status: String,
    var image: String,
    var gender: String,
    var countryCode: String,
    var uid: String
){
    constructor() : this("","","","","","")
}

data class Messages(
    var from: String,
    var message: String,
    var type: String,
    var to: String,
    var messagaeId: String,
    var time: String,
    var date: String,
    var name: String
){
    constructor():this("","","","","","","","")
}