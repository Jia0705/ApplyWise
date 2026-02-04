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

/**
 * Repository implementation for Job Applications using Firebase Firestore
 * This handles all database operations - Create, Read, Update, Delete (CRUD)
 * Think of this as the bridge between our app and the cloud database
 */
@Singleton
class JobApplicationRepoFireImpl @Inject constructor(
    private val firestore: FirebaseFirestore
): JobApplicationRepo {
    // Reference to "applications" collection in Firestore
    private val applicationsCollection = firestore.collection("applications")

    /**
     * Create a new application in Firestore
     * Firestore automatically generates a unique ID for us
     * Returns the new ID so we can use it for notifications
     */
    override suspend fun createApplication(application: JobApplication): String {
        // Generate a new document with auto ID
        val docRef = applicationsCollection.document()
        // Add the ID and timestamps to the application
        val newApplication = application.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        // Save to Firestore (await = wait until done)
        docRef.set(newApplication.toMap()).await()
        return docRef.id
    }

    /**
     * Get all applications for a user with REAL-TIME updates
     * Returns a Flow that automatically updates when Firestore data changes
     * Example: Add application on phone → Flow sends new list → Screen updates automatically
     */
    override fun getApplicationsByUser(userId: String): Flow<List<JobApplication>> = callbackFlow {
        // Set up a listener for real-time updates
        val listener = applicationsCollection
            .whereEqualTo("userId", userId) // Only get this user's applications
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // If error occurs, just stop (Flow will handle it)
                    return@addSnapshotListener
                }
                
                // Convert Firestore documents to JobApplication objects
                val applications = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { data ->
                        JobApplication.fromMap(data + ("id" to doc.id))
                    }
                } ?: emptyList()
                
                // Send sorted list through Flow (newest updated first)
                trySend(applications.sortedByDescending { it.updatedAt })
            }
        // When Flow is closed, remove the listener to save resources
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Get a single application by ID (one-time fetch, not real-time)
     * Used when viewing application details
     */
    override suspend fun getApplicationById(id: String): JobApplication? {
        val snapshot = applicationsCollection.document(id).get().await()
        return snapshot.data?.let { data ->
            JobApplication.fromMap(data + ("id" to id))
        }
    }

    /**
     * Update an existing application in Firestore
     * Automatically sets updatedAt to current time
     */
    override suspend fun updateApplication(application: JobApplication) {
        val updatedApplication = application.copy(updatedAt = System.currentTimeMillis())
        applicationsCollection
            .document(application.id)
            .set(updatedApplication.toMap())
            .await()
    }

    /**
     * Delete an application from Firestore
     * Warning: This is permanent! No undo.
     */
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