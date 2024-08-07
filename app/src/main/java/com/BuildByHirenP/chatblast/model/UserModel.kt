package com.BuildByHirenP.chatblast.model

import com.google.firebase.Timestamp

data class UserModel(
    var PhoneNumber : String? = null,
    var UserName : String? = null,
    var CreatedTimeStamp : Timestamp? = null,
    var UserId : String? = null,
    var FcmToken : String? = null
)