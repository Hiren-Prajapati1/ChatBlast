package com.BuildByHirenP.chatblast.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.BuildByHirenP.chatblast.ChatActivity
import com.BuildByHirenP.chatblast.R
import com.BuildByHirenP.chatblast.SearchUser
import com.BuildByHirenP.chatblast.model.ChatRoomModel
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RecentRecyclerChatAdapter(options: FirestoreRecyclerOptions<ChatRoomModel> , private val context : Context) : FirestoreRecyclerAdapter<ChatRoomModel, RecentRecyclerChatAdapter.ViewHolder>(options) {

    var lastPosition : Int = -1

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val UserChatImage = itemView.findViewById<ImageView>(R.id.raw_recent_user_chat_imageview)
        val UserChatName = itemView.findViewById<TextView>(R.id.raw_recent_user_chat_textview_name)
        val UserChatLastMessage = itemView.findViewById<TextView>(R.id.raw_recent_user_chat_textview_last_message)
        val UserChatLastMessageTime = itemView.findViewById<TextView>(R.id.raw_recent_user_chat_textview_last_message_time)
        val UserChatUnseenMessage = itemView.findViewById<TextView>(R.id.raw_recent_user_chat_textview_unseen_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.raw_recent_user_chat_recycler_row, parent, false)

        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ChatRoomModel) {
        setAnimation(holder.itemView, position)
        Log.e("TAG", "onBindViewHolder: ------------111111111111111111---------start" )

        FirebaseUtils.getOtherUserFromChatRoom(model.UserIds).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val otherUserModel = document.toObject(UserModel::class.java)
                    if (otherUserModel != null) {
                        FirebaseUtils.getOtherProfilePicsStorageRef(otherUserModel.UserId.toString()).downloadUrl
                            .addOnCompleteListener { t ->
                                if (t.isSuccessful) {
                                    val uri: Uri = t.result
                                    Utility.setProfilePic(context, uri, holder.UserChatImage)
                                } else {
                                    Log.e("ProfilePicDownload", "Failed to get profile pic URL: ${t.exception}")
                                }
                            }

                        val lastMessageSentByMe = model.LastMessageSenderId == FirebaseUtils.currentUserId()
                        holder.UserChatName.text = otherUserModel.UserName
                        holder.UserChatLastMessage.text = if (lastMessageSentByMe) {
                            "You: ${model.LastMessage}"
                        } else {
                            model.LastMessage
                        }
                        holder.UserChatLastMessageTime.text = FirebaseUtils.TimeStampToString(model.LastMessageTimestamp)

                        holder.itemView.setOnClickListener {
                            // Navigate to chat activity
                            val intent = Intent(context, ChatActivity::class.java)
                            Utility.passUserModelAsIntent(intent, otherUserModel)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            Animatoo.animateSwipeLeft(context)
                        }
                    } else {
                        Log.e("FirestoreData", "UserModel data is null for userIds: ${model.UserIds}")
                    }
                } else {
                    Log.e("FirestoreData", "Document does not exist for userIds: ${model.UserIds}")
                }
            } else {
                Log.e("FirestoreTask", "Failed to get user data: ${task.exception}")
            }
        }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left) // this animation is inbuilt
            viewToAnimate.startAnimation(slideIn)
            lastPosition = position
        }
    }

}