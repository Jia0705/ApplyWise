package com.team.applywise.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.data.model.JobApplication
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobApplicationRepoFireImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): JobApplicationRepo {
    private val applicationsCollection = firestore.collection("applications")

    override suspend fun createApplication(application: JobApplication) {
        val docRef = applicationsCollection.document()
        val newApplication = application.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        docRef.set(newApplication.toMap()).await()
    }

    override fun getApplicationsByUser(userId: String): Flow<List<JobApplication>> = callbackFlow {
        val listener = applicationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                val applications = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        JobApplication.fromMap(data + ("id" to doc.id))
                    }
                } ?: emptyList()
                
                trySend(applications.sortedByDescending { it.updatedAt })
            }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getApplicationById(id: String): JobApplication? {
        val snapshot = applicationsCollection.document(id).get().await()
        return snapshot.data?.let { data ->
            JobApplication.fromMap(data + ("id" to id))
        }
    }

    override suspend fun updateApplication(application: JobApplication) {
        val updatedApplication = application.copy(updatedAt = System.currentTimeMillis())
        applicationsCollection
            .document(application.id)
            .set(updatedApplication.toMap())
            .await()
    }

    override suspend fun deleteApplication(id: String) {
        applicationsCollection
            .document(id)
            .delete()
            .await()
    }

    override fun getApplicationsByStatus(userId: String, status: ApplicationStatus): Flow<List<JobApplication>> = callbackFlow {
        val listener = applicationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                val applications = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        JobApplication.fromMap(data + ("id" to doc.id))
                    }
                } ?: emptyList()
                
                trySend(applications.sortedByDescending { it.updatedAt })
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getApplicationCounts(userId: String): Map<ApplicationStatus, Int> {
        val snapshot = applicationsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
        
        val applications = snapshot.documents.mapNotNull { doc ->
            doc.data?.let { data ->
                JobApplication.fromMap(data + ("id" to doc.id))
            }
        }
        
        return ApplicationStatus.entries.associateWith { status ->
            applications.count { it.status == status }
        }
    }
}