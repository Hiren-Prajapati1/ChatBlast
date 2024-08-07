package com.BuildByHirenP.chatblast.model

import com.google.firebase.Timestamp

data class ChatRoomModel(
    var ChatRoomId : String? = null,
    var UserIds : List<String?>? = null,
    var LastMessageTimestamp: Timestamp,
    var LastMessageSenderId : String? = null,
    var LastMessage : String? = null
) {
    constructor() : this("", emptyList(), Timestamp.now(), "", "")
}