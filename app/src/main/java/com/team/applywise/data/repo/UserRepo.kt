package com.team.applywise.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.team.applywise.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepo @Inject constructor(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User) {
        val docRef = usersCollection.document(user.uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            docRef.set(user).await()
        }
    }

    suspend fun getUser(uid: String): User? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(User::class.java)
    }
}