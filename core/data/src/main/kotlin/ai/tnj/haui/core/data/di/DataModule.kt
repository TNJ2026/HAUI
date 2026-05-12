package ai.tnj.haui.core.data.di

import ai.tnj.haui.core.data.repository.ChatHistoryRepository
import ai.tnj.haui.core.data.repository.ChatHistoryRepositoryImpl
import ai.tnj.haui.core.data.repository.HermesRepository
import ai.tnj.haui.core.data.repository.HermesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds
    @Singleton
    abstract fun bindHermesRepository(
        impl: HermesRepositoryImpl
    ): HermesRepository

    @Binds
    @Singleton
    abstract fun bindChatHistoryRepository(
        impl: ChatHistoryRepositoryImpl
    ): ChatHistoryRepository
}
