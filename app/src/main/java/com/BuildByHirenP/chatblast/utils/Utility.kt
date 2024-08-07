package com.BuildByHirenP.chatblast.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.Constant.PREFS_NAME
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class Utility {

    var mActivity : Activity

    constructor(mActivity: Activity) {
        this.mActivity = mActivity
    }

    fun writeSharedPreferencesString(key : String, value: String){

        val editor = mActivity.getSharedPreferences(PREFS_NAME, 0).edit()

        editor.putString(key, value)
        editor.apply()

    }

    fun clearAllData(){
        val setting = mActivity.getSharedPreferences(PREFS_NAME, 0).edit()
        setting.clear().commit()
    }

    fun getAppPrefString(key: String): String? {
        try {
            val setting = mActivity.getSharedPreferences(PREFS_NAME, 0)
            val value = setting.getString(key, "")

            return value
        }catch (e : Exception){
            e.printStackTrace()
            return ""
        }
    }

    companion object {

        fun passUserModelAsIntent(intent: Intent, model: UserModel) {
            intent.putExtra("username", model.UserName)
            intent.putExtra("phonenumber", model.PhoneNumber)
            intent.putExtra("userid", model.UserId)
            intent.putExtra("fcmToken", model.FcmToken)
        }

        fun getUserModelFromIntent(intent: Intent): UserModel? {
            return UserModel().apply {
                UserName = intent.getStringExtra("username")?: ""
                PhoneNumber = intent.getStringExtra("phonenumber")?: ""
                UserId = intent.getStringExtra("userid")?: ""
                FcmToken = intent.getStringExtra("fcmToken")?: ""
            }
        }

        fun setProfilePic(context : Context, imageUri : Uri, imageView : ImageView){
            Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView)
        }
    }
}