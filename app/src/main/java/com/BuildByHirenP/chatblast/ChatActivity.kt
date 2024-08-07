package com.BuildByHirenP.chatblast

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BuildByHirenP.chatblast.adapter.ChatRecyclerAdapter
import com.BuildByHirenP.chatblast.databinding.ActivityChatBinding
import com.BuildByHirenP.chatblast.model.ChatMessageModel
import com.BuildByHirenP.chatblast.model.ChatRoomModel
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.Constant
import com.BuildByHirenP.chatblast.utils.Constant.CHATTHEME
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    lateinit var binding : ActivityChatBinding
    lateinit var OtherUser : UserModel
    lateinit var chatRoomId : String
    lateinit var chatroommodel : ChatRoomModel
    lateinit var adapter: ChatRecyclerAdapter

    lateinit var mUtility : Utility
    lateinit var ActiveTheme : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.chatToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val mActivity = this
        mUtility = Utility(mActivity)

        ActiveTheme = mUtility.getAppPrefString(Constant.CHATTHEME).toString()
        Log.e("TAG", "onCreate: ======Theme--value====" + ActiveTheme )
        if (ActiveTheme == "Theme1"){
            activeTheme1()
        }else if (ActiveTheme == "Theme2"){
            activeTheme2()
        }else if (ActiveTheme == "Theme3"){
            activeTheme3()
        }


        OtherUser = Utility.getUserModelFromIntent(intent)!!
        chatRoomId = FirebaseUtils.getChatRoomId(FirebaseUtils.currentUserId(), OtherUser.UserId)
        updateCurrentTime(binding.textTimeChat)

        if (OtherUser!= null) {
            binding.chatUsername.text = OtherUser.UserName
        } else {
            Log.e("ChatActivity", "UserModel is null")
        }

        FirebaseUtils.getOtherProfilePicsStorageRef(OtherUser.UserId.toString()).downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri = task.result
                Utility.setProfilePic(this, uri, binding.imageContact)
            }
        }

        binding.btnsendmessagechat.setOnClickListener {
            val message = binding.messageInputchat.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToUser(message)
            }
        }

        binding.messageInputchat.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val message = binding.messageInputchat.text.toString().trim()
                if (message.isNotEmpty()) {
                    sendMessageToUser(message)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        val contentView = findViewById<ConstraintLayout>(R.id.main)
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val keyboardHeight = getKeyboardHeight(contentView)
            if (keyboardHeight > 0) {
                // Keyboard is visible
                adjustLayout(keyboardHeight)
            } else {
                // Keyboard is hidden
                resetLayout()
            }
        }

        binding.chatToolbar.setNavigationOnClickListener {
            finish()
            Animatoo.animateSwipeRight(this)
        }

        getOrCreateChatroomModel()
        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {
        val query = FirebaseUtils.getChatRoomMessageReference(chatRoomId)
            .orderBy("timeStamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java).build()

        adapter = ChatRecyclerAdapter(options, this)
        binding.recyclerChat.adapter = adapter
        adapter.startListening()
        val manager = LinearLayoutManager(this)
        manager.reverseLayout = true
        binding.recyclerChat.layoutManager = manager
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.recyclerChat.smoothScrollToPosition(0)
            }
        })
    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtils.getChatRoomReference(chatRoomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chatroommodel = task.result.toObject(ChatRoomModel::class.java) ?: run {
                    // first time chat
                    val userIds = listOf(FirebaseUtils.currentUserId(), OtherUser.UserId ?: "")

                    ChatRoomModel(chatRoomId, userIds, Timestamp.now(), "").also {
                        FirebaseUtils.getChatRoomReference(chatRoomId).set(it)
                        Log.e("TAG", "getOrCreateChatroomModel: =====User-Ids====1===" +  OtherUser.UserId + "==Token===")
                        Log.e("TAG", "getOrCreateChatroomModel: =====User-Ids==lists=====" +  userIds)
                    }
                }
            }
        }
    }

    private fun sendMessageToUser(message: String) {
        chatroommodel.LastMessageTimestamp = Timestamp.now()
        chatroommodel.LastMessageSenderId = FirebaseUtils.currentUserId()
        chatroommodel.LastMessage = message
        FirebaseUtils.getChatRoomReference(chatRoomId).set(chatroommodel)

        val chatMessageModel = ChatMessageModel(message, FirebaseUtils.currentUserId(), Timestamp.now())
        FirebaseUtils.getChatRoomMessageReference(chatRoomId).add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.messageInputchat.setText("")
                    sendNotification(message)
                }
            }
    }

    private fun sendNotification(message: String) {
        FirebaseUtils.currentUserDetails()!!.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = task.result.toObject(UserModel::class.java)
                try {
                    val jsonObject = JSONObject()

                    val notificationObj = JSONObject()
                    notificationObj.put("title", currentUser?.UserName)
                    notificationObj.put("body", message)

                    val dataObj = JSONObject()
                    dataObj.put("userId", currentUser?.UserId)

                    jsonObject.put("message", JSONObject().apply {
                        put("token", OtherUser.FcmToken)
                        put("notification", notificationObj)
                        put("data", dataObj)
                    })

                    Log.e("TAG", "sendNotification: =======data====Message" + jsonObject )

                    callApi(jsonObject)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun callApi(jsonObject: JSONObject) {
        val JSON = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/v1/projects/chat-blast-de68a/messages:send"
        val body = RequestBody.create(JSON, jsonObject.toString())
        val token = ""
        Log.e("TAG", "callApi: =======AccessToken====" + token )
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer $token")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // handle failure
                Log.e("TAG", "onFailure: ====Notification Failure=====", e)
            }

            override fun onResponse(call: Call, response: Response) {
                // handle response
                Log.e("TAG", "onResponse: =======onResponse of firebase notification======"+ response)
            }
        })
    }

//    private fun getAccessToken(context: Context): String {
//        val jsonContent = context.readJsonFromAssets("fcm.json")
//        if (jsonContent != null) {
//            println("success to read the JSON file from assets.")
//        } else {
//            println("Failed to read the JSON file from assets.")
//        }
//
//        val inputStream: InputStream = ByteArrayInputStream(jsonContent!!.toByteArray(StandardCharsets.UTF_8))
//        val googleCredentials: GoogleCredentials = GoogleCredentials
//            .fromStream(inputStream)
//            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
//
//        googleCredentials.refreshIfExpired()
//        return googleCredentials.accessToken.tokenValue
//    }
//
//    fun Context.readJsonFromAssets(fileName: String): String? {
//        return try {
//            val inputStream = assets.open(fileName)
//            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//            val stringBuilder = StringBuilder()
//            var line: String?
//
//            while (bufferedReader.readLine().also { line = it } != null) {
//                stringBuilder.append(line)
//            }
//
//            bufferedReader.close()
//            inputStream.close()
//            stringBuilder.toString()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }


    private fun getKeyboardHeight(contentView: View): Int {
        val r = Rect()
        contentView.getWindowVisibleDisplayFrame(r)
        return contentView.height - r.bottom
    }

    private fun adjustLayout(keyboardHeight: Int) {
        // Move EditText and SendButton upwards
        binding.linearLayout.translationY = -keyboardHeight.toFloat()

        // Adjust RecyclerView height to avoid overlapping with keyboard
        binding.recyclerChat.layoutParams = (binding.recyclerChat.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomMargin = keyboardHeight
        }
    }

    private fun resetLayout() {
        // Reset EditText and SendButton position
        binding.linearLayout.translationY = 0f

        // Reset RecyclerView height to its original value
        binding.recyclerChat.layoutParams = (binding.recyclerChat.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomMargin = 0
        }
    }

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("hh:mm", Locale.US)
        return formatter.format(currentTime)
    }
    fun setCurrentTime(textView: TextView) {
        val currentTime = getCurrentTime()
        textView.text = currentTime
    }
    fun updateCurrentTime(textView: TextView) {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                setCurrentTime(textView)
                handler.postDelayed(this, 1000) // update every 1 second
            }
        }, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_chat_heme_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.theme1 -> {

                activeTheme1()
                mUtility.writeSharedPreferencesString(CHATTHEME, "Theme1")
            }
            R.id.theme2 -> {

                activeTheme2()
                mUtility.writeSharedPreferencesString(CHATTHEME, "Theme2")
            }
            R.id.theme3 -> {

                activeTheme3()
                mUtility.writeSharedPreferencesString(CHATTHEME, "Theme3")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun activeTheme1(){
        binding.chatToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbarcolor1))
        binding.btnsendmessagechat.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sendbtncolor1))
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbarcolor1))
        binding.recyclerChat.setBackgroundResource(R.drawable.chat_background1)
        binding.linearLayout.setBackgroundResource(R.drawable.chat_background_bottom1)

        binding.linearLayout2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        binding.messageInputchat.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun activeTheme2(){
        binding.chatToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbarcolor2))
        binding.btnsendmessagechat.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sendbtncolor2))
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbarcolor2))
        binding.recyclerChat.setBackgroundResource(R.drawable.chat_background2)
        binding.linearLayout.setBackgroundResource(R.drawable.chat_background_bottom2)

        binding.linearLayout2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        binding.messageInputchat.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

    private fun activeTheme3(){
        binding.chatToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbarcolor3))
        binding.btnsendmessagechat.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sendbtncolor3))
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbarcolor3))
        binding.recyclerChat.setBackgroundResource(R.drawable.chat_background3)
        binding.linearLayout.setBackgroundResource(R.drawable.chat_background_bottom3)

        binding.linearLayout2.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        binding.messageInputchat.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        Animatoo.animateSwipeRight(this)
        finish()
    }
}