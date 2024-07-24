package com.example.carpoolapp.ui.log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.carpoolapp.R
import com.example.carpoolapp.databinding.LogInFragmentBinding
import com.example.carpoolapp.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: LogInFragmentBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: LoginViewModel by activityViewModels()

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = LogInFragmentBinding.inflate(inflater, container, false)

        auth = (activity as MainActivity).auth

        CoroutineScope(Dispatchers.Main).launch {

            val userD = async(Dispatchers.IO) { viewModel.getUser() }
            val user = userD.await()

            user?.let {

                binding.userLine.visibility = View.VISIBLE
                binding.btnContinue.visibility = View.VISIBLE
                val continueAs =
                    StringBuilder(getString(R.string.continue_as) + getUsername(it.email))
                binding.btnContinue.text = continueAs

                binding.btnContinue.setOnClickListener {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_findFragment,
                        bundleOf("user_id" to user.id)
                    )
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email != "" && password != "") {
                loginFunc(email, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_input),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        return binding.root
    }

    private fun loginFunc(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_findFragment,
                        bundleOf("user_id" to auth.currentUser?.uid)
                    )
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.email_password_incorrect),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun getUsername(email: String): String {
        return email.substringBefore("@")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}