package com.example.projemanag.activities


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMyProfileBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException



class MyProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var mUserDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        FirestoreClass().loadUserData(this)

        binding.ivProfileUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }else{
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))

                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            }
        }else{
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage. You can also allow it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(mSelectedImageFileUri)
                    .centerCrop() //fitCenter()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }
        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }
    fun setUserDataInUI(user: User) {

        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop() //fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != 0L) {
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty()
            && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if (binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }
        if (binding.etMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("USER_IMAGE"
                        + System.currentTimeMillis() + "."
                        + getFileExtension(mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i("Firebase image URL", taskSnapshot.metadata!!
                    .reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageURL = uri.toString()

                   updateUserProfileData()

                }
            }.addOnFailureListener{
                exception->
                Toast.makeText(this@MyProfileActivity,
                    exception.message, Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }

    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        finish()
    }

    companion object{
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

}