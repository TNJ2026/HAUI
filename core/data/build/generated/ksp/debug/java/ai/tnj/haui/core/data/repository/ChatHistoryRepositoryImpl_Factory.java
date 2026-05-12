package ai.tnj.haui.core.data.repository;

import ai.tnj.haui.core.data.db.dao.ChatMessageDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ChatHistoryRepositoryImpl_Factory implements Factory<ChatHistoryRepositoryImpl> {
  private final Provider<ChatMessageDao> daoProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private ChatHistoryRepositoryImpl_Factory(Provider<ChatMessageDao> daoProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.daoProvider = daoProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public ChatHistoryRepositoryImpl get() {
    return newInstance(daoProvider.get(), ioDispatcherProvider.get());
  }

  public static ChatHistoryRepositoryImpl_Factory create(Provider<ChatMessageDao> daoProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new ChatHistoryRepositoryImpl_Factory(daoProvider, ioDispatcherProvider);
  }

  public static ChatHistoryRepositoryImpl newInstance(ChatMessageDao dao,
      CoroutineDispatcher ioDispatcher) {
    return new ChatHistoryRepositoryImpl(dao, ioDispatcher);
  }
}
