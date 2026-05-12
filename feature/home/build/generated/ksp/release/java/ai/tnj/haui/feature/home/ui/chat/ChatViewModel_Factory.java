package ai.tnj.haui.feature.home.ui.chat;

import ai.tnj.haui.core.data.LocalDataStore;
import ai.tnj.haui.core.data.repository.ChatHistoryRepository;
import ai.tnj.haui.core.data.repository.HermesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata
@QualifierMetadata("ai.tnj.haui.core.data.di.IoDispatcher")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<LocalDataStore> localDataStoreProvider;

  private final Provider<HermesRepository> hermesRepositoryProvider;

  private final Provider<ChatHistoryRepository> chatHistoryRepositoryProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private ChatViewModel_Factory(Provider<LocalDataStore> localDataStoreProvider,
      Provider<HermesRepository> hermesRepositoryProvider,
      Provider<ChatHistoryRepository> chatHistoryRepositoryProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.localDataStoreProvider = localDataStoreProvider;
    this.hermesRepositoryProvider = hermesRepositoryProvider;
    this.chatHistoryRepositoryProvider = chatHistoryRepositoryProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(localDataStoreProvider.get(), hermesRepositoryProvider.get(), chatHistoryRepositoryProvider.get(), ioDispatcherProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<LocalDataStore> localDataStoreProvider,
      Provider<HermesRepository> hermesRepositoryProvider,
      Provider<ChatHistoryRepository> chatHistoryRepositoryProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new ChatViewModel_Factory(localDataStoreProvider, hermesRepositoryProvider, chatHistoryRepositoryProvider, ioDispatcherProvider);
  }

  public static ChatViewModel newInstance(LocalDataStore localDataStore,
      HermesRepository hermesRepository, ChatHistoryRepository chatHistoryRepository,
      CoroutineDispatcher ioDispatcher) {
    return new ChatViewModel(localDataStore, hermesRepository, chatHistoryRepository, ioDispatcher);
  }
}
