package com.mariofronza.face_collection_app.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mariofronza.face_collection_app.api.ProfileRequest
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.FragmentProfileBinding
import com.mariofronza.face_collection_app.ui.handle
import com.mariofronza.face_collection_app.ui.toast
import com.mariofronza.face_collection_app.ui.visible
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = requireContext().vision.session.current?.user
        binding.etName.setText(user?.name)
        binding.etEmail.setText(user?.email)
        binding.btnSave.setOnClickListener { validate()?.let(::save) }
    }

    private fun validate(): ProfileRequest? {
        val request = ProfileRequest(
            name = binding.etName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            oldPassword = binding.etCurrentPassword.text.toString(),
            password = binding.etNewPassword.text.toString(),
            passwordConfirmation = binding.etConfirmPassword.text.toString(),
        )
        return when {
            listOf(request.name, request.email, request.oldPassword, request.password).any { it.isEmpty() } ->
                null.also { requireContext().toast("Preencha todos os campos") }
            request.password != request.passwordConfirmation ->
                null.also { requireContext().toast("A confirmação não confere com a nova senha") }
            else -> request
        }
    }

    private fun save(request: ProfileRequest) {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val vision = requireContext().vision
            safeCall { vision.api.updateProfile(request) }
                .onSuccess { user ->
                    vision.session.current = vision.session.current?.copy(user = user)
                    requireContext().toast("Perfil atualizado")
                }
                .onFailure { requireActivity().handle(it) }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSave.isEnabled = !loading
        binding.progress.visible(loading)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
