package com.mariofronza.face_collection_app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.mariofronza.face_collection_app.api.ApiError
import com.mariofronza.face_collection_app.ui.user.SignInActivity
import com.mariofronza.face_collection_app.vision

fun Context.toast(message: String?) = Toast.makeText(this, message ?: "Erro inesperado", Toast.LENGTH_SHORT).show()

fun View.visible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/** Shows the failure, sending the user back to sign in when the session is gone. */
fun Activity.handle(error: Throwable) {
    toast(error.message)
    if ((error as? ApiError)?.status == 401 && vision.session.current == null) signOut()
}

fun Activity.signOut() {
    vision.session.current = null
    startActivity(Intent(this, SignInActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
}
