package com.tesis.ecoriego

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun signIn(email: String, pass: String, cb: (Result<Unit>) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) cb(Result.success(Unit))
                else cb(Result.failure(task.exception ?: Exception("Error al iniciar sesión")))
            }
    }

    fun signUp(name: String, email: String, pass: String, cb: (Result<Unit>) -> Unit) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        auth.createUserWithEmailAndPassword(cleanEmail, pass)
            .addOnCompleteListener { createTask ->
                if (!createTask.isSuccessful) {
                    cb(Result.failure(createTask.exception ?: Exception("No se pudo crear la cuenta")))
                    return@addOnCompleteListener
                }

                val user = auth.currentUser ?: run {
                    cb(Result.failure(Exception("Usuario no encontrado tras crear cuenta"))); return@addOnCompleteListener
                }

                val req = UserProfileChangeRequest.Builder()
                    .setDisplayName(cleanName)
                    .build()

                user.updateProfile(req).addOnCompleteListener { updTask ->
                    if (!updTask.isSuccessful) {
                        cb(Result.failure(updTask.exception ?: Exception("No se pudo guardar el nombre")))
                        return@addOnCompleteListener
                    }

                    val doc = mapOf(
                        "uid" to user.uid,
                        "name" to cleanName,
                        "email" to cleanEmail,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("users").document(user.uid)
                        .set(doc)
                        .addOnSuccessListener { cb(Result.success(Unit)) }
                        .addOnFailureListener { e -> cb(Result.failure(e)) }
                }
            }
    }


    fun signOut() { auth.signOut() }

    fun fetchNameFromFirestore(uid: String, cb: (String?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap -> cb(snap.getString("name")) }
            .addOnFailureListener { cb(null) }
    }

    fun updateDisplayName(newName: String, cb: (Result<Unit>) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            cb(Result.failure(IllegalStateException("No hay usuario logueado")))
            return
        }

        val clean = newName.trim()
        val req = UserProfileChangeRequest.Builder()
            .setDisplayName(clean)
            .build()

        // 1) Actualiza el displayName en Firebase Auth
        user.updateProfile(req).addOnCompleteListener { updTask ->
            if (!updTask.isSuccessful) {
                cb(Result.failure(updTask.exception ?: Exception("No se pudo actualizar el nombre en Auth")))
                return@addOnCompleteListener
            }

            // 2) Refleja el cambio también en Firestore (/users/{uid})
            db.collection("users").document(user.uid)
                // merge para no pisar otros campos si en futuro agregas más
                .set(mapOf("name" to clean), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { cb(Result.success(Unit)) }
                .addOnFailureListener { e -> cb(Result.failure(e)) }
        }
    }


}
