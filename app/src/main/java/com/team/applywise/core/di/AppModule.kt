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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserRepo(firestore: FirebaseFirestore): UserRepo {
        return UserRepo(firestore)
    }

    @Provides
    @Singleton
    fun provideJobApplicationRepo(firestore: FirebaseFirestore): JobApplicationRepo {
        return JobApplicationRepoFireImpl(firestore)
    }
}