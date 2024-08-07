package com.BuildByHirenP.chatblast.utils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

class FirebaseUtils {

    companion object{

        fun currentUserId(): String? {
            return FirebaseAuth.getInstance().uid
        }

        fun currentUserDetails() : DocumentReference? {
            return FirebaseFirestore.getInstance().collection("users").document(currentUserId()!!)
        }

        fun isLoggedIn() : Boolean{
            if (currentUserId() != null){
                return true
            }
            return false
        }

        fun Logout() {
            FirebaseAuth.getInstance().signOut()
        }

        fun allUserCollectionReference() : CollectionReference{
            return FirebaseFirestore.getInstance().collection("users")
        }

        fun getChatRoomReference(chatroomId : String) : DocumentReference {
            return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
        }

        fun getChatRoomMessageReference(chatroomId : String) : CollectionReference{
            return getChatRoomReference(chatroomId).collection("chats")
        }

        fun getChatRoomId(userId1 : String? , userId2 : String?) : String {
            if(userId1.hashCode() < userId2.hashCode()){
                return userId1 + "_" + userId2
            }else{
                return userId2 + "_" + userId1
            }
        }

        fun allChatRoomCollectionReference() : CollectionReference{
            return FirebaseFirestore.getInstance().collection("chatrooms")
        }

        fun getOtherUserFromChatRoom(userIds : List<String?>?) : DocumentReference{
            if (userIds!!.get(0).equals(FirebaseUtils.currentUserId())){
                return allUserCollectionReference().document(userIds!!.get(1)!!)
            }else{
                return allUserCollectionReference().document(userIds!!.get(0)!!)
            }
        }

        fun TimeStampToString(timeStamp : Timestamp) : String{
            return SimpleDateFormat("hh:mm a").format(timeStamp.toDate())
        }

        fun getCurrentProfilePicsStorageRef() : StorageReference{
            return FirebaseStorage.getInstance().getReference().child("profile_pics")
                .child(FirebaseUtils.currentUserId().toString())
        }

        fun getOtherProfilePicsStorageRef(otherUserId : String) : StorageReference{
            return FirebaseStorage.getInstance().getReference().child("profile_pics")
                .child(otherUserId)
        }
    }
}