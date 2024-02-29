package com.journeyjunctionxml
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
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
import java.util.UUID

class Firebase {

    companion object {
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
                    Log.d(TAG,"Heda: " + collection)
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
                    "uid" to user.uid,
                    "privacy" to privacy,
                    "react" to 0
                )
                db.collection("posts")
                    .document(user.uid)
                    .set(postData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Post URL stored in Firestore for user: ${user.uid}")
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