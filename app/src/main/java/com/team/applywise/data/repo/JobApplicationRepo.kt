package com.team.applywise.data.repo

import com.team.applywise.data.model.ApplicationStatus
import com.team.applywise.data.model.JobApplication
import kotlinx.coroutines.flow.Flow

interface JobApplicationRepo {
    suspend fun createApplication(application: JobApplication)
    fun getApplicationsByUser(userId: String): Flow<List<JobApplication>>
    suspend fun getApplicationById(id: String): JobApplication?
    suspend fun updateApplication(application: JobApplication)
    suspend fun deleteApplication(id: String)
    fun getApplicationsByStatus(userId: String, status: ApplicationStatus): Flow<List<JobApplication>>
    suspend fun getApplicationCounts(userId: String): Map<ApplicationStatus, Int>
}