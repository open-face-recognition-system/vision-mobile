package com.mariofronza.face_collection_app.ui.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mariofronza.face_collection_app.api.SignUpRequest
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.ActivitySignUpBinding
import com.mariofronza.face_collection_app.ui.MainActivity
import com.mariofronza.face_collection_app.ui.toast
import com.mariofronza.face_collection_app.ui.visible
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignIn.setOnClickListener { finish() }
        binding.btnSignUp.setOnClickListener { validate()?.let(::signUp) }
    }

    private fun validate(): SignUpRequest? {
        val request = SignUpRequest(
            name = binding.etName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            password = binding.etPassword.text.toString(),
            enrollment = binding.etEnrollment.text.toString().trim(),
        )
        return when {
            listOf(request.name, request.email, request.enrollment, request.password).any { it.isEmpty() } ->
                null.also { toast("Preencha todos os campos") }
            request.password.length < 6 -> null.also { toast("A senha precisa ter pelo menos 6 caracteres") }
            else -> request
        }
    }

    private fun signUp(request: SignUpRequest) {
        setLoading(true)
        lifecycleScope.launch {
            safeCall { vision.api.signUp(request) }
                .mapCatching { SignInActivity.signIn(this@SignUpActivity, request.email, request.password).getOrThrow() }
                .onSuccess {
                    toast("Conta criada com sucesso")
                    startActivity(
                        Intent(this@SignUpActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                    )
                }
                .onFailure { toast(it.message) }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignUp.isEnabled = !loading
        binding.progress.visible(loading)
    }
}
