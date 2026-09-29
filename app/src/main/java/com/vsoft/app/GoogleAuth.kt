package com.vsoft.app

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun signInWithGoogle(context: Context, serverClientId: String): FirebaseUser {
    require(serverClientId.isNotBlank()) { "Google server client ID is missing" }
    val credentialManager = CredentialManager.create(context)
    val googleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(googleOption).build()
    val result = credentialManager.getCredential(context = context, request = request)
    val credential = result.credential
    if (credential !is CustomCredential ||
        credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        error("Google credential was not returned")
    }
    val googleIdTokenCredential = try {
        GoogleIdTokenCredential.createFrom(credential.data)
    } catch (e: GoogleIdTokenParsingException) {
        throw IllegalStateException("Could not read Google account credential", e)
    }
    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
    val authResult = suspendCoroutine<com.google.firebase.auth.AuthResult> { continuation ->
        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
    return authResult.user ?: error("Firebase did not return a signed-in user")
}

suspend fun signOutFromGoogle(context: Context) {
    FirebaseAuth.getInstance().signOut()
    runCatching {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }
}
