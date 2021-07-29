package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
            Log.e("ups", "Register error")
        }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFirestore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while creating a board", e)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,
                    "Board created successfully!")
                Toast.makeText(activity,
                    "Board created successfully!", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while creating a board" , e)
                Toast.makeText(activity,
                    "Error while creating a board", Toast.LENGTH_LONG).show()
            }
    }

    fun getBoardList(activity: MainActivity) {
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while creating a board", e)
            }
    }

    fun addUpdateTaskList(activity: TaskListActivity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "taskList updated successfully")
                activity.addUpdateTaskListSuccess()
            }.addOnFailureListener {
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while creating a board", e)
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity,
                              userHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,
                    "Profile data updates successfully!")
                Toast.makeText(activity,
                    "Profile updated successfully!", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while creating a board" , e)
                Toast.makeText(activity,
                    "Error while creating a board", Toast.LENGTH_LONG).show()
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                when (activity) {
                    is SignInActivity ->{
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity ->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                    }
                    is MyProfileActivity ->{
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }.addOnFailureListener {
                    e ->
                when (activity) {
                    is SignInActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e("ups", "Register error")
            }
    }
    fun getCurrentUserId():String {

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getAssignedMembersListDetails(activity: MembersActivity,
                                      assignedTo: ArrayList<String>) {
        mFirestore.collection(Constants.USERS).whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                documemt ->
                Log.e(activity.javaClass.simpleName, documemt.documents.toString())
                val usersList: ArrayList<User> = ArrayList()

                for (i in documemt.documents) {
                    val user = i.toObject(User::class.java)
                    if (user != null) {
                        usersList.add(user)
                    }
                }
                activity.setupMembersList(usersList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

}