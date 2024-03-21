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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
class Firebase {
    companion object {
        var idtype = "-1"
        var name = "-1"
        var xx = 1
        var imageurl = "-1"
        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        @SuppressLint("StaticFieldLeak")
        private val db = Firebase.firestore
        fun getCurrentUser(): FirebaseUser? {
            return auth.currentUser
        }
        fun loadJourney(context: Context, uid: String, onCompleted: (MutableMap<String, String>) -> Unit) {
            db.collection("journeys")
                .document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val dataMap = mutableMapOf<String, String>()
                    dataMap["roadmap"] = documentSnapshot.getString("roadmap") ?: ""
                    dataMap["date"] = documentSnapshot.getString("date") ?: ""
                    dataMap["description"] = documentSnapshot.getString("description") ?: ""
                    dataMap["title"] = documentSnapshot.getString("title") ?: ""
                    dataMap["introduction"] = documentSnapshot.getString("introduction") ?: ""
                    dataMap["destination"] = documentSnapshot.getString("places") ?: ""
                    dataMap["checkpoints"] = documentSnapshot.getString("checkpoints") ?: ""
                    dataMap["highlights"] = documentSnapshot.getString("highlights") ?: ""
                    onCompleted(dataMap)
                }
        }
        fun isReacted(uid: String, myid: String, pid: String, onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(uid)
                .collection("posts")
                .document(pid)
                .collection("reacted")
                .document(myid)
                .get()
                .addOnSuccessListener { ishere ->
                    if(ishere.exists())
                        onCompleted(true)
                    else
                        onCompleted(false)
                }
                .addOnFailureListener { e ->
                    onCompleted(false)
                }
        }
        fun isEmailVerified(onResult: (Boolean) -> Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isVerified = currentUser.isEmailVerified
                    onResult(isVerified)
                } else {
                    onResult(false)
                }
            }
        }

        fun getPost(uid: String, onComplete: (List<PostModel>) -> Unit) {
            db.collection("users")
                .document(uid)
                .collection("posts")
                .get()
                .addOnSuccessListener { posts ->
                    val list = mutableListOf<PostModel>()
                    for (document in posts) {
                        val createTime = document.getTimestamp("timestampField")?.toDate() ?: Date()
                        val pp = PostModel(
                            profileName = document.getString("name") ?: "",
                            contentCaption = document.getString("caption") ?: "",
                            contentImage = document.getString("imageUrl") ?: "",
                            reactCount = document.getString("react").toString() ?: "",
                            uid = document.getString("owner") ?: "",
                            pid = document.id,
                            time = createTime
                        )
                        list.add(pp)
                    }
                    list.sortByDescending { it.time }
                    onComplete(list)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting posts for user $uid", exception)
                    onComplete(emptyList())
                }
        }

        fun updateJourneyField(context: Context,uid: String, field: String, text: String){
            val userData = hashMapOf(
                field to text
            )
            db.collection("journeys")
                .document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(context, "Update successfully done.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Update failed!.", Toast.LENGTH_SHORT).show()
                }
        }
        fun getPendingreq(uid: String, onComplete: (List<search_users_model>) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("pending_requests")
                    .get()
                    .addOnSuccessListener { documents ->
                        val list = mutableListOf<search_users_model>()
                        for (document in documents) {
                            val uid = document.getString("uid") ?: ""
                            val name = document.getString("name") ?: ""
                            if (uid.isNotEmpty() && name.isNotEmpty()) {
                                list.add(search_users_model(uid, name))
                            }
                        }
                        onComplete(list)
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting pending requests: ", exception)
                        onComplete(emptyList()) // Optionally call onComplete with an empty list in case of failure
                    }
            }
        }

        fun getFriends(type: String,onComplete: (List<search_users_model>) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection(type)
                    .get()
                    .addOnSuccessListener { documents ->
                        val list = mutableListOf<search_users_model>()
                        for (document in documents) {
                            val uid = document.getString("uid") ?: ""
                            val name = document.getString("name") ?: ""
                            if (uid.isNotEmpty() && name.isNotEmpty()) {
                                list.add(search_users_model(uid, name))
                            }
                        }
                        onComplete(list) // Call onComplete with the list
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting pending requests: ", exception)
                        onComplete(emptyList()) // Optionally call onComplete with an empty list in case of failure
                    }
            }
        }
        fun checkIfMemberExist(type: String, journeyID: String, onCompleted: (Boolean) -> Unit){
            db.collection("journeys")
                .document(journeyID)
                .collection(type)
                .get()
                .addOnSuccessListener { docs ->
                    // Check if the size of the docs list is greater than 0
                    if (!docs.isEmpty) {
                        onCompleted(true) // Documents exist
                    } else {
                        onCompleted(false) // No documents found
                    }
                }
                .addOnFailureListener {
                    onCompleted(false) // Error occurred, assume no documents found
                }
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
                        if (documentSnapshot.exists() && documentSnapshot != null) {
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


        fun FriendRequest(from: String,to: String,myid: String,uid: String, onCompleted: (Boolean) -> Unit) {
            val userRef = db.collection("users").document(myid)
            val requestDocRef = userRef.collection(from).document(uid)

            requestDocRef.get().addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    onCompleted(false)
                    return@addOnSuccessListener
                }
                val friendData = documentSnapshot.data ?: return@addOnSuccessListener onCompleted(false)
                val friendDocRef = userRef.collection(to).document(uid)

                db.runTransaction { transaction ->
                    transaction.set(friendDocRef, friendData)
                    transaction.delete(requestDocRef)
                    null
                }.addOnSuccessListener {
                    onCompleted(true)
                }.addOnFailureListener { e ->
                    onCompleted(false)
                }
            }.addOnFailureListener { e ->
                onCompleted(false)
            }
        }
        fun checkIfSentFriendExists(uidToCheck: String,onCompleted: (Boolean) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("friend_requests")
                    .document(uidToCheck)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists() && documentSnapshot != null) {
                            onCompleted(true)
                        } else {
                            // The friend UID does not exist in the "friends" collection
                            onCompleted(false)
                            Log.e(TAG, "Not exists ")
                            // You can perform further actions here
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error checking friend existence: ", exception)
                    }
            }
        }
        fun getPhotos (uid: String, onComplete: (List<String>) -> Unit){
            val urls = mutableListOf<String>()
            var cnt = 0
            db.collection("photos")
                .document(uid)
                .get()
                .addOnSuccessListener { docs ->
                    if(docs.exists()){
                        val data = docs.data
                        if (data != null) {
                            for ((key, value) in data) {
                                if (value is String) {
                                    urls.add(value)
                                }
                            }
                            cnt++
                            if(cnt == 2){
                                onComplete(urls.toList())
                            }
                        }
                    }
                    else{
                        cnt++
                        if(cnt == 2){
                            onComplete(urls.toList())
                        }
                    }
                }
                .addOnFailureListener { e ->
                    cnt++
                    if(cnt == 2){
                        onComplete(urls.toList())
                    }
                }
            db.collection("profile_pictures")
                .document(uid)
                .get()
                .addOnSuccessListener { docs ->
                    if(docs.exists()){
                        val data = docs.data
                        if (data != null) {
                            for ((key, value) in data) {
                                if (value is String) {
                                    urls.add(value)
                                }
                            }
                            cnt++
                            if(cnt == 2){
                                onComplete(urls.toList())
                            }
                        }
                    }
                    else{
                        cnt++
                        if(cnt == 2){
                            onComplete(urls.toList())
                        }
                    }
                }
                .addOnFailureListener { e ->
                    cnt++
                    if(cnt == 2){
                        onComplete(urls.toList())
                    }
                }
        }
        fun getJourneyPagePhotos(uid: String, onCompleted: (List<journey_page_photos_model>) -> Unit){
            db.collection("journeys")
                .document(uid)
                .collection("photos")
                .get()
                .addOnSuccessListener { documents ->
                    val photolist = mutableListOf<journey_page_photos_model>()
                    for(document in documents){
                        try {
                            val journey = document.toObject(journey_page_photos_model::class.java)
                            photolist.add(journey)
                            Log.d(TAG, journey.toString())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deserializing document ${document.id}", e)
                        }
                    }
                    onCompleted(photolist)
                }
                .addOnFailureListener {
                    onCompleted(emptyList())
                }
        }
        fun getAllJourney(onComplete: (List<TripsModel>) -> Unit){
            db.collection("journeys")
                .get()
                .addOnSuccessListener { documents ->
                    val userList = mutableListOf<TripsModel>()
                    for (document in documents) {
                        try {
                            val journey = document.toObject(TripsModel::class.java)
                            journey.uid = document.id
                            userList.add(journey)
                            Log.d(TAG, journey.toString())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deserializing document ${document.id}", e)
                            // Handle or log error as needed, e.g., incorrect data types
                        }
                    }
                    Log.d(TAG, "Documents fetched successfully")
                    onComplete(userList)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching documents", exception)
                    onComplete(emptyList())
                }
        }
        fun getJourneybySearch(namePrefix: String, type: String, onCompleted: (List<TripsModel>) -> Unit) {
            val lowercasePrefix = namePrefix.toLowerCase(Locale.ROOT)
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
                            journey.uid = document.id
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
        fun CheckMyJourney(journeyID: String, myUID: String,onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(myUID)
                .collection("own_journey")
                .document(journeyID)
                .get()
                .addOnSuccessListener {
                    docs ->
                        if(docs.exists())
                            onCompleted(true)
                        else
                            onCompleted(false)
                }
                .addOnFailureListener {
                    e ->
                        onCompleted(false)
                }
        }

        fun getUsersByEmail(email: String, onCompleted: (String) -> Unit){
            db.collection("users_by_email")
                .document(email)
                .get()
                .addOnSuccessListener { docs ->
                    if(docs.exists()){
                        onCompleted(docs.getString("uid").toString())
                    }
                    else
                        onCompleted("")
                }
                .addOnFailureListener { e ->
                    onCompleted("")
                }
        }
        fun getUserByUID(uid: String, onComplete: (List<search_users_model>) -> Unit){
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { docs ->
                    if(docs.exists()) {
                        val userList = mutableListOf<search_users_model>()
                        val userName = docs.getString("name") ?: ""
                        val user = search_users_model(uid, userName)
                        userList.add(user)
                        onComplete(userList)
                    }
                    else{
                        onComplete(emptyList())
                    }
                }
                .addOnFailureListener {
                    onComplete(emptyList())
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
        fun getFriends(context: Context, onCompleted: (List<String>) -> Unit) {
            Log.d(TAG, "i am in getFriends")
            val progressDialog = ProgressDialog(context).apply {
                setMessage("Loading...")
                show()
            }
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("friends")
                    .get()
                    .addOnSuccessListener { documents ->
                        val uids = mutableListOf<String>()
                        for (document in documents) {
                            val uid = document.getString("uid")
                            Log.d(TAG,uid.toString())
                            uid?.let { uids.add(it) }
                        }
                        onCompleted(uids)
                        progressDialog.dismiss()
                    }
                    .addOnFailureListener { exception ->
                        progressDialog.dismiss()
                        Log.w(TAG, "Error getting Friends ", exception)
                        onCompleted(emptyList())
                        progressDialog.dismiss()
                    }
            } else {
                progressDialog.dismiss()
                onCompleted(emptyList())
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
                    .document(journeyID)
                    .set(data)
                    .addOnSuccessListener {
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
        fun isInMyPending(uid: String,journeyID: String,onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(uid)
                .collection("pending")
                .document(journeyID)
                .get()
                .addOnSuccessListener {docs ->
                    onCompleted(docs.exists())
                }
                .addOnFailureListener {
                    onCompleted(false)
                }

        }
        fun askToJoin(uid: String,name: String,journeyID: String,onCompleted: (Boolean) -> Unit){
            val data = hashMapOf(
                "uid" to uid,
                "name" to name
            )
            db.collection("journeys")
                .document(journeyID)
                .collection("pending")
                .document(uid)
                .set(data)
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun ismemberOrWannabeMember(uid: String,type: String,journeyID: String,onCompleted: (Boolean) -> Unit){
            db.collection("journeys")
                .document(journeyID)
                .collection(type)
                .document(uid)
                .get()
                .addOnSuccessListener {docs ->
                    if(docs.exists())
                        onCompleted(true)
                    else
                        onCompleted(false)
                }
                .addOnFailureListener { e ->
                    onCompleted(false)
                }
        }
        fun delete_journey_from_user(type: String, uid: String, journeyID: String, onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(uid)
                .collection(type)
                .document(journeyID)
                .delete()
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener { e ->
                    println("Error deleting document: $e")
                    onCompleted(false)
                }
        }
        fun delete_journey_user(from: String, journeyID: String,uid: String, onCompleted: (Boolean) -> Unit){
            db.collection("journeys")
                .document(journeyID)
                .collection(from)
                .document(uid)
                .delete()
                .addOnSuccessListener {
                    onCompleted(true)
                }
        }
        fun move_user_in_journey(from: String, to: String, uid: String, journeyID: String, onCompleted: (Boolean) -> Unit){
            db.collection("journeys")
                .document(journeyID)
                .collection(from)
                .document(uid)
                .get()
                .addOnSuccessListener { docs ->
                    val data = docs.data
                    if(data != null){
                        db.collection("journeys")
                            .document(journeyID)
                            .collection(to)
                            .document(uid)
                            .set(data)
                            .addOnSuccessListener {
                                delete_journey_user(from,journeyID,uid,onCompleted = {istrue ->
                                    if(istrue)
                                        onCompleted(true)
                                })
                            }
                            .addOnFailureListener {
                                onCompleted(false)
                            }

                    }
                    else
                        onCompleted(false)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun react(myuid: String,uid: String, postID: String, onCompleted: (String) -> Unit){
            db.collection("users")
                .document(uid)
                .collection("posts")
                .document(postID)
                .get()
                .addOnSuccessListener {docs ->
                    val current = docs.getString("react") ?: "0"
                    var num = current.toInt()+1
                    val data = hashMapOf(
                        "react" to num.toString()
                    )
                    val data1 = hashMapOf(
                        "uid" to myuid
                    )
                    db.collection("users")
                        .document(uid)
                        .collection("posts")
                        .document(postID)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            db.collection("users")
                                .document(uid)
                                .collection("posts")
                                .document(postID)
                                .collection("reacted")
                                .document(myuid)
                                .set(data1)
                            onCompleted(num.toString())

                        }
                        .addOnFailureListener {
                            onCompleted(num.toString())
                        }
                }
        }
        fun move_journey_in_users_collection(from: String,to: String, uid: String, journeyID: String, onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(uid)
                .collection(from)
                .document(journeyID)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val data = doc.data
                        if (data != null) {
                            db.collection("users")
                                .document(uid)
                                .collection(to)
                                .document(journeyID)
                                .set(data)
                                .addOnSuccessListener {
                                    db.collection("users")
                                        .document(uid)
                                        .collection(from)
                                        .document(journeyID)
                                        .delete()
                                        .addOnSuccessListener {
                                            onCompleted(true)
                                        }
                                        .addOnFailureListener {e ->
                                            onCompleted(false)
                                        }

                                }
                                .addOnFailureListener { e ->
                                    println("Error moving document: $e")
                                    onCompleted(false)
                                }
                        } else {
                            println("Document data is null.")
                            onCompleted(false)
                        }
                    } else {
                        println("Document does not exist.")
                        onCompleted(false)
                    }
                }
                .addOnFailureListener { e ->
                    // An error occurred while fetching the document
                    println("Error fetching document: $e")
                    onCompleted(false)
                }
        }
        fun sent_request_to_journey(type: String,uid: String, name: String, juid: String, onCompleted: (Boolean) -> Unit){
            val data = hashMapOf(
                "uid" to uid,
                "name" to name
            )
            db.collection("journeys")
                .document(juid)
                .collection(type)
                .document(uid)
                .set(data)
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun sentInviteToFriend(uid: String,type: String,by_whom: String,journeyID: String,onCompleted: (Boolean) -> Unit){
            val data = hashMapOf(
                "journeyID" to journeyID,
                "by_whom" to by_whom
            )
            db.collection("users")
                .document(uid)
                .collection(type)
                .document(journeyID)
                .set(data)
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun getJourneyFields (uid: String,field: String, onCompleted: (String) -> Unit){
            db.collection("journeys")
                .document(uid)
                .get()
                .addOnSuccessListener {
                    docs ->
                        onCompleted(docs.getString(field).toString())
                }
                .addOnFailureListener {
                    onCompleted("")
                }
        }
        fun search_tour(){

        }
        fun get_upcoming_tour(context: Context,type: String, uid: String, onComplete: (List<TripsModel>) -> Unit) {
            val tripsList = mutableListOf<TripsModel>()
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            db.collection("users").document(uid).collection(type)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result ?: return@addOnCompleteListener onComplete(emptyList())
                        if (documents.isEmpty) onComplete(emptyList())
                        var pendingQueries = documents.size()
                        for (document in documents) {
                            val foundString = document.getString("journeyID") ?: ""
                            db.collection("journeys")
                                .document(foundString)
                                .get()
                                .addOnSuccessListener { document ->
                                    Log.d(TAG,document.toString())
                                    val trip = TripsModel(
                                        picture = document.getString("picture") ?: "",
                                        title = document.getString("title") ?: "",
                                        duration = document.getString("duration") ?: "",
                                        date = document.getString("date") ?: "",
                                        places = document.getString("places") ?: "",
                                        budget = document.getString("budget") ?: "",
                                        check_in = document.getString("check_in") ?: "",
                                        vacancy = document.getString("vacancy") ?: "",
                                        uid = foundString,
                                        gender = document.getString("gender") ?: "",
                                        owner = document.getString("owner") ?: ""
                                    )
                                    tripsList.add(trip)
                                    pendingQueries--
                                    if (pendingQueries <= 0) {
                                        onComplete(tripsList)
                                    }
                                }
                                .addOnFailureListener {
                                    pendingQueries--
                                    if (pendingQueries <= 0) {
                                        onComplete(tripsList)
                                    }
                                }
                        }
                        progressDialog.dismiss()
                    } else {
                        task.exception?.let {
                            Log.w("Error", "Error getting documents: ", it)
                            onComplete(emptyList())
                        }
                        progressDialog.dismiss()
                    }
                }
                .addOnFailureListener {
                    onComplete(emptyList())
                }
        }
        fun get_past_tour(context: Context,uid: String, onComplete: (List<TripsModel>) -> Unit) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val tripsList = mutableListOf<TripsModel>()
            db.collection("users").document(uid).collection("past_journey")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result ?: return@addOnCompleteListener onComplete(emptyList())
                        if (documents.isEmpty) onComplete(emptyList()) // Early return if no journeys

                        var pendingQueries = documents.size() // Set counter for pending queries
                        for (document in documents) {
                            val foundString = document.getString("journeyID") ?: ""
                            db.collection("journeys")
                                .document(foundString)
                                .get()
                                .addOnSuccessListener { document ->

                                    Log.d(TAG,document.toString())

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
                                    pendingQueries--
                                    if (pendingQueries <= 0) {
                                        onComplete(tripsList) // Call onComplete when all queries are done
                                    }
                                }
                                .addOnFailureListener {
                                    pendingQueries-- // Ensure to decrement on failure too
                                    if (pendingQueries <= 0) {
                                        onComplete(tripsList)
                                    }
                                }
                        }
                        progressDialog.dismiss()
                    } else {
                        task.exception?.let {
                            Log.w("Error", "Error getting documents: ", it)
                            onComplete(emptyList())
                        }
                    }
                }
                .addOnFailureListener {
                    onComplete(emptyList())
                }
        }
        fun setProfile(uid: String, field: String,text: String, onCompleted: (Boolean) -> Unit){
            val data = hashMapOf(
                field to text
            )
            db.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun getReactNum(uid: String, pid: String, onCompleted: (String) -> Unit){
            db.collection("users")
                .document(uid)
                .collection("posts")
                .document(pid)
                .collection("reacted")
                .get()
                .addOnSuccessListener { docs ->
                    val doc = docs.size()
                    onCompleted(doc.toString())
                }
                .addOnFailureListener {
                    onCompleted("0")
                }
        }
        fun cancelJoinRequest(uid: String, myuid: String, onCompleted: (Boolean) -> Unit){
            db.collection("journeys")
                .document(uid)
                .collection("pending")
                .document(myuid)
                .delete()
                .addOnSuccessListener {
                    onCompleted(true)
                }
        }
        fun deletePost(uid: String, postID: String,onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(uid)
                .collection("posts")
                .document(postID)
                .delete()
                .addOnSuccessListener {
                    onCompleted(true)
                }
        }
        fun getProfileInfo(uid: String, onComplete: (List<String>) -> Unit) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val list = mutableListOf<String>()
                    val email = document.getString("email")
                    val phone = document.getString("phone")
                    val address = document.getString("address")
                    val nationality = document.getString("nationality")
                    val interests = document.getString("interests")
                    val gender = document.getString("gender")
                    val destinations = document.getString("destinations")
                    val seasons = document.getString("seasons")
                    val duration = document.getString("duration")
                    val budget = document.getString("budget")
                    list.add(email ?: "")
                    list.add(phone ?: "")
                    list.add(address ?: "")
                    list.add(nationality ?: "")
                    list.add(interests ?: "")
                    list.add(gender ?: "")
                    list.add(destinations ?: "")
                    list.add(seasons ?: "")
                    list.add(duration ?: "")
                    list.add(budget ?: "")
                    onComplete(list)
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
                        callback(null)
                    }
            } else {
                callback(null)
            }
        }
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
        fun remove_friend(uid: String, myuid: String,onCompleted: (Boolean) -> Unit){
            db.collection("users")
                .document(uid)
                .collection("friends")
                .document(myuid)
                .delete()
                .addOnSuccessListener {
                    db.collection("users")
                        .document(myuid)
                        .collection("friends")
                        .document(uid)
                        .delete()
                        .addOnSuccessListener {
                            onCompleted(true)
                        }
                        .addOnFailureListener {
                            onCompleted(false)
                        }
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun addFriend(uid: String, context: Context,callback: (Boolean) -> Unit) {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                var fname = "-1"
                var fprofile_pic = "-1"
                var myname = "-1"
                var myprofile_pic = "-1"
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
                            db.collection("users")
                                .document(uid)
                                .collection("pending_requests")
                                .document(currentUser.uid)
                                .set(myData)
                                .addOnSuccessListener { documentReference ->
                                    callback(true)
                                    Log.d(TAG, "Friend request recieved successfully")
                                    Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener{e ->
                                    callback(false)
                                }
                        }
                        .addOnFailureListener { e ->
                            callback(false)
                            Log.w(TAG, "Friend request send failed", e)
                            Toast.makeText(context, "Friend request send failed", Toast.LENGTH_SHORT).show()
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
        fun getUserNotification(uid: String, onComplete: (List<user_notification_model>) -> Unit) {
            db.collection("users")
                .document(uid)
                .collection("notification")
                .get()
                .addOnSuccessListener { documents ->
                    val notifications = mutableListOf<user_notification_model>()
                    for (document in documents) {
                        val text = document.getString("text") ?: ""
                        val timestamp = document.getDate("timestampField") ?: Date()
                        val notification = user_notification_model(text, timestamp)
                        notifications.add(notification)
                    }
                    notifications.sortByDescending { it.timestampField }
                    onComplete(notifications)
                }
                .addOnFailureListener { exception ->
                    println("Error getting documents: $exception")
                    onComplete(emptyList())
                }
        }

        fun createUserNotification(uid: String,text: String){
            val data = hashMapOf(
                "text" to text,
                "timestampField" to FieldValue.serverTimestamp()
            )
            db.collection("users")
                .document(uid)
                .collection("notification")
                .add(data)
        }
        fun createUserReactNotification(uid: String,pid: String,text: String){
            val data = hashMapOf(
                "text" to text,
                "timestampField" to FieldValue.serverTimestamp()
            )
            db.collection("users")
                .document(uid)
                .collection("notification")
                .document(pid)
                .set(data)
        }
        fun getNotifications(journeyID: String, onComplete: (List<notification_model>) -> Unit) {
            db.collection("journeys")
                .document(journeyID)
                .collection("notices")
                .get()
                .addOnSuccessListener { all_notices ->
                    val notifications = mutableListOf<notification_model>()
                    for (document in all_notices.documents) {
                        val notification = document.getString("text") ?: ""
                        val uid = document.getString("uid") ?: ""
                        val name = document.getString("name") ?: ""
                        val createTime = document.getTimestamp("timestampField")?.toDate() ?: Date()
                        val notificationModel = notification_model(notification, uid, name, createTime)
                        notifications.add(notificationModel)
                    }
                    // Sort notifications by timestamp in descending order
                    notifications.sortByDescending { it.timestampField }

                    onComplete(notifications)
                }
                .addOnFailureListener {
                    onComplete(emptyList())
                }
        }
        fun create_notice(journeyID: String,text: String, name: String,uid: String,onCompleted: (Boolean) -> Unit){
            val data = hashMapOf(
                "text" to text,
                "journeyID" to journeyID,
                "name" to name,
                "uid" to uid,
                "timestampField" to FieldValue.serverTimestamp()
            )
            db.collection("journeys")
                .document(journeyID)
                .collection("notices")
                .add(data)
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }

        }
        fun uploadImageURLToJourney(url: String, owner: String, onCompleted: (Boolean) -> Unit) {
            val data = hashMapOf(
                "owner" to owner,
                "url" to url
            )
            val journeyUID = DataClass.journeyUID
            db.collection("journeys")
                .document(journeyUID)
                .collection("photos")
                .add(data)
                .addOnSuccessListener {
                    onCompleted(true)
                }
                .addOnFailureListener {
                    onCompleted(false)
                }
        }
        fun uploadImageJourneyPage(imageUri: Uri, context: Context,purpose: String, onCompleted: (String) -> Unit) {
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
                    onCompleted(downloadUri.toString())
                    Log.d(TAG, "Download URL: $downloadUri")

                } else {
                    Log.e(TAG, "Failed to upload image")
                    onCompleted("null")
                    Toast.makeText(context,"Image Upload failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
        fun uploadImageToFirestore(imageUri: Uri, context: Context,purpose: String,privacy: Int,caption: String,name: String) {
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
                    storeImageUrlInFirestore(context,downloadUri.toString(),purpose,privacy,caption,name)
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
        fun storeImageUrlInFirestore(context: Context,imageUrl: String,collection: String,privacy: Int,caption: String,name: String) {
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
                        createPost(imageUrl, caption, privacy, context,name)
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
        fun isExistinJourney(uid: String, juid: String, onCompleted: (Boolean) -> Unit){
            db.collection("journeys")
                .document(juid)
                .collection("members")
                .document(uid)
                .get()
                .addOnSuccessListener { info ->
                    if(info.exists())
                        onCompleted(true)
                    else
                        onCompleted(false)
                }
                .addOnFailureListener {e ->
                    onCompleted(false)
                }
        }
        fun inviteFriends(uid: String,onCompleted: (List<search_users_model>) -> Unit) {
            db.collection("users")
                .document(uid)
                .collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    val userList = mutableListOf<search_users_model>()
                    for (document in documents) {
                        val uid = document.id
                        val userName = document.getString("name") ?: ""
                        val user = search_users_model(uid, userName)
                        userList.add(user)
                    }
                    onCompleted(userList)
                }
                .addOnFailureListener {
                    onCompleted(emptyList())
                }
        }
        fun getJourneyPageMembers(journeyID: String,type: String,onComplete: (List<search_users_model>) -> Unit){
            db.collection("journeys")
                .document(journeyID)
                .collection(type)
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
        fun getuserinfo(uid: String, field: String, onCompleted: (String) -> Unit){
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener {docs ->
                    if(docs.exists()){
                        onCompleted(docs[field].toString())
                    }
                    else{
                        onCompleted("")
                    }
                }
                .addOnFailureListener {
                    onCompleted("")
                }
        }
        fun createPost(imageUrl: String, caption: String, privacy: Int, context: Context,name: String) {
            val currentUser = getCurrentUser()
            currentUser?.let { user ->
                val postData = hashMapOf(
                    "name" to name,
                    "owner" to currentUser.uid,
                    "imageUrl" to imageUrl,
                    "caption" to caption,
                    "privacy" to privacy,
                    "react" to "0",
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