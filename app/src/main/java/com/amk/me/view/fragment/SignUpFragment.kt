package com.amk.me.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.amk.me.R
import com.amk.me.databinding.FragmentSignupBinding
import com.amk.me.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null

    @Inject
    lateinit var viewModel: MainViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        binding.executePendingBindings()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signInButton.setOnClickListener {
            navigateToSignIn()
        }


        viewModel.signInState.observe(viewLifecycleOwner) { t ->
            run {
                when (t) {
                    MainViewModel.NO_ACTION -> {}
                    MainViewModel.ON_SIGN_UP -> {}
                    MainViewModel.FAIL_SIGN_UP -> {
                        showErrorSignUp()
                    }
                    MainViewModel.DONE_SIGN_UP -> {
                        navigateToSignIn()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToSignIn() {
//        findNavController().navigateUp()
        findNavController().popBackStack()
    }

    private fun showErrorSignUp() {
        Toast.makeText(context, R.string.fail_in_signup, Toast.LENGTH_SHORT).show()
    }

}