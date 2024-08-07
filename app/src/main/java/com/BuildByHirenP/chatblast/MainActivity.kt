package com.BuildByHirenP.chatblast

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.BuildByHirenP.chatblast.adapter.RecentRecyclerChatAdapter
import com.BuildByHirenP.chatblast.adapter.searchUserAdapter
import com.BuildByHirenP.chatblast.databinding.ActivityMainBinding
import com.BuildByHirenP.chatblast.model.ChatRoomModel
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.nordan.dialog.Animation
import com.nordan.dialog.DialogType
import com.nordan.dialog.NordanAlertDialog

class MainActivity : AppCompatActivity() {
    
    lateinit var binding : ActivityMainBinding
    var adapter : RecentRecyclerChatAdapter? = null
    lateinit var Token : String

    private var backPressedOnce = false
    private val exitToast by lazy {
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbarMainActivity)
        binding.toolbarMainActivity.setTitle(R.string.app_name)
        binding.toolbarMainActivity.setTitleTextColor(Color.parseColor("#000000"))

        setUpRecyclerView()
        getFCMToken()
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful){
                Token = it.getResult()
                FirebaseUtils.currentUserDetails()!!.update("fcmToken", Token)
                Log.e("TAG", "getFCMToken: =====fcm-Token====" + Token )
            }
        }
    }

    private fun setUpRecyclerView() {

        val query = FirebaseUtils.allChatRoomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtils.currentUserId()!!)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result.documents
                Log.e("TAG", "Documents size: ${documents.size}")
            } else {
                Log.e("TAG", "Error getting documents: ${task.exception}")
            }
        }

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query, ChatRoomModel::class.java)
            .build()

        adapter = RecentRecyclerChatAdapter(options, this)
        binding.recyclerviewUserchatMainactivity.adapter = adapter
        adapter!!.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_search_bar -> {

                val i = Intent(this, SearchUser::class.java)
                startActivity(i)
                Animatoo.animateFade(this)
            }
            R.id.menu_profile -> {

                val i = Intent(this, ProfileActivity::class.java)
                startActivity(i)
                Animatoo.animateFade(this)
            }
            R.id.menu_logout -> {

                NordanAlertDialog.Builder(this)
                    .setDialogType(DialogType.WARNING)
                    .setAnimation(Animation.SLIDE)
                    .isCancellable(false)
                    .setDialogAccentColor(R.color.brand_color)
                    .setTitle("Logout?")
                    .setMessage("Are You Sure You Want To LogOut?")
                    .setPositiveBtnText("Logout")
                    .onPositiveClicked {

                        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                            if (it.isSuccessful){
                                FirebaseUtils.Logout()

                                val intent = Intent(this, LoginAndRegisterActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                Animatoo.animateDiagonal(this)
                            }
                        }
                    }
                    .setNegativeBtnText("Cancle")
                    .onNegativeClicked {

                    }
                    .build()
                    .show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed()
            val i = Intent(Intent.ACTION_MAIN)
            i.addCategory(Intent.CATEGORY_HOME)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            return
        }

        backPressedOnce = true
        exitToast.show()

        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }
}