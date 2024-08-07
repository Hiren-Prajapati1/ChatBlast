package com.BuildByHirenP.chatblast

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo

class SplaceScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splace_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        Log.e("TAG", "onCreate: ====11111111===="+ intent.extras )
        if (FirebaseUtils.isLoggedIn() && intent.extras?.getString("userId") != null){
            // from notification
            Log.e("TAG", "onCreate: ===22222222222====="+ intent.extras!! )
            Log.e("TAG", "onCreate: ====33333333333===="+ intent.extras!!.getString("userId") )
            var UserId : String = intent.extras!!.getString("userId")!!
            FirebaseUtils.allUserCollectionReference().document(UserId).get()
                .addOnCompleteListener {
                    if (it.isSuccessful){

                        var model : UserModel = it.getResult().toObject(UserModel::class.java)!!

                        val intent = Intent(this, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)

                        val i = Intent(this, ChatActivity::class.java)
                        Utility.passUserModelAsIntent(i, model)
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                        Animatoo.animateZoom(this)

                        finish()
                    }
                }

        }else{
            Handler(Looper.getMainLooper()).postDelayed({

                if(FirebaseUtils.isLoggedIn()){
                    startActivity(Intent(this@SplaceScreen , MainActivity::class.java))
                }else{
                    startActivity(Intent(this@SplaceScreen , LoginAndRegisterActivity::class.java))
                }
                finish()

                Animatoo.animateZoom(this)

            }, 2000)
        }
    }
}