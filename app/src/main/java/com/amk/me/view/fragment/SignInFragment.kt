package com.amk.me.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.amk.me.R
import com.amk.me.databinding.FragmentSigninBinding
import com.amk.me.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SignInFragment() : Fragment() {

    private var _binding: FragmentSigninBinding? = null

    @Inject
    lateinit var viewmodel: MainViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.e("TAG", "onCreateView: SignInFragment")
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewmodel
        binding.executePendingBindings()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.signUpButton.setOnClickListener {
            navigateToSignUp()
        }

        viewmodel.signInState.observe(viewLifecycleOwner) { t ->
            run {
                when (t) {
                    MainViewModel.NO_ACTION -> {}
                    MainViewModel.ON_SIGN_IN -> {}
                    MainViewModel.FAIL_SIGN_IN -> {
                        showErrorSignIn()
                    }
                    MainViewModel.DONE_SIGN_IN -> {
                        navigateToProfileActivity()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToProfileActivity() {
        findNavController().navigate(R.id.action_FirstFragment_to_profileActivity)
    }

    private fun navigateToSignUp() {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    private fun showErrorSignIn() {
        Toast.makeText(context, R.string.fail_in_signin, Toast.LENGTH_SHORT).show()
    }


}