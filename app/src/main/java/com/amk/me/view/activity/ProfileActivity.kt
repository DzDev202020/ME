package com.amk.me.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amk.me.R
import com.amk.me.databinding.ActivityProfileBinding
import com.amk.me.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST: Int = 1
    }

    private lateinit var binding: ActivityProfileBinding
    lateinit var someActivityResultLauncher: ActivityResultLauncher<Intent>

    var pictureDialog: AlertDialog? = null
    var needPermissionDialog: AlertDialog? = null


    @Inject
    lateinit var viewmodel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        binding.viewmodel = viewmodel
        binding.lifecycleOwner = this
        binding.executePendingBindings()


        setContentView(binding.root)

        createPictureSelectResultTracker()

        startObserverPictureState()
        initProfilePicture()

    }

    private fun startObserverPictureState() {
        viewmodel.pictureState.observe(this) { state ->
            run {
                when (state) {
                    ProfileViewModel.IDEAL_STATE -> {

                    }
                    ProfileViewModel.ON_UPLOAD -> {

                    }
                    ProfileViewModel.FAIL_UPLOAD -> {
                        showToastFailUploadPicture()
                    }
                    ProfileViewModel.DONE_UPLOAD -> {
                        initProfilePicture()
                    }
                    ProfileViewModel.DONE_DELETE -> {
                        showToastPictureDeleted()
                        deletePictureFile()

                    }
                    ProfileViewModel.FAIL_DELETE -> {
                        showToastFailDeletedPicture()
                    }
                    ProfileViewModel.ON_DELETE -> {

                    }
                    ProfileViewModel.DONE_DOWNLOAD -> {

                    }
                    ProfileViewModel.FAIL_DOWNLOAD -> {
                        showToastFailDownloadPicture()
                    }
                    ProfileViewModel.NO_PICTURE -> {

                    }

                }
            }

        }
    }

    private fun showToastFailDownloadPicture() {
        Toast.makeText(this, R.string.fail_download_picture, Toast.LENGTH_SHORT).show()
    }

    private fun showToastFailDeletedPicture() {
        Toast.makeText(this, R.string.fail_delete_picture, Toast.LENGTH_SHORT).show()
    }

    private fun deletePictureFile() {
        getPictureFile()?.delete()
    }

    private fun showToastPictureDeleted() {
        Toast.makeText(this, R.string.done_delete_picture, Toast.LENGTH_SHORT).show()
    }

    private fun showToastFailUploadPicture() {
        Toast.makeText(this, R.string.fail_upload_picture, Toast.LENGTH_SHORT).show()
    }


    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openSelectPicture()
            } else {
                createToastNoPermissionNoUpload()
            }
        }

    }


    @Override
    override fun onDestroy() {
        super.onDestroy()
        if (pictureDialog != null && pictureDialog!!.isShowing) pictureDialog!!.dismiss()
        if (needPermissionDialog != null && needPermissionDialog!!.isShowing) needPermissionDialog!!.dismiss()

    }

    private fun createPictureSelectResultTracker() {
        someActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data: Intent? = result.data
                if (result.resultCode == Activity.RESULT_OK && data != null && data.data != null) {


                    val mimeType: String? = data.data!!.let { returnUri ->
                        contentResolver.getType(returnUri)
                    }

                    val values: Array<Any>? = data.data!!.let { returnUri ->
                        contentResolver.query(returnUri, null, null, null, null)
                    }?.use { cursor ->

                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        cursor.moveToFirst()

                        arrayOf(cursor.getString(nameIndex), cursor.getDouble(sizeIndex))

                    }

                    if (hasAccessToExternalStorage()) {
                        viewmodel.uploadPicture(
                            getImageFilePath(data.data!!) ?: "",
                            mimeType.toString(),
                            values!![0] as String,
                            values[1] as Double
                        )
                    } else {
                        requestExternalStoragePermission()
                    }
                } else {
                    showErrorPickPicture()
                }
            }
    }


    fun getImageFilePath(uri: Uri?): String? {
        var path: String? = null
        var image_id: String? = null
        var cursor = contentResolver.query(uri!!, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            image_id = cursor.getString(0)
            image_id = image_id.substring(image_id.lastIndexOf(":") + 1)
            cursor.close()
        }
        cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Images.Media._ID + " = ? ",
            arrayOf(image_id),
            null
        )
        if (cursor != null) {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            path = cursor.getString(index)
            cursor.close()
        }
        return path
    }


    private fun initProfilePicture() {
        if (userHasPicture()) {
            setPicture()
        } else {
            viewmodel.tryGetUserPicture(getPictureFileDir())
        }
    }

    private fun setPicture() {
        val file: File = getPictureFile()!!
        Picasso.get().load(file).into(binding.profilePicture)
    }

    private fun userHasPicture(): Boolean {
        if (getPictureFile() != null) return true
        return false
    }

    private fun getPictureFileDir(): File {
        return filesDir
    }

    private fun getPictureFile(): File? {
        val mainDirFile: File = filesDir
        for (fileX: File in mainDirFile.listFiles()!!) if (fileX.name.contains(viewmodel.user.full_name)) return fileX
        return null
    }

    fun onPictureClick(view: View) {
        if (userHasPicture()) {
            createDialogUpdateOrDeletePicture()
        } else {
            createDialogUploadPicture()
        }
    }

    private fun createDialogUploadPicture() {
        pictureDialog = MaterialAlertDialogBuilder(this).setTitle(R.string.my_picture)
            .setMessage(R.string.upload_new_picture)
            .setNegativeButton(R.string.cancel) { dialog, which ->
                // Respond to negative button press
            }.setPositiveButton(resources.getString(R.string.select)) { dialog, which ->
                checkPermissionExternalStorage()
            }.show()
    }

    private fun createDialogUpdateOrDeletePicture() {
        pictureDialog = MaterialAlertDialogBuilder(this).setTitle(R.string.my_picture)
            .setMessage(R.string.delete_picture)
            .setNegativeButton(R.string.cancel) { dialog, which ->
                // Respond to negative button press
            }.setPositiveButton(resources.getString(R.string.select)) { dialog, which ->
                deleteProfilePicture()
            }.show()
    }

    private fun deleteProfilePicture() {
        viewmodel.deletePicture()
    }

    private fun openSelectPicture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        someActivityResultLauncher.launch(intent)

    }

    private fun openSettingApp() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun checkPermissionExternalStorage() {
        if (hasAccessToExternalStorage()) {
            openSelectPicture()
        } else {
            requestExternalStoragePermission()
        }
    }

    private fun requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showWeNeedExternalStoragePermission()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST
            )
        }
    }

    private fun hasAccessToExternalStorage(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createToastNoPermissionNoUpload() {
        Toast.makeText(this, R.string.no_permission_no_upload, Toast.LENGTH_SHORT).show()
    }

    private fun showWeNeedExternalStoragePermission() {
        needPermissionDialog = MaterialAlertDialogBuilder(this).setTitle(R.string.permission)
            .setMessage(R.string.need_permission_message)
            .setNegativeButton(R.string.cancel) { dialog, which ->
                // Respond to negative button press
            }.setPositiveButton(R.string.give) { dialog, which ->
                openSettingApp()
            }.show()
    }

    private fun showErrorPickPicture() {
        Toast.makeText(this, R.string.error_when_selecting_picture, Toast.LENGTH_SHORT).show()
    }

    fun logout(view: View) {
        viewmodel.logout()
        finish()
    }

}