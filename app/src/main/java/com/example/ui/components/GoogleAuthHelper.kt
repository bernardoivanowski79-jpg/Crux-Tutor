package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.R

fun performGoogleSignIn(
    context: Context,
    coroutineScope: CoroutineScope,
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    coroutineScope.launch {
        try {
            val credentialManager = CredentialManager.create(context)
            val webClientId = context.getString(R.string.default_web_client_id)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                
                // Authenticate with Firebase
                val firebaseAuth = FirebaseAuth.getInstance()
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                
                val user = authResult.user
                if (user != null) {
                    onSuccess(user.displayName ?: "Usuário Google", user.email ?: "")
                } else {
                    onError("Falha ao obter usuário do Firebase.")
                }
            } else {
                onError("Tipo de credencial inválido.")
            }

        } catch (e: Exception) {
            Log.e("GoogleLogin", "Error during sign in", e)
            onError(e.localizedMessage ?: "Erro desconhecido")
        }
    }
}
