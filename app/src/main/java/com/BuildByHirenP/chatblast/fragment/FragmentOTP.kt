package com.BuildByHirenP.chatblast.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.BuildByHirenP.chatblast.MainActivity
import com.BuildByHirenP.chatblast.R
import com.BuildByHirenP.chatblast.databinding.FragmentOTPBinding
import com.BuildByHirenP.chatblast.utils.Constant
import com.BuildByHirenP.chatblast.utils.Utility
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.math.truncate

class FragmentOTP : Fragment() {

    private lateinit var binding: FragmentOTPBinding

    lateinit var mUtility : Utility
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var otp : String
    lateinit var VerificationCode : String
    lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    var timeOutSecond : Long = 60L
    lateinit var Phonenumber : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mActivity = activity
        mUtility = Utility(mActivity!!)
        Phonenumber = mUtility.getAppPrefString(Constant.PHONE_NUMBER).toString()
        otp = mUtility.getAppPrefString(Constant.OTP).toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOTPBinding.inflate(inflater, container, false)

//        sendOTP(Phonenumber, false)
//
//        binding.btnOTP.setOnClickListener {
//            var EnterdOTP : String = binding.editTextOTP.text.toString()
//
//            if (EnterdOTP.isEmpty()){
//                Toast.makeText(context, "Enter the OTP", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            var credential = PhoneAuthProvider.getCredential(VerificationCode, EnterdOTP)
//            signIn(credential)
//        }

        binding.textViewResendOtp.setOnClickListener {
//            sendOTP(Phonenumber, true)
        }

        childFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner, ::onFragmentResult)

        resendOTPTvVisibility()
        binding.textViewResendOtp.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }
        setInProgress(false)

        binding.btnOTP.setOnClickListener {
            //collect otp from all the edit texts
            var EnterdOTP : String = binding.editTextOTP.text.toString()

            if (EnterdOTP.isEmpty()){
                Toast.makeText(context, "Enter the OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                otp, EnterdOTP
            )
            setInProgress(true)
            signInWithPhoneAuthCredential(credential)

        }

        return binding.root
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        // Check if the request key matches the one we're expecting
        if (requestKey == "requestKey") {
            // Get the ForceResendingToken object from the result bundle
            resendToken = result.getParcelable<PhoneAuthProvider.ForceResendingToken>("resendToken")!!

        }
    }

    private fun resendOTPTvVisibility() {
//        binding.textViewResendOtp.visibility = View.GONE
        binding.textViewResendOtp.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            binding.textViewResendOtp.visibility = View.VISIBLE
            binding.textViewResendOtp.isEnabled = true
        }, 60000)
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(Phonenumber)       // Phone number to verify
            .setTimeout(timeOutSecond, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireContext() as Activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
            setInProgress(true)
            // Show a message and update the UI
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

            otp = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information

                    Toast.makeText(context, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    sendToMain()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
                setInProgress(false)
            }
    }

    private fun sendToMain() {
        findNavController().navigate(R.id.action_fragmentOTP_to_profileDetails)
    }


//    private fun sendOTP(phonenumber: String, isResend: Boolean) {
//        setInProgress(true)
////        startResendTimer()
//
//        val builder = PhoneAuthOptions.newBuilder(mAuth)
//            .setPhoneNumber(phonenumber)
//            .setTimeout(timeOutSecond, TimeUnit.SECONDS)
//            .setActivity(requireContext() as Activity)
//            .setCallbacks(callbacks)
//        if (isResend){
//            PhoneAuthProvider.verifyPhoneNumber(builder.build())
//        }else{
//            PhoneAuthProvider.verifyPhoneNumber(builder.build())
//        }
//
//    }

//    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//            Log.d("TAG", "onVerificationCompleted:$credential")
//            signIn(credential)
//            setInProgress(false)
//        }
//
//        override fun onVerificationFailed(e: FirebaseException) {
//            Log.w("TAG", "onVerificationFailed===11111", e)
//            setInProgress(false)
//
//            if(e is FirebaseAuthInvalidCredentialsException){
//                Log.e("TAG", "onVerificationFailed--2: ${e.toString()}")
//            }else if (e is FirebaseTooManyRequestsException){
//                Log.e("TAG", "onVerificationFailed--3: ${e.toString()}")
//            }
//
//            Toast.makeText(context, "OTP verification faild, please try again later", Toast.LENGTH_SHORT).show()
//
//        }
//
//        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//            Log.d("TAG", "onCodeSent:$verificationId")
//
//            VerificationCode = verificationId
//            resendToken = token
//            Toast.makeText(context, "OTP Send Successfully", Toast.LENGTH_SHORT).show()
//            setInProgress(false)
//        }
//    }

    private fun signIn(cradential : PhoneAuthCredential){
        setInProgress(true)

        mAuth.signInWithCredential(cradential)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    findNavController().navigate(R.id.action_fragmentOTP_to_profileDetails)
                    setInProgress(false)
                }else{
                    Toast.makeText(context, "OTP is Invalid", Toast.LENGTH_SHORT).show()
                    setInProgress(false)
                }
            }
    }

    private fun setInProgress(inProcess: Boolean) {
        if (inProcess){
            binding.progressBarOTP.visibility = View.VISIBLE
            binding.btnOTP.visibility = View.GONE
        }else{
            binding.progressBarOTP.visibility = View.GONE
            binding.btnOTP.visibility = View.VISIBLE
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    private fun startResendTimer() {
        binding.textViewResendOtp.isEnabled = false
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                timeOutSecond--
                val text = "Resend OTP in " + timeOutSecond + " seconds"
                handler.post {
                    binding.textViewResendOtp.text = text
                }

                if (timeOutSecond <= 0) {
                    timeOutSecond = 60L
                    timer.cancel()
                    handler.post {
                        binding.textViewResendOtp.isEnabled = true
                        binding.textViewResendOtp.text = "Click to resend OTP"
//                        binding.textViewResendOtp.setTextColor(Color.parseColor(R.color.chat_color_sender.toString()))
                    }
                }
            }
        }, 0, 1000)
    }
}