package com.amk.me.viewmodel

import android.util.Log
import android.widget.CheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amk.me.R
import com.amk.me.model.models.User
import com.amk.me.model.repositories.MainRepository
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityRetainedScoped
class MainViewModel @Inject constructor(var mainRepository: MainRepository) : ViewModel() {

    companion object {
        const val NO_ACTION = 0

        const val ON_SIGN_IN = 1
        const val FAIL_SIGN_IN = 2
        const val DONE_SIGN_IN = 3

        const val ON_SIGN_UP = 5
        const val FAIL_SIGN_UP = 6
        const val DONE_SIGN_UP = 7
    }

    init {
        doInit()
        tryAutoSignIn()
    }



    private fun tryAutoSignIn() {
        if (mainRepository.keepMeIn()) {
            if (_email.value != null && password.value != null) signIn()
        }
    }


    private fun doInit() {
        _rememberMe = MutableLiveData(mainRepository.keepMeIn())
        rememberMe = _rememberMe

        _fullName = MutableLiveData("")
        fullName = _fullName
        _email = MutableLiveData(mainRepository.getEmail())
        email = _email
        _password = MutableLiveData(mainRepository.getPassword())
        password = _password

        _signInState = MutableLiveData(NO_ACTION)
        signInState = _signInState

        _onDoTask = MutableLiveData(false)
        onDoTask = _onDoTask

        _emailErrorState = MutableLiveData(false)
        emailErrorState = _emailErrorState

        _passwordErrorState = MutableLiveData(false)
        passwordErrorState = _passwordErrorState

        _fullNameErrorState = MutableLiveData(false)
        fullNameErrorState = _fullNameErrorState

        _emailErrorMessage = MutableLiveData(0)
        emailErrorMessage = _emailErrorMessage

        _passwordErrorMessage = MutableLiveData(0)
        passwordErrorMessage = _passwordErrorMessage

        _fullNameErrorMessage = MutableLiveData(0)
        fullNameErrorMessage = _fullNameErrorMessage

        _canTouchUiSignIn = MutableLiveData(true)

        canTouchUiSignIn = _canTouchUiSignIn
        _canTouchUiSignUp = MutableLiveData(true)


        canTouchUiSignUp = _canTouchUiSignUp
    }

    private lateinit var _emailErrorState: MutableLiveData<Boolean>
    lateinit var emailErrorState: LiveData<Boolean>
    private lateinit var _passwordErrorState: MutableLiveData<Boolean>
    lateinit var passwordErrorState: LiveData<Boolean>
    private lateinit var _fullNameErrorState: MutableLiveData<Boolean>
    lateinit var fullNameErrorState: LiveData<Boolean>


    private lateinit var _emailErrorMessage: MutableLiveData<Int>
    lateinit var emailErrorMessage: LiveData<Int>
    private lateinit var _passwordErrorMessage: MutableLiveData<Int>
    lateinit var passwordErrorMessage: LiveData<Int>
    private lateinit var _fullNameErrorMessage: MutableLiveData<Int>
    lateinit var fullNameErrorMessage: LiveData<Int>


    private lateinit var _onDoTask: MutableLiveData<Boolean>
    lateinit var onDoTask: LiveData<Boolean>
    private lateinit var _canTouchUiSignIn: MutableLiveData<Boolean>
    lateinit var canTouchUiSignIn: LiveData<Boolean>
    private lateinit var _canTouchUiSignUp: MutableLiveData<Boolean>
    lateinit var canTouchUiSignUp: LiveData<Boolean>
    private lateinit var _signInState: MutableLiveData<Int>
    lateinit var signInState: LiveData<Int>


//    fun signIn(
//        email: TextInputEditText,
//        password: TextInputEditText,
//        remember: CheckBox
//    ): () -> Unit = {
//        if (remember.isChecked)
//            mainRepository.remember(
//                email.text.toString(),
//                password.text.toString(),
//                remember.isChecked
//            )
//        signIn(email.text.toString(), password.text.toString())
//    }
//
//
//    private fun signIn(email: String, password: String) {
//        _onDoTask.value = true
//        _canTouchUiSignIn.value = false
//        _signInState.value = ON_SIGN_IN
//
//        viewModelScope.launch {
//            try {
//
//                val user: User? =
//                    mainRepository.readUser(email, password)
//                if (user != null) {
//                    doneSignIn()
//                } else {
//                    failSignIn()
//                }
//            } catch (e: Exception) {
//                failSignIn()
//            }
//        }
//    }


    fun signIn() {

        if (checkSignInInputs())
            return

        _onDoTask.value = true
        _canTouchUiSignIn.value = false
        _signInState.value = ON_SIGN_IN

        if (_rememberMe.value!!) mainRepository.remember(
            _email.value, _password.value, _rememberMe.value!!
        )

        viewModelScope.launch {
            try {

                val user: User? = mainRepository.readUser(_email.value!!, _password.value!!)
                if (user != null) {
                    doneSignIn()
                } else {
                    failSignIn()
                }
            } catch (e: Exception) {
                Log.e("TAG", "signIn: ", e)
                failSignIn()
            }
        }
    }

    private fun checkSignInInputs(): Boolean {

        if (_email.value != "") {
            _emailErrorState.value = false
        } else {

            _emailErrorMessage.value = R.string.empty_email_error_message
            _emailErrorState.value = true
        }
        if (_password.value != "") {
            _passwordErrorState.value = false

        } else {

            _passwordErrorMessage.value = R.string.empty_password_error_message
            _passwordErrorState.value = true
        }

        if (_email.value != "" && _password.value != "") {

            return false

        }

        return true

    }

    private fun failSignIn() {
        Log.e("TAG", "failSignIn: ")

        _onDoTask.postValue(false)
        _canTouchUiSignIn.postValue(true)
        _signInState.postValue(FAIL_SIGN_IN)
        _signInState.postValue(NO_ACTION)
    }

    private fun doneSignIn() {
        Log.e("TAG", "doneSignIn: ")

        _signInState.value=DONE_SIGN_IN
    }


    fun signUp() {
        Log.e("TAG", "signUp: ")
        if (checkSignUpInput())
            return

        _onDoTask.value = true
        _canTouchUiSignUp.value = false
        _signInState.value = ON_SIGN_UP

        viewModelScope.launch {
            try {
                val user: User? = mainRepository.updateUser(
                    User(
                        _fullName.value!!, _email.value!!, _password.value!!
                    )
                )

                if (user != null) {
//                        _email.postValue(user.email)
//                        _password.postValue(user.passwrod)
//                    _email.value = user.email
//                    _password.value = user.password
                    doneSignUp()
                } else {
                    failSignUp()
                }

            } catch (e: Exception) {
                Log.e("TAG", "signUp: ", e)
                failSignUp()
            }
        }
    }

    private fun checkSignUpInput(): Boolean {

        if (_fullName.value != "")
            _fullNameErrorState.value = false
        else {
            _fullNameErrorState.value = true
            _fullNameErrorMessage.value = R.string.full_name_error_message

        }
        val result = checkSignInInputs()

        if (_fullNameErrorState.value!! || result) {
            return true
        }

        return false

    }
//
//    fun signUp(fullName: TextInputEditText, email: TextInputEditText, password: TextInputEditText) {
//        _onDoTask.value = true
//        _canTouchUiSignUp.value = false
//        _signInState.value = ON_SIGN_UP
//
//        viewModelScope.launch {
//            try {
//                val user: User? = mainRepository.updateUser(
//                    User(
//                        fullName.text.toString(), email.text.toString(), password.text.toString()
//                    )
//                )
//
//                viewModelScope.launch {
//                    if (user != null) {
////                        _email.postValue(user.email)
////                        _password.postValue(user.passwrod)
//                        _email.value = user.email
//                        _password.value = user.password
//                        doneSignUp()
//                    } else {
//                        failSignUp()
//                    }
//                }
//            } catch (e: Exception) {
//                failSignUp()
//            }
//        }
//    }

    private fun doneSignUp() {
        _signInState.value = DONE_SIGN_UP
        _signInState.value = NO_ACTION
    }

    private fun failSignUp() {
        _onDoTask.value = false
        _canTouchUiSignUp.value = true
        _signInState.value = FAIL_SIGN_UP
        _signInState.value = NO_ACTION
    }


    private lateinit var _rememberMe: MutableLiveData<Boolean>
    lateinit var rememberMe: LiveData<Boolean>


    fun rememberMeChanged(checkBox: CheckBox) {
        if (rememberMe.value == checkBox.isChecked) return
        _rememberMe.value = checkBox.isChecked
    }

    private lateinit var _fullName: MutableLiveData<String>
    lateinit var fullName: LiveData<String>
    fun rememberFullName(fullName: TextInputEditText) {
        if (this.fullName.value == fullName.text.toString()) return
        _fullName.value = fullName.text.toString()
    }

    private lateinit var _email: MutableLiveData<String>
    lateinit var email: LiveData<String>
    fun rememberEmail(email: TextInputEditText) {
        if (this.email.value == email.text.toString()) return
        _email.value = email.text.toString()
    }

    private lateinit var _password: MutableLiveData<String>
    lateinit var password: LiveData<String>

    fun rememberPassword(password: TextInputEditText) {
        if (this.password.value == password.text.toString()) return
        _password.value = password.text.toString()
    }

}