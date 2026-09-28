package com.vsoft.app

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

suspend fun signInWithGoogle(
    context: Context,
    serverClientId: String
): FirebaseUser {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(serverClientId)
        .setFilterByAuthorizedAccounts(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(
        context = context,
        request = request
    )

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

    val firebaseCredential = GoogleAuthProvider.getCredential(
        googleIdTokenCredential.idToken,
        null
    )

    val authResult = FirebaseAuth.getInstance()
        .signInWithCredential(firebaseCredential)
        .awaitResult()

    return authResult.user
        ?: error("Firebase did not return a signed-in user")
}

suspend fun signOutFromGoogle(context: Context) {
    FirebaseAuth.getInstance().signOut()
    runCatching {
        CredentialManager.create(context)
            .clearCredentialState(ClearCredentialStateRequest())
    }
}
