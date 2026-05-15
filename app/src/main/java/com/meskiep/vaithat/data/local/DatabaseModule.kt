package com.meskiep.vaithat.data.local

import android.content.Context
import androidx.room.Room
import com.meskiep.vaithat.data.local.data_character.DataCharacterDAO
import com.meskiep.vaithat.data.local.edit.EditCharacterDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideDataCharacterDAO(appDatabase: AppDatabase): DataCharacterDAO {
        return appDatabase.dataCharacterDao()
    }

    @Provides
    fun provideEditCharacterDAO(appDatabase: AppDatabase): EditCharacterDAO {
        return appDatabase.editCharacterDao()
    }
}