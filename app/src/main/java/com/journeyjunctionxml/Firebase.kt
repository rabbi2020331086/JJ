package com.journeyjunctionxml
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Arrays
import java.util.Locale
import java.util.UUID

class Firebase {
    companion object {
        var idtype = "-1"
        var name = "-1"
        var postList = mutableListOf<Pair<String, Map<String, Any>>>()
        var pending_req: MutableList<Map<String, Any>> = mutableListOf()
        var sent_req: MutableList<Map<String, Any>> = mutableListOf()
        var friends: MutableList<Map<String, Any>> = mutableListOf()
        var xx = 1
        var imageurl = "-1"
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore
        fun getCurrentUser(): FirebaseUser? {
            return auth.currentUser
        }
//        fun sort() {
//            val sortedPostList = postList.sortedBy { pair ->
//                val timestamp = pair.second["timestamp"] as? Long
//                timestamp ?: Long.MAX_VALUE // Default to a very large value if timestamp is null
//            }
//
//            sortedPostList.forEach { pair ->
//                val postId = pair.first
//                val postData = pair.second
//                val timestamp = postData["timestamp"] as? Long // Safe cast using `as?`
//                if (timestamp != null) {
//                    println("Post ID: $postId, Timestamp: $timestamp")
//                } else {
//                    println("Post ID: $postId, Timestamp: null")
//                }
//            }
//        }


        fun getPost(context: Context, uid: String, privacy: Int, onCompleted: (Boolean) -> Unit) {
            val progressDialog = ProgressDialog(context).apply {
                setMessage("Loading...")
                isIndeterminate = true
                setCancelable(false)
                show()
            }
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        val documents = Firebase.firestore
                            .collection("users")
                            .document(uid)
                            .collection("posts")
                            .get()
                            .await()

                        documents.forEach { document ->
                            Log.d(TAG, "bokasoda")
                            val postPrivacy = document.getDouble("privacy")?.toInt()
                            if (postPrivacy == privacy) {
                                val postId = document.id
                                val postData = document.data.toMutableMap()
                                postData["postId"] = postId
                                if (postData["imageUrl"] != null) {
                                    val uri: Uri? = withContext(Dispatchers.IO) {
                                        downloadImageUri(postData["imageUrl"].toString())
                                    }
                                    postData["imageUrl"] = uri
                                } else {
                                    postData["imageUrl"] = null
                                }

                                postList.add(Pair(postId, postData))
                                Log.d(TAG, "Post ID: $postId, Data: $postData")
                            }
                        }
                        onCompleted(true)
                        progressDialog.dismiss()
                    } catch (exception: Exception) {
                        Log.w(TAG, "Error getting documents: ", exception)
                        onCompleted(false)
                        progressDialog.dismiss()
                    }
                }
            }
        }



        fun getPendingreq() {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("sen")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val requestId = document.id
                            val requestData = document.data
                            pending_req.add(requestData)
                            Log.d(TAG, "Request ID: $requestId, Data: $requestData")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting pending requests: ", exception)
                    }
            }
        }
        fun getSentReq() {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("friend_requests")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val requestId = document.id
                            val requestData = document.data
                            sent_req.add(requestData)
                            Log.d(TAG, "Request ID: $requestId, Data: $requestData")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting sent requests: ", exception)
                    }
            }
        }
        fun clearFriends() {
            friends.clear()
        }
        fun checkIfFriendExists(uidToCheck: String,onCompleted: (Boolean) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("friends")
                    .document(uidToCheck)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Friend $uidToCheck exists for user ${currentUser.uid}")
                            onCompleted(true)
                        } else {
                            // The friend UID does not exist in the "friends" collection
                            onCompleted(false)
                            Log.d(TAG, "Friend $uidToCheck does not exist for user ${currentUser.uid}")
                            // You can perform further actions here
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error checking friend existence: ", exception)
                    }
            }
        }
        fun getJourneybySearch(namePrefix: String, type: String, onCompleted: (List<TripsModel>) -> Unit) {
            val lowercasePrefix = namePrefix.toLowerCase(Locale.ROOT)
            Log.d(TAG, "$type search type")
            Log.d(TAG, "Function invoked")

            db.collection("journeys")
                .orderBy(type)
                .startAt(lowercasePrefix)
                .endAt(lowercasePrefix + "\uf8ff")
                .get()
                .addOnSuccessListener { documents ->
                    val userList = mutableListOf<TripsModel>()
                    for (document in documents) {
                        try {
                            val journey = document.toObject(TripsModel::class.java)
                            userList.add(journey)
                            Log.d(TAG, journey.toString())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deserializing document ${document.id}", e)
                            // Handle or log error as needed, e.g., incorrect data types
                        }
                    }
                    Log.d(TAG, "Documents fetched successfully")
                    onCompleted(userList)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching documents", exception)
                    onCompleted(emptyList())
                }
        }

        fun getUsersByName(namePrefix: String, onComplete: (List<search_users_model>) -> Unit) {
            val lowerCaseNamePrefix = namePrefix.toLowerCase()

            db.collection("users")
                .orderBy("name_lowercase")
                .startAt(lowerCaseNamePrefix)
                .endAt(lowerCaseNamePrefix + "\uf8ff")
                .get()
                .addOnSuccessListener { documents ->
                    val userList = mutableListOf<search_users_model>()
                    for (document in documents) {
                        val uid = document.id
                        val userName = document.getString("name") ?: ""
                        val user = search_users_model(uid, userName)
                        userList.add(user)
                    }
                    onComplete(userList)
                }
                .addOnFailureListener { exception ->
                    onComplete(emptyList())
                }
        }
        fun getFriends(context: Context, onCompleted: (Boolean) -> Unit) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("friends")
                    .get()
                    .addOnSuccessListener { documents ->
                        progressDialog.dismiss()

                        for (document in documents) {
                            val requestId = document.id
                            val requestData = document.data
                            friends.add(requestData)
                            Log.d(TAG, "Request ID: $requestId, Data: $requestData")
                        }
                        onCompleted(true)
                    }
                    .addOnFailureListener { exception ->
                        progressDialog.dismiss()

                        Log.w(TAG, "Error getting Friends ", exception)
                        onCompleted(false)
                    }
            } else {
                progressDialog.dismiss()

                onCompleted(false)
            }
        }


        fun add_journeyID_to_users_collection(journeyID: String, onComplete: (Boolean) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                val data = hashMapOf(
                    "journeyID" to journeyID
                )
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("own_journey")
                    .add(data)
                    .addOnSuccessListener { documentReference ->
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false)
                    }
            }
        }
        fun create_journey(tripDetailsList: List<String>, callback: (String?) -> Unit) {
            val dest = tripDetailsList[1].toLowerCase()
            val check_in = tripDetailsList[5].toLowerCase()
            val currentuser = getCurrentUser()
            if (currentuser != null) {
                val tripDetailsMap = hashMapOf(
                    "picture" to "nothing",
                    "title" to tripDetailsList[0],
                    "places" to tripDetailsList[1],
                    "date" to tripDetailsList[2],
                    "endDateTime" to tripDetailsList[3],
                    "description" to tripDetailsList[4],
                    "checkpoints" to tripDetailsList[5],
                    "budget" to tripDetailsList[6],
                    "destination_search" to dest,
                    "check_in" to check_in,
                    "capacity" to tripDetailsList[7],
                    "gender" to tripDetailsList[8],
                    "owner" to currentuser.uid
                )
                db.collection("journeys")
                    .add(tripDetailsMap)
                    .addOnSuccessListener { documentReference ->
                        val journeyID = documentReference.id
                        Log.d(TAG, "Trip details added successfully with ID: $journeyID")
                        callback(journeyID)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding trip details", e)
                        callback(null)
                    }
            } else {
                callback(null)
            }
        }

        fun search_tour(){

        }
        fun get_upcoming_tour(uid: String,onComplete: (List<TripsModel>) -> Unit){
            db.collection("users").document(uid).collection("upcoming_tour")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val tripsList = mutableListOf<TripsModel>()

                        tripsList.add(
                            TripsModel(
                                picture = "dummy_picture_url_1",
                                title = "Trip 1",
                                duration = "5 days",
                                date = "2024-04-15",
                                places = "City A, City B",
                                budget = "1000",
                                check_in = "Hotel X",
                                gender = "female",
                                vacancy = "5 (out of 10)",
                                uid = "user_id_1",
                                owner = "sjvujsv67stfs",
                            )
                        )

                        tripsList.add(
                            TripsModel(
                                picture = "dummy_picture_url_2",
                                title = "Trip 2",
                                duration = "3 days",
                                date = "2024-05-20",
                                places = "City C, City D",
                                budget = "200",
                                check_in = "Hotel Y",
                                gender = "female",
                                vacancy = "8 (out of 10)",
                                uid = "user_id_2",
                                owner = "rtdbdf457478fggh"
                            )
                        )

                        for (document in task.result ?: emptyList()) {
                            val trip = TripsModel(
                                picture = document.getString("picture") ?: "",
                                title = document.getString("title") ?: "",
                                duration = document.getString("duration") ?: "",
                                date = document.getString("date") ?: "",
                                places = document.getString("places") ?: "",
                                budget = document.getString("budget") ?: "",
                                check_in = document.getString("check_in") ?: "",
                                vacancy = document.getString("vacancy") ?: "",
                                uid = document.getString("uid") ?: "",
                                gender = document.getString("gender") ?: "",
                                owner = document.getString("owner") ?: ""
                            )
                            tripsList.add(trip)
                        }
                        onComplete(tripsList)
                    } else {
                        task.exception?.let {
                            Log.w("Error", "Error getting documents: ", it)
                        }
                        onComplete(emptyList())
                    }
                }
                .addOnFailureListener {
                    onComplete(emptyList())
                }
        }
        fun get_past_tour(uid: String,onComplete: (List<TripsModel>) -> Unit){
            db.collection("users").document(uid).collection("past_tour")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val tripsList = mutableListOf<TripsModel>()
                        tripsList.add(
                            TripsModel(
                                picture = "dummy_picture_url_1",
                                title = "Trip 1",
                                duration = "5 days",
                                date = "2024-04-15",
                                places = "City A, City B",
                                budget = "1000",
                                check_in = "Hotel X",
                                gender = "male and female",
                                owner = "sjvujsv67stfs",
                                vacancy = "5 (out of 10)",
                                uid = "user_id_1"
                            )
                        )

                        tripsList.add(
                            TripsModel(
                                picture = "dummy_picture_url_2",
                                title = "Trip 2",
                                duration = "3 days",
                                date = "2024-05-20",
                                gender = "male",
                                owner = "sjvujsv67stfs",
                                places = "City C, City D",
                                budget = "800",
                                check_in = "Hotel Y",
                                vacancy = "8 (out of 10)",
                                uid = "user_id_2"
                            )
                        )
                        for (document in task.result ?: emptyList()) {
                            val trip = TripsModel(
                                picture = document.getString("picture") ?: "",
                                title = document.getString("title") ?: "",
                                duration = document.getString("duration") ?: "",
                                date = document.getString("date") ?: "",
                                places = document.getString("places") ?: "",
                                budget = document.getString("budget") ?: "",
                                check_in = document.getString("check_in") ?: "",
                                vacancy = document.getString("vacancy") ?: "",
                                uid = document.getString("uid") ?: "",
                                owner = document.getString("owner") ?: "",
                                gender = document.getString("gender") ?: ""
                            )
                            tripsList.add(trip)
                        }
                        onComplete(tripsList)
                    } else {
                        task.exception?.let {
                            Log.w("Error", "Error getting documents: ", it)
                        }
                        onComplete(emptyList())
                    }
                }
                .addOnFailureListener {
                    onComplete(emptyList())
                }
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
        fun createAccount(email: String, password: String, name: String,type: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = getCurrentUser()
                        if (currentUser != null) {
                            this.name = name
                            val userData = hashMapOf(
                                "name_lowercase" to name.toLowerCase(),
                                "email" to email,
                                "uid" to currentUser.uid,
                                "name" to name,
                                "password" to password,
                                "type" to type
                            )
                            db.collection("users")
                                .document(currentUser.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    val user_mail = hashMapOf(
                                        "uid" to currentUser.uid
                                    )
                                    Log.d(TAG, "DocumentSnapshot added with ID: ${currentUser.uid}")
                                    db.collection("users_by_email")
                                        .document(email)
                                        .set(user_mail)
                                        .addOnCompleteListener{
                                            Log.d(TAG, "Email added with ID: ${currentUser.uid}")
                                        }
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
                var completedOperations = 0
                fun trySendingFriendRequest() {
                    if (fname == "-1" || fprofile_pic == "-1" || myname == "-1" || myprofile_pic == "-1") {
                        Log.d(TAG, "Cannot proceed with friend request.")
                        return
                    }
                    val fData = hashMapOf(
                        "uid" to uid,
                        "name" to fname,
                        "profile_pic_icon" to fprofile_pic
                    )
                    val myData = hashMapOf(
                        "uid" to currentUser.uid,
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
                    "name" to this.name,
                    "owner" to currentUser.uid,
                    "imageUrl" to imageUrl,
                    "caption" to caption,
                    "privacy" to privacy,
                    "react" to 0,
                    "timestampField" to FieldValue.serverTimestamp()
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
        suspend fun downloadBitmap(imageUrl: String): Bitmap {
            val urlConnection = URL(imageUrl).openConnection() as HttpURLConnection
            try {
                urlConnection.connect()
                val inputStream: InputStream = urlConnection.inputStream
                return BitmapFactory.decodeStream(inputStream)
            } finally {
                urlConnection.disconnect()
            }
        }

        suspend fun downloadImageUri(imageUrl: String): Uri? {
            return withContext(Dispatchers.IO) {
                try {
                    val url = URL(imageUrl)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000
                    connection.instanceFollowRedirects = true

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG,"uri done")
                        val inputStream = BufferedInputStream(connection.inputStream)
                        return@withContext Uri.parse(imageUrl)
                    } else {
                        // Handle other response codes if needed
                        return@withContext null
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    return@withContext null
                }
            }
        }



    }
}