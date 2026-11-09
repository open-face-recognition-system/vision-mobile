package com.mariofronza.face_collection_app.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mariofronza.face_collection_app.api.SessionRequest
import com.mariofronza.face_collection_app.api.safeCall
import com.mariofronza.face_collection_app.databinding.ActivitySignInBinding
import com.mariofronza.face_collection_app.ui.MainActivity
import com.mariofronza.face_collection_app.ui.toast
import com.mariofronza.face_collection_app.ui.visible
import com.mariofronza.face_collection_app.vision
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvCreateAccount.setOnClickListener { startActivity(Intent(this, SignUpActivity::class.java)) }
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) toast("Preencha todos os campos") else signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        setLoading(true)
        lifecycleScope.launch {
            signIn(this@SignInActivity, email, password)
                .onSuccess { openApp() }
                .onFailure { toast(it.message) }
            setLoading(false)
        }
    }

    private fun openApp() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignIn.isEnabled = !loading
        binding.progress.visible(loading)
    }

    companion object {
        /** Signs in a student or teacher and stores the session; admins use the web panel. */
        suspend fun signIn(context: Context, email: String, password: String): Result<Unit> =
            safeCall { context.vision.api.signIn(SessionRequest(email, password)) }.mapCatching { session ->
                if (session.user.role == "admin") error("Administradores usam o painel web do Vision")
                context.vision.session.current = session
            }
    }
}
