package com.BuildByHirenP.chatblast

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.BuildByHirenP.chatblast.databinding.ActivityProfileBinding
import com.BuildByHirenP.chatblast.model.UserModel
import com.BuildByHirenP.chatblast.utils.FirebaseUtils
import com.BuildByHirenP.chatblast.utils.Utility
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.github.dhaval2404.imagepicker.ImagePicker
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    lateinit var binding : ActivityProfileBinding

    private lateinit var currentUserModel: UserModel
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    lateinit var uploadedImgPreview : CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbarUserProfileActivity)
        binding.toolbarUserProfileActivity.setTitle("Profile")
        binding.toolbarUserProfileActivity.setTitleTextColor(Color.parseColor("#000000"))

        imagePickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    selectedImageUri = data.data
                    Utility.setProfilePic(this, selectedImageUri!!, uploadedImgPreview)
                }
            }
        }

        binding.toolbarUserProfileActivity.setNavigationOnClickListener {
            finish()
            Animatoo.animateFade(this)
        }

        binding.imgProfileEdit.setOnClickListener {
            profileUpdate()
        }

        getUserData()
    }

    private fun profileUpdate() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.raw_update_profile_user_name)

        val editname: EditText = dialog.findViewById(R.id.update_profile_user_name)
        val btnupdate: Button = dialog.findViewById(R.id.btn_update_user_name)
        val btnUploadImg : ImageView= dialog.findViewById(R.id.btn_upload_image)
        uploadedImgPreview = dialog.findViewById(R.id.img_update_profile)

        editname.setText(binding.textProfileUaerName.text.toString())
        FirebaseUtils.getCurrentProfilePicsStorageRef().downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri = task.result
                Utility.setProfilePic(this, uri, uploadedImgPreview)
            }
        }

        btnUploadImg.setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent { intent ->
                imagePickLauncher.launch(intent)
                Unit
            }
        }

        btnupdate.setOnClickListener {

            if (editname.text.toString().equals("") || editname.length() < 3) {
                editname.error = "Username length should be at least 3 chars"
                return@setOnClickListener
            }

            updateBtnClick(editname.text.toString())


            dialog.dismiss()
        }
        dialog.show()

    }

    private fun updateBtnClick(updatename : String) {

        currentUserModel.UserName = updatename.toString()
        setInProgress(true)

        if (selectedImageUri != null) {
            Log.e("TAG", "updateBtnClick: ====if-condition true====" )
            FirebaseUtils.getCurrentProfilePicsStorageRef().putFile(selectedImageUri!!)
                .addOnCompleteListener {
                    updateToFirestore()
                }
        } else {
            Log.e("TAG", "updateBtnClick: ====else====" )
            updateToFirestore()
        }
    }

    private fun updateToFirestore() {
        FirebaseUtils.currentUserDetails()!!.set(currentUserModel)
            .addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(intent)
                    Animatoo.animateFade(this)
                } else {
                    Toast.makeText(this, "Updates failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun getUserData() {
        setInProgress(true)

        FirebaseUtils.getCurrentProfilePicsStorageRef().downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uri = task.result
                Utility.setProfilePic(this, uri, binding.imgProfile)
            }
        }

        FirebaseUtils.currentUserDetails()!!.get().addOnCompleteListener { task ->
            setInProgress(false)
            currentUserModel = task.result.toObject(UserModel::class.java)!!
            binding.textProfileUaerName.setText(currentUserModel.UserName)
            binding.textProfilePhoneNumber.setText(currentUserModel.PhoneNumber)
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBarProfileActivity.visibility = View.VISIBLE
        } else {
            binding.progressBarProfileActivity.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        Animatoo.animateFade(this)
        finish()
    }
}