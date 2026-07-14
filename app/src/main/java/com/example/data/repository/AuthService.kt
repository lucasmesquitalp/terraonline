package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.UserDao
import com.example.data.model.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthService(
    private val context: Context,
    private val userDao: UserDao
) {
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        return false
    }

    private fun getFirebaseAuthSafe(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.w("AuthService", "Firebase not initialized.")
                null
            } else {
                FirebaseAuth.getInstance()
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Failed to retrieve FirebaseAuth: ${e.message}")
            null
        }
    }

    private fun getFirebaseFirestoreSafe(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                null
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Failed to retrieve FirebaseFirestore: ${e.message}")
            null
        }
    }

    suspend fun checkActiveSession(): User? = withContext(Dispatchers.IO) {
        try {
            val auth = getFirebaseAuthSafe() ?: return@withContext getOfflineSession()
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val role = if (firebaseUser.isAnonymous) "Convidado" else "Jogador"
                val email = firebaseUser.email ?: ""
                val displayName = firebaseUser.displayName ?: if (firebaseUser.isAnonymous) "Convidado" else "Jogador"
                
                val localUser = userDao.getUserById(uid)
                if (localUser != null) {
                    localUser
                } else {
                    val newUser = User(id = uid, email = email, displayName = displayName, role = role)
                    userDao.insertUser(newUser)
                    newUser
                }
            } else {
                getOfflineSession()
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Error during active session check: ${e.message}", e)
            getOfflineSession()
        }
    }

    private suspend fun getOfflineSession(): User? {
        return try {
            val users = userDao.getAllUsers()
            users.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithGoogleToken(idToken: String): User = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            throw Exception("Erro: Sem conexão com a internet. Verifique sua rede e tente novamente.")
        }

        val auth = getFirebaseAuthSafe() ?: throw Exception("Erro: Firebase não inicializado corretamente.")

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = Tasks.await(auth.signInWithCredential(credential))
            val firebaseUser = authResult.user ?: throw Exception("Falha ao obter usuário do Firebase")
            
            val uid = firebaseUser.uid
            val email = firebaseUser.email ?: ""
            val displayName = firebaseUser.displayName ?: "Jogador"
            val photoUrl = firebaseUser.photoUrl?.toString() ?: ""
            
            var role = "Jogador"
            val firestore = getFirebaseFirestoreSafe() ?: throw Exception("Erro: Firebase não inicializado corretamente (Firestore indisponível).")
            
            val docRef = firestore.collection("users").document(uid)
            try {
                val document = Tasks.await(docRef.get())
                if (document.exists()) {
                    role = document.getString("role") ?: "Jogador"
                    val updateData = mapOf("lastLogin" to System.currentTimeMillis())
                    Tasks.await(docRef.update(updateData))
                    Log.d("AuthService", "Firestore: Existing Google user lastLogin updated.")
                } else {
                    val userData = mapOf(
                        "uid" to uid,
                        "displayName" to displayName,
                        "email" to email,
                        "photoURL" to photoUrl,
                        "role" to role,
                        "createdAt" to System.currentTimeMillis(),
                        "lastLogin" to System.currentTimeMillis()
                    )
                    Tasks.await(docRef.set(userData))
                    Log.d("AuthService", "Firestore: New Google user document created.")
                }
            } catch (fe: Exception) {
                Log.e("AuthService", "Firestore operation failed: ${fe.message}", fe)
                val feMsg = fe.message ?: ""
                if (feMsg.contains("PERMISSION_DENIED", ignoreCase = true) || feMsg.contains("permission-denied", ignoreCase = true) || feMsg.contains("permissão", ignoreCase = true)) {
                    throw Exception("Erro: Firestore sem permissão (permissão negada para o usuário).")
                } else {
                    throw Exception("Erro no Firestore: ${fe.localizedMessage}")
                }
            }
            
            val user = User(id = uid, email = email, displayName = displayName, role = role)
            userDao.insertUser(user)
            user
        } catch (e: Exception) {
            Log.e("AuthService", "Google Firebase Login failed: ${e.message}", e)
            val msg = e.message ?: ""
            if (msg.startsWith("Erro:")) {
                throw e
            }
            if (msg.contains("DEVELOPER_ERROR", ignoreCase = true) || msg.contains("10") || msg.contains("12500")) {
                throw Exception("Erro: Assinatura SHA-1 do aplicativo inválida ou não cadastrada no Console do Firebase.")
            } else if (msg.contains("network", ignoreCase = true) || msg.contains("UnknownHostException", ignoreCase = true)) {
                throw Exception("Erro: Sem conexão com a internet. Verifique sua rede e tente novamente.")
            } else {
                throw Exception("Erro na autenticação: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }
    }

    suspend fun signInAnonymously(): User = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            throw Exception("Erro: Sem conexão com a internet. Verifique sua rede e tente novamente.")
        }

        val auth = getFirebaseAuthSafe() ?: throw Exception("Erro: Firebase não inicializado corretamente.")

        try {
            val authResult = Tasks.await(auth.signInAnonymously())
            val firebaseUser = authResult.user ?: throw Exception("Falha ao autenticar como convidado")
            
            val uid = firebaseUser.uid
            val displayName = "Convidado"
            val role = "Convidado"
            
            val firestore = getFirebaseFirestoreSafe() ?: throw Exception("Erro: Firebase não inicializado corretamente (Firestore indisponível).")
            val docRef = firestore.collection("users").document(uid)
            try {
                val document = Tasks.await(docRef.get())
                if (!document.exists()) {
                    val userData = mapOf(
                        "uid" to uid,
                        "displayName" to displayName,
                        "role" to role,
                        "createdAt" to System.currentTimeMillis()
                    )
                    Tasks.await(docRef.set(userData))
                    Log.d("AuthService", "Firestore: New guest user document created.")
                }
            } catch (fe: Exception) {
                Log.e("AuthService", "Firestore operation failed for guest: ${fe.message}", fe)
                val feMsg = fe.message ?: ""
                if (feMsg.contains("PERMISSION_DENIED", ignoreCase = true) || feMsg.contains("permission-denied", ignoreCase = true) || feMsg.contains("permissão", ignoreCase = true)) {
                    throw Exception("Erro: Firestore sem permissão (permissão negada para o usuário).")
                } else {
                    throw Exception("Erro no Firestore: ${fe.localizedMessage}")
                }
            }
            
            val user = User(id = uid, email = "", displayName = displayName, role = role)
            userDao.insertUser(user)
            user
        } catch (e: Exception) {
            Log.e("AuthService", "Anonymous Firebase Login failed: ${e.message}", e)
            val msg = e.message ?: ""
            if (msg.startsWith("Erro:")) {
                throw e
            }
            if (msg.contains("network", ignoreCase = true) || msg.contains("UnknownHostException", ignoreCase = true)) {
                throw Exception("Erro: Sem conexão com a internet. Verifique sua rede e tente novamente.")
            } else {
                throw Exception("Erro ao entrar como convidado: ${e.localizedMessage ?: "Erro desconhecido"}")
            }
        }
    }

    fun logout() {
        try {
            getFirebaseAuthSafe()?.signOut()
        } catch (e: Exception) {
            Log.e("AuthService", "Error during signout: ${e.message}")
        }
    }
}
