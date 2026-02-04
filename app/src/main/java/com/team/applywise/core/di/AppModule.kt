package com.team.applywise.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team.applywise.data.repo.JobApplicationRepo
import com.team.applywise.data.repo.JobApplicationRepoFireImpl
import com.team.applywise.data.repo.UserRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule - Dependency Injection setup for the entire app
 * 
 * What is Dependency Injection?
 * Instead of creating objects manually (like `FirebaseAuth.getInstance()`),
 * Hilt automatically creates and provides them when needed.
 * 
 * Benefits:
 * - Single instances (Singleton) shared across the app
 * - Easy to test (can replace with fake objects)
 * - Clean code (no manual creation everywhere)
 * 
 * This module provides:
 * - FirebaseAuth (for authentication)
 * - FirebaseFirestore (for database)
 * - UserRepo (for user operations)
 * - JobApplicationRepo (for application operations)
 * 
 * @Module tells Hilt this class provides dependencies
 * @InstallIn(SingletonComponent::class) makes them available app-wide
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide FirebaseAuth instance
     * @Singleton ensures only ONE instance exists in the entire app
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provide FirebaseFirestore instance
     * This is our cloud database
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /**
     * Provide UserRepo
     * Hilt automatically passes FirebaseFirestore (from above) to the constructor
     */
    @Provides
    @Singleton
    fun provideUserRepo(firestore: FirebaseFirestore): UserRepo {
        return UserRepo(firestore)
    }

    /**
     * Provide JobApplicationRepo
     * Returns the Firestore implementation
     * Hilt knows to use this when ViewModels ask for JobApplicationRepo
     */
    @Provides
    @Singleton
    fun provideJobApplicationRepo(firestore: FirebaseFirestore): JobApplicationRepo {
        return JobApplicationRepoFireImpl(firestore)
    }
}