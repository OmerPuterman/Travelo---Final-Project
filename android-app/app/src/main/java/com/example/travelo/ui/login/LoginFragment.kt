package com.example.travelo.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.travelo.R
import com.example.travelo.databinding.FragmentLoginBinding
import com.example.travelo.model.User
import com.example.travelo.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private var isRegistering = false
    private var selectedRole = "TRAVELER"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.roleGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedRole = when (checkedId) {
                R.id.roleGuide -> "GUIDE"
                R.id.roleBusiness -> "BUSINESS"
                else -> "TRAVELER"
            }
        }

        binding.toggleModeButton.setOnClickListener {
            isRegistering = !isRegistering
            updateModeUi()
        }

        binding.submitButton.setOnClickListener { onSubmit() }

        updateModeUi()
    }

    private fun updateModeUi() {
        binding.subtitle.text = if (isRegistering) "Create an Account" else "Welcome Back"
        binding.nameSection.visibility = if (isRegistering) View.VISIBLE else View.GONE
        binding.loginAnimation.visibility = if (isRegistering) View.GONE else View.VISIBLE
        binding.submitButton.text = if (isRegistering) "Sign Up" else "Login"
        binding.toggleModeButton.text = if (isRegistering)
            "Already have an account? Log in" else "Don't have an account? Sign up"
    }

    private fun setLoading(loading: Boolean) {
        binding.submitButton.isEnabled = !loading
        binding.loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        binding.submitButton.text = if (loading) "" else if (isRegistering) "Sign Up" else "Login"
    }

    private fun onSubmit() {
        val name = binding.nameInput.text?.toString().orEmpty()
        val email = binding.emailInput.text?.toString().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        if (email.isBlank() || password.isBlank() || (isRegistering && name.isBlank())) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        if (isRegistering) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid ?: ""
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val newUser = User(uid, name, email, selectedRole)
                                RetrofitInstance.api.registerUser(newUser)
                                withContext(Dispatchers.Main) {
                                    setLoading(false)
                                    navigateToRole(selectedRole)
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) { setLoading(false) }
                            }
                        }
                    } else {
                        setLoading(false)
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid ?: ""
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val response = RetrofitInstance.api.getUser(uid)
                                withContext(Dispatchers.Main) {
                                    setLoading(false)
                                    if (response.isSuccessful && response.body() != null) {
                                        navigateToRole(response.body()!!.role)
                                    } else {
                                        Toast.makeText(requireContext(), "Error fetching user role", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    setLoading(false)
                                    Toast.makeText(requireContext(), "Server connection failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        setLoading(false)
                        Toast.makeText(requireContext(), "Invalid Credentials", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun navigateToRole(role: String) {
        val actionId = when (role) {
            "GUIDE" -> R.id.action_login_to_guideDashboard
            "BUSINESS" -> R.id.action_login_to_businessDashboard
            else -> R.id.action_login_to_travelerDashboard
        }
        findNavController().navigate(actionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}