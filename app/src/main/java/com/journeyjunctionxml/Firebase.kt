package com.journeyjunctionxml
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class Firebase {
    companion object {

        var xx = 1
        var imageurl = "-1"
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore
        fun getCurrentUser(): FirebaseUser? {
            return auth.currentUser
        }
        fun get_docs_info(collection: String, documentt: String, callback: (Map<String, Any>?) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection(collection)
                    .document(documentt)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val documentData = document.data
                            callback(documentData)
                        } else {
                            callback(null) // Document does not exist
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure
                        callback(null)
                    }
            } else {
                callback(null)
            }
        }

        //Create Account
        fun createAccount(email: String, password: String, name: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = getCurrentUser()
                        if (currentUser != null) {
                            val userData = hashMapOf(
                                "email" to email,
                                "uid" to currentUser.uid,
                                "name" to name,
                                "password" to password
                            )
                            db.collection("users")
                                .document(currentUser.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot added with ID: ${currentUser.uid}")
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding document", e)
                                    onFailure(e)
                                }
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        onFailure(task.exception!!)
                    }
                }
        }
        //Sign In
        fun signInWithEmailPassword(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(task.exception!!)
                    }
                }
        }
        fun addFriend(uid: String, context: Context) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                var fname = "-1"
                var fprofile_pic = "-1"
                var myname = "-1"
                var myprofile_pic = "-1"

                // Counter to track completion of asynchronous operations
                var completedOperations = 0

                // Function to check if all data is retrieved successfully and proceed
                fun trySendingFriendRequest() {
                    if (fname == "-1" || fprofile_pic == "-1" || myname == "-1" || myprofile_pic == "-1") {
                        Log.d(TAG, "Cannot proceed with friend request.")
                        return
                    }

                    // Proceed with sending friend request as all data is valid
                    val fData = hashMapOf(
                        "uid" to uid,
                        "name" to fname,
                        "profile_pic_icon" to fprofile_pic
                    )
                    val myData = hashMapOf(
                        "uid" to currentUser.uid.toString(),
                        "name" to myname,
                        "profile_pic_icon" to myprofile_pic
                    )
                    db.collection("users")
                        .document(currentUser.uid)
                        .collection("friend_requests")
                        .document(uid) // Use the friend's UID as the document ID
                        .set(fData)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "Friend request sent successfully")
                            Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Friend request send failed", e)
                            Toast.makeText(context, "Friend request send failed", Toast.LENGTH_SHORT).show()
                        }
                    db.collection("users")
                        .document(uid)
                        .collection("pending_requests")
                        .document(currentUser.uid)
                        .set(myData)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "Friend request recieved successfully")
                        }
                }

                fun myinfo() {
                    get_docs_info("users", currentUser.uid) { data ->
                        if (data != null) {
                            myname = (data["name"] as? String).toString()
                            myprofile_pic = (data["profile_pictures"] as? String).toString()
                            trySendingFriendRequest()
                        }
                        else{
                            Toast.makeText(context, "Friend request send failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                // Fetch friend's info
                get_docs_info("users", uid) { data ->
                    if (data != null) {
                        fname = (data["name"] as? String).toString()
                        fprofile_pic = (data["profile_pictures"] as? String).toString()
                        myinfo()
                    }
                    else{
                        Toast.makeText(context, "Friend request send failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun uploadImageToFirestore(imageUri: Uri, context: Context,purpose: String,privacy: Int,caption: String) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Uploading")
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("${purpose}/${UUID.randomUUID()}")

            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.addOnProgressListener { snapshot ->
                progressDialog.setMessage("Uploading")
            }

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    storeImageUrlInFirestore(context,downloadUri.toString(),purpose,privacy,caption)
                    Log.d(TAG, "Download URL: $downloadUri")

                } else {
                    Log.e(TAG, "Failed to upload image")
                    Toast.makeText(context,"Image Upload failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
        fun logout(){
            auth.signOut()
        }
        fun storeImageUrlInFirestore(context: Context,imageUrl: String,collection: String,privacy: Int,caption: String) {
            var numberofProfilePicture = 0
            getDocumentSize(collection) { docSize ->
                Log.d(TAG,"photos: "+ docSize)
                numberofProfilePicture = docSize
                numberofProfilePicture = numberofProfilePicture + 1
                val profile_pic_name = "imageUrl_" + numberofProfilePicture.toString()
                val docData = hashMapOf(
                    profile_pic_name to imageUrl
                )
                val profilepic = hashMapOf(
                    collection to imageUrl
                )
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    db.collection(collection)
                        .document(currentUser.uid)
                        .set(
                            docData,
                            SetOptions.merge()
                        ) // Use SetOptions.merge() to merge with existing document
                        .addOnSuccessListener {

                            Log.d(TAG, "Image URL stored in Firestore for user: ${currentUser.uid}")
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                TAG,
                                "Error adding image URL to Firestore for user: ${currentUser.uid}",
                                e
                            )
                        }
                    if(collection == "profile_pictures") {
                        db.collection("users")
                            .document(currentUser.uid)
                            .set(
                                profilepic,
                                SetOptions.merge()
                            )
                            .addOnSuccessListener {
                                xx = 2
                                Log.d(TAG,"Image URL stored in Firestore for user: ${currentUser.uid}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG,"Error adding image URL to Firestore for user: ${currentUser.uid}",e)
                            }
                    }
                    else{
                        createPost(imageUrl, caption, privacy, context)
                    }
                } else {

                    Log.e(TAG, "Current user is null. Unable to store image URL in Firestore.")
                }
            }
        }
        fun getDocumentSize(collection: String,callback: (Int) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection(collection)
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val dataSize = document.data?.size ?: 0
                            callback(dataSize)
                        } else {
                            callback(0)
                        }
                    }
                    .addOnFailureListener { exception ->
                        callback(0)
                    }
            } else {
                callback(0)
            }
        }
        fun createPost(imageUrl: String, caption: String, privacy: Int, context: Context) {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val postData = hashMapOf(
                    "imageUrl" to imageUrl,
                    "caption" to caption,
                    "privacy" to privacy,
                    "react" to 0
                )
                db.collection("users")
                    .document(user.uid)
                    .collection("posts")
                    .add(postData)
                    .addOnSuccessListener { documentReference ->
                        val postId = documentReference.id
                        Log.d(TAG, "Post with ID: $postId added successfully for user: ${user.uid}")
                        Toast.makeText(context, "Post added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding Post to Firestore for user: ${user.uid}", e)
                        Toast.makeText(context, "Failed to add post", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        

    }
}