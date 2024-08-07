package com.BuildByHirenP.chatblast.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.BuildByHirenP.chatblast.ChatActivity
import com.BuildByHirenP.chatblast.R
import com.BuildByHirenP.chatblast.SearchUser
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class searchUserAdapter(options: FirestoreRecyclerOptions<UserModel> , val context : Context) : FirestoreRecyclerAdapter<UserModel, searchUserAdapter.ViewHolder>(options) {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val SearchUseImage = itemView.findViewById<ImageView>(R.id.raw_search_user_imageview)
        val SearchUserName = itemView.findViewById<TextView>(R.id.raw_search_user_textview_name)
        val SearchUserMobileNO = itemView.findViewById<TextView>(R.id.raw_search_user_textview_mobileno)
        lateinit var userModel: UserModel // Add this property
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.raw_search_user_item, parent, false)

        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: UserModel) {

        holder.SearchUserName.setText(model.UserName)
        holder.SearchUserMobileNO.setText(model.PhoneNumber)

        FirebaseUtils.getOtherProfilePicsStorageRef(model.UserId.toString()).downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri = task.result
                Utility.setProfilePic(context, uri, holder.SearchUseImage)
            }
        }

        if (model.UserId.equals(FirebaseUtils.currentUserId())){
            holder.SearchUserName.setText(model.UserName + "(Me)")
        }
        holder.userModel = model // Set the userModel property
        holder.itemView.setOnClickListener {
            val i = Intent(context, ChatActivity::class.java)
            Utility.passUserModelAsIntent(i, holder.userModel)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            Animatoo.animateSwipeLeft(context)
        }
    }
}