package com.BuildByHirenP.chatblast

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.BuildByHirenP.chatblast.adapter.searchUserAdapter
import com.BuildByHirenP.chatblast.databinding.ActivitySearchUserBinding
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

class SearchUser : AppCompatActivity() {

    lateinit var binding : ActivitySearchUserBinding
    var adapter : searchUserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val searchname = binding.editTextSearch.text.toString()

        binding.toolbarSearchUserActivity.setTitle(R.string.search_user)
        binding.toolbarSearchUserActivity.setTitleTextColor(Color.parseColor("#000000"))
        binding.editTextSearch.requestFocus()

        binding.editTextSearch.addTextChangedListener( object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // this function is called before text is edited
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // this function is called when text is edited
                if (s.length < 3){
                    binding.editTextSearch.setError("Invalid Name")
                    return
                }

                setUpSearchRecyclerView(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
                // this function is called after text is edited
            }
        } )

        binding.imageviewSearch.setOnClickListener {

            if (searchname.isEmpty()){
                Toast.makeText(this, "Please, Enter Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (searchname.length < 3){
                binding.editTextSearch.setError("Invalid Name")
                return@setOnClickListener
            }

            setUpSearchRecyclerView(searchname)
        }
    }

    private fun setUpSearchRecyclerView(searchname: String) {

        val query : Query = FirebaseUtils.allUserCollectionReference()
            .whereGreaterThanOrEqualTo("userName", searchname)
            .whereLessThanOrEqualTo("userName", searchname + "\uf88ff")

        val options = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java)
            .build()

        adapter = searchUserAdapter(options, this)
        binding.recyclerviewSearch.adapter = adapter
        adapter!!.startListening()
    }

//    override fun onStart() {
//        super.onStart()
//        if (adapter != null){
//            adapter!!.startListening()
//        }else{
//            Log.e("TAG", "onStart: =====adapter is null " )
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (adapter != null){
//            adapter!!.stopListening()
//        }else{
//            Log.e("TAG", "onStart: =====adapter is null " )
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (adapter != null){
//            adapter!!.startListening()
//        }else{
//            Log.e("TAG", "onStart: =====adapter is null " )
//        }
//    }
    
    override fun onBackPressed() {
        super.onBackPressed()

        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        Animatoo.animateFade(this)
        finish()
    }
}