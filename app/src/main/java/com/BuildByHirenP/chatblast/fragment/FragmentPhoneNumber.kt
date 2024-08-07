package com.BuildByHirenP.chatblast.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.BuildByHirenP.chatblast.MainActivity
import com.BuildByHirenP.chatblast.R
import com.BuildByHirenP.chatblast.databinding.FragmentPhoneNumberBinding
import com.BuildByHirenP.chatblast.utils.Constant.OTP
import com.BuildByHirenP.chatblast.utils.Constant.PHONE_NUMBER
import com.BuildByHirenP.chatblast.utils.Utility
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class FragmentPhoneNumber : Fragment() {

    private var _binding: FragmentPhoneNumberBinding? = null
    private val binding get() = _binding!!

    lateinit var mUtility : Utility
    private var PhoneNumber : String? = null
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mActivity = activity
        mUtility = Utility(mActivity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)

        binding.countryCodePicker.registerCarrierNumberEditText(binding.editTextNumber)

        binding.editTextNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!binding.countryCodePicker.isValidFullNumber){
                    binding.textInputLayoutNumber.error = "Please, Enter Valid Number"
                    binding.btnVerifyNumber.isEnabled = false
                }else{
                    binding.textInputLayoutNumber.error = ""
                    binding.btnVerifyNumber.isEnabled = true
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
        binding.btnVerifyNumber.setOnClickListener {
            PhoneNumber = binding.countryCodePicker.selectedCountryCodeWithPlus.toString().trim() + binding.editTextNumber.text.toString().trim()

            Log.e("TAG", "onCreateView: =======1======" + PhoneNumber )

            if (binding.editTextNumber.text.toString().isEmpty()){
                val snackbar = Snackbar
                    .make(requireView(), "Please, enter Phone number", Snackbar.LENGTH_LONG)
                snackbar.show()
                return@setOnClickListener
            }


//            mUtility.writeSharedPreferencesString(PHONE_NUMBER, PhoneNumber.toString())
//            findNavController().navigate(R.id.action_fragmentPhoneNumber_to_fragmentOTP)

            binding.progressBarPhonenumber.visibility =View.VISIBLE

            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(PhoneNumber.toString())       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireContext() as Activity)                 // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

        }

        return binding.root
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(context , "Authenticate Successfully" , Toast.LENGTH_SHORT).show()
                    sendToMain()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${it.exception.toString()}")
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
                binding.progressBarPhonenumber.visibility = View.INVISIBLE
            }
    }

    private fun sendToMain(){
        startActivity(Intent(context , MainActivity::class.java))
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                binding.progressBarPhonenumber.visibility = View.INVISIBLE
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                binding.progressBarPhonenumber.visibility = View.INVISIBLE
            }
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Toast.makeText(context , "OTP Send Successfully" , Toast.LENGTH_SHORT).show()
            PhoneNumber = binding.countryCodePicker.selectedCountryCodeWithPlus.toString().trim() + binding.editTextNumber.text.toString().trim()

            mUtility.writeSharedPreferencesString(OTP, verificationId)
            mUtility.writeSharedPreferencesString(PHONE_NUMBER, PhoneNumber.toString())

            val bundle = bundleOf("resendToken" to token)
            setFragmentResult("requestKey", bundle)

            findNavController().navigate(R.id.action_fragmentPhoneNumber_to_fragmentOTP)
            binding.progressBarPhonenumber.visibility = View.INVISIBLE
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}