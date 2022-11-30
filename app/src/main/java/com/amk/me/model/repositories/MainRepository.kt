package com.amk.me.model.repositories

import android.content.SharedPreferences
import com.amk.me.model.models.User
import com.amk.me.model.retrofit.SessionManager
import com.amk.me.model.retrofit.UserWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Credentials
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(

    private var sessionManager: SessionManager,
    private var userWebService: UserWebService,
    var sharedPreferences: SharedPreferences
) {

    var user: User? = null


    suspend fun readUser(email: String, password: String): User? {
        val emailPassword = Credentials.basic(email, password)
        user = userWebService.readUser(emailPassword)
        return user
    }

    suspend fun updateUser(user: User): User? {
        this.user = userWebService.updateUser(user)
        return this.user
    }

    suspend fun uploadUserPicture(file: MultipartBody.Part): Response<ResponseBody> {
        return userWebService.uploadUserPicture(user!!.id, file)
    }

    suspend fun downLoadUserPicture(id: String, fileDir: File): Flow<DownloadState> {
        val response = userWebService.downloadUserPicture(id)

        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.downloadToFileWithProgress(fileDir)
        } else {
            flow {
                emit(DownloadState.Fail(response.code()))
            }
        }
    }

    suspend fun deleteUserPicture(): Boolean {
        return userWebService.deleteUserPicture(user!!.id).isSuccessful
    }

    private fun ResponseBody.downloadToFileWithProgress(
        directory: File
    ): Flow<DownloadState> =
        flow {
            if (contentLength() == 0.toLong()) {
                emit(DownloadState.Progress(-1))
            } else {
                emit(DownloadState.Progress(0))

                var deleteFile = true
                val file = File(directory, user!!.full_name + "." + "jpg")

                try {
                    byteStream().use { inputStream ->
                        file.outputStream().use { outputStream ->
                            val totalBytes = contentLength()
                            val data = ByteArray(8_192)
                            var progressBytes = 0L

                            while (true) {
                                val bytes = inputStream.read(data)

                                if (bytes == -1) {
                                    break
                                }

                                outputStream.write(data, 0, bytes)
                                progressBytes += bytes

                                emit(DownloadState.Progress(percent = ((progressBytes * 100) / totalBytes).toInt()))
                            }

                            when {
                                progressBytes < totalBytes ->
                                    throw Exception("missing bytes")
                                progressBytes > totalBytes ->
                                    throw Exception("too many bytes")
                                else ->
                                    deleteFile = false
                            }
                        }
                    }
                    emit(DownloadState.Finished(file))
                } finally {
                    if (deleteFile) {
                        file.delete()
                    }
                }
            }

        }
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()

    fun keepMeIn(): Boolean {
        return sharedPreferences.getBoolean(SessionManager.KEEP_ME_IN, false)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(SessionManager.USER_TOKEN, "")
    }

    fun getPassword(): String? {
        return sharedPreferences.getString(SessionManager.USER_PASSWORD, "")
    }

    fun remember(email: String?, password: String?, keepMeIn: Boolean) {
        val editor: SharedPreferences.Editor =
            sharedPreferences.edit()

        editor.putString(SessionManager.USER_ID, email)
        editor.putString(SessionManager.USER_PASSWORD, password)
        editor.putBoolean(SessionManager.KEEP_ME_IN, keepMeIn)

        editor.apply()
    }

    fun logout() {
        sessionManager.logout()
        user = null
    }

}

sealed class DownloadState {
    data class Progress(val percent: Int) : DownloadState()
    data class Fail(val responseCode: Int) : DownloadState()
    data class Finished(val file: File) : DownloadState()

}