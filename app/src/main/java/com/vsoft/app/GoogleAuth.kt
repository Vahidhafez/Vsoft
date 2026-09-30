package com.vsoft.app

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun createGoogleSignInClient(context: Context, serverClientId: String): GoogleSignInClient {
    require(serverClientId.isNotBlank()) { "Google server client ID is missing" }
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(serverClientId)
        .requestEmail()
        .build()
    return GoogleSignIn.getClient(context, options)
}

suspend fun firebaseSignInWithGoogleIdToken(idToken: String): FirebaseUser {
    require(idToken.isNotBlank()) { "Google ID token is missing" }
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    val authResult = suspendCoroutine<com.google.firebase.auth.AuthResult> { continuation ->
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
    return authResult.user ?: error("Firebase did not return a signed-in user")
}

fun signOutFromGoogle(context: Context) {
    FirebaseAuth.getInstance().signOut()
    GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    ).signOut()
}
