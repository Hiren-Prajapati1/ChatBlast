package com.BuildByHirenP.chatblast.model

import com.google.firebase.Timestamp

data class ChatMessageModel(

    var Message : String? = null,
    var SenderId : String? = null,
    var TimeStamp : Timestamp? = null
)
