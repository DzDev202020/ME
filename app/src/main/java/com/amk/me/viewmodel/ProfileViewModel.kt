package com.amk.me.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amk.me.model.models.User
import com.amk.me.model.repositories.DownloadState
import com.amk.me.model.repositories.ProfileRepository
import com.amk.me.model.retrofit.CustomUploadRequestBody
import com.amk.me.model.repositories.MainRepository
import com.amk.me.model.retrofit.MyCustomRequestBodyCallback
import dagger.hilt.android.scopes.ActivityRetainedScoped


import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject


@ActivityRetainedScoped
class ProfileViewModel @Inject constructor(
    var mainRepository: MainRepository,
    var profileRepository: ProfileRepository
) : ViewModel() {

    companion object {
        const val IDEAL_STATE: Int = 0
        const val ON_UPLOAD: Int = 1
        const val FAIL_UPLOAD: Int = 2
        const val DONE_UPLOAD: Int = 3

        const val DONE_DELETE: Int = 4
        const val FAIL_DELETE: Int = 5
        const val ON_DELETE: Int = 6

        const val DONE_DOWNLOAD: Int = 7
        const val FAIL_DOWNLOAD: Int = 8
        const val ON_DOWNLOAD: Int = 9
        const val NO_PICTURE: Int = 10
    }


    init {
        doInit()
    }

    private fun doInit() {
        user = mainRepository.user!!

        _onUploadPicture = MutableLiveData(false)
        onUploadPicture = _onUploadPicture

        _pictureState = MutableLiveData(IDEAL_STATE)
        pictureState = _pictureState

        _pictureUploadProgress = MutableLiveData(0)
        pictureUploadProgress = _pictureUploadProgress
    }

    lateinit var user: User

    private lateinit var _pictureState: MutableLiveData<Int>
    lateinit var pictureState: LiveData<Int>
    private lateinit var _pictureUploadProgress: MutableLiveData<Int>
    lateinit var pictureUploadProgress: LiveData<Int>

    private lateinit var _onUploadPicture: MutableLiveData<Boolean>
    lateinit var onUploadPicture: LiveData<Boolean>


    fun uploadPicture(filePath: String, mimeType: String, name: String, size: Double) {

        _pictureState.value = ON_UPLOAD
        _onUploadPicture.value = true


        val file = File(filePath)


        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "picture",
            name,
            CustomUploadRequestBody(file, mimeType, object : MyCustomRequestBodyCallback {
                override fun onProgressUpdate(percentage: Int) {

                    _pictureUploadProgress.value = percentage
                }
            })
        )

        viewModelScope.launch {
            try {
                val response = mainRepository.uploadUserPicture(filePart)

                if (response.isSuccessful) {
                    _pictureState.value = DONE_UPLOAD
                    _onUploadPicture.value = false
                    _pictureUploadProgress.value = 0
                    _pictureState.value = IDEAL_STATE
                } else {
                    _pictureState.value = FAIL_UPLOAD
                    _onUploadPicture.value = false
                    _pictureUploadProgress.value = 0
                    _pictureState.value = IDEAL_STATE
                }

            } catch (e: Exception) {


                _pictureState.value = FAIL_UPLOAD
                _onUploadPicture.value = false
                _pictureUploadProgress.value = 0
                _pictureState.value = IDEAL_STATE
            }
        }

    }

    fun deletePicture() {
        _pictureState.value = ON_DELETE
        _onUploadPicture.value = true

        viewModelScope.launch {
            try {
                val deleted = mainRepository.deleteUserPicture()
                if (deleted) _pictureState.value = DONE_DELETE
                else _pictureState.value = FAIL_DELETE

            } catch (e: Exception) {
                _pictureState.value = FAIL_DELETE

            }
            _onUploadPicture.value = false
            _pictureState.value = IDEAL_STATE
        }
    }

    fun tryGetUserPicture(file: File?) {
        downLoadUserPicture(file)
    }

    private fun downLoadUserPicture(fileDir: File?) {

        if (fileDir == null) {
            failDownLoadPicture()
            return
        }
        _pictureState.value = ON_DOWNLOAD
        _onUploadPicture.value = true

        viewModelScope.launch {
            try {
                mainRepository.downLoadUserPicture(user.id, fileDir).collect { download ->


                    when (download) {
                        is DownloadState.Progress -> {

                            if (download.percent == -1) {
                                userHasNoPicture()
                            } else {
                                _pictureUploadProgress.value = download.percent
                            }
                        }
                        is DownloadState.Finished -> {
                            doneDownLoadPicture()
                        }
                        is DownloadState.Fail -> {
                            failDownLoadPicture()
                        }
                    }

                }
            } catch (e: Exception) {
                failDownLoadPicture()
            }
        }
    }

    private fun failDownLoadPicture() {

        _onUploadPicture.value = false
        _pictureState.value = FAIL_DOWNLOAD
        _pictureState.value = IDEAL_STATE
        _pictureUploadProgress.value = 0
    }

    private fun doneDownLoadPicture() {

        _onUploadPicture.value = false
        _pictureState.value = DONE_DOWNLOAD
        _pictureState.value = IDEAL_STATE
        _pictureUploadProgress.value = 0
    }

    private fun userHasNoPicture() {

        _onUploadPicture.value = false
        _pictureState.value = NO_PICTURE
        _pictureState.value = IDEAL_STATE
    }

    fun logout() {
        mainRepository.logout()
    }

}