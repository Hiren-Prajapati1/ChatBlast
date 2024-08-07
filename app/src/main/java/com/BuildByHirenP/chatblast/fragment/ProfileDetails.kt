package com.BuildByHirenP.chatblast.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.BuildByHirenP.chatblast.MainActivity
import com.BuildByHirenP.chatblast.R
import com.BuildByHirenP.chatblast.databinding.FragmentProfileDetailsBinding
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.Constant
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.firebase.Timestamp


class ProfileDetails : Fragment() {

    lateinit var binding : FragmentProfileDetailsBinding

    lateinit var mUtility : Utility
    lateinit var usermodel: UserModel
    lateinit var Username : String
    lateinit var Phonenumber : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mActivity = activity
        mUtility = Utility(mActivity!!)
        Phonenumber = mUtility.getAppPrefString(Constant.PHONE_NUMBER).toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileDetailsBinding.inflate(inflater, container, false)

        val focusListener =
            OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.border_active)
                } else {
                    v.setBackgroundResource(R.drawable.border)
                }
            }

        binding.editTextProfileUserName.setOnFocusChangeListener(focusListener)

        getUserName()

        binding.btnSubmit.setOnClickListener {
            setUserName()
        }

        return binding.root
    }

    private fun setUserName() {

        Username = binding.editTextProfileUserName.text.toString().trim()
        if (Username.isEmpty()){
            Toast.makeText(context, "Please, enter your username", Toast.LENGTH_SHORT).show()
            return
        }

        setInProgress(true)

        if (usermodel != null){
            Log.e("TAG", "setUserName: ======call=====11111111111==" + Phonenumber )
            usermodel.UserName = Username
            usermodel.PhoneNumber = Phonenumber
            usermodel.CreatedTimeStamp = Timestamp.now()
            usermodel.UserId = FirebaseUtils.currentUserId().toString()
        }else {
            Log.e("TAG", "setUserName: ======call=====222222222==" + Phonenumber )
            usermodel = UserModel(Phonenumber, Username, Timestamp.now(), FirebaseUtils.currentUserId().toString(), "")
        }

        FirebaseUtils.currentUserDetails()!!.set(usermodel)
            .addOnCompleteListener {
                setInProgress(false)
                if(it.isSuccessful){
                    findNavController().navigate(R.id.action_profileDetails_to_mainActivity)

                }else{
                    Log.e("TAG", "setUserName: =====error on navigate to main activity===")
                }
            }
    }

    private fun getUserName() {
        setInProgress(true)

        FirebaseUtils.currentUserDetails()!!.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setInProgress(false)
                    val result = task.result
                    if (result != null && result.data != null) {
                        usermodel = result.toObject(UserModel::class.java)!!
                        if (usermodel != null) {
                            binding.editTextProfileUserName.setText(usermodel.UserName)
                        } else {
                            Log.e("TAG", "getUserName: =====user Not registered====")
                        }
                    } else {
                        Log.e("TAG", "getUserName: =====task result or data is null====")
                        usermodel = UserModel("", "", Timestamp.now(), "", "")
                    }
                } else {
                    Toast.makeText(context, "Get method Problem", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setInProgress(inProcess: Boolean) {
        if (inProcess){
            binding.progressBarProfileDetils.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        }else{
            binding.progressBarProfileDetils.visibility = View.GONE
            binding.btnSubmit.visibility = View.VISIBLE
        }
    }
}