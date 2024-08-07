package com.BuildByHirenP.chatblast.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.BuildByHirenP.chatblast.ChatActivity
import com.BuildByHirenP.chatblast.R
import com.BuildByHirenP.chatblast.SearchUser
import com.BuildByHirenP.chatblast.model.ChatMessageModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ChatRecyclerAdapter(options: FirestoreRecyclerOptions<ChatMessageModel> , val context : Context) : FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ViewHolder>(options) {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val LeftChatLayout = itemView.findViewById<LinearLayout>(R.id.raw_left_chat_layout)
        val LeftChatTextView = itemView.findViewById<TextView>(R.id.raw_left_chat_textview)
        val LeftChatTime = itemView.findViewById<TextView>(R.id.raw_left_chat_time_textview)
        val RightChatLayout = itemView.findViewById<LinearLayout>(R.id.raw_right_chat_layout)
        val RightChatTextView = itemView.findViewById<TextView>(R.id.raw_right_chat_textview)
        val RightChatTime = itemView.findViewById<TextView>(R.id.raw_right_chat_time_textview)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(context).inflate(R.layout.raw_chat_message_recycler, parent, false)

        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ChatMessageModel) {
        Log.e("TAG", "onBindViewHolder: =====callll====11111111" )

        if (model.SenderId.equals(FirebaseUtils.currentUserId())){
            holder.LeftChatLayout.visibility = View.GONE
            holder.RightChatLayout.visibility = View.VISIBLE
            holder.RightChatTextView.setText(model.Message)

            holder.RightChatTime.text = FirebaseUtils.TimeStampToString(model.TimeStamp!!)
        }else{
            holder.RightChatLayout.visibility = View.GONE
            holder.LeftChatLayout.visibility = View.VISIBLE
            holder.LeftChatTextView.setText(model.Message)

            holder.LeftChatTime.text = FirebaseUtils.TimeStampToString(model.TimeStamp!!)
        }
    }
}