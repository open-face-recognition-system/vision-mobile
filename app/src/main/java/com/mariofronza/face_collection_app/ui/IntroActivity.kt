package com.mariofronza.face_collection_app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mariofronza.face_collection_app.databinding.ActivityIntroBinding
import com.mariofronza.face_collection_app.ui.user.SignInActivity
import com.mariofronza.face_collection_app.vision

/** Presents the consent term; the app is only usable after it is accepted. */
class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cbAccept.setOnCheckedChangeListener { _, checked -> binding.btnContinue.isEnabled = checked }
        binding.btnContinue.setOnClickListener {
            vision.consented = true
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
}
