package com.example.messengerapp.models

class ChatMessage(val id: String,val text: String,val fromId : String,val toId: String, val timestamp: Long, val readReceipt: String) {
    constructor() : this("","","","",-1, "")
}
