package ai.tnj.haui.feature.home.ui.agent;

import ai.tnj.haui.core.data.LocalDataStore;
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
public final class AgentViewModel_Factory implements Factory<AgentViewModel> {
  private final Provider<HermesRepository> hermesRepositoryProvider;

  private final Provider<LocalDataStore> localDataStoreProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private AgentViewModel_Factory(Provider<HermesRepository> hermesRepositoryProvider,
      Provider<LocalDataStore> localDataStoreProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.hermesRepositoryProvider = hermesRepositoryProvider;
    this.localDataStoreProvider = localDataStoreProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public AgentViewModel get() {
    return newInstance(hermesRepositoryProvider.get(), localDataStoreProvider.get(), ioDispatcherProvider.get());
  }

  public static AgentViewModel_Factory create(Provider<HermesRepository> hermesRepositoryProvider,
      Provider<LocalDataStore> localDataStoreProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new AgentViewModel_Factory(hermesRepositoryProvider, localDataStoreProvider, ioDispatcherProvider);
  }

  public static AgentViewModel newInstance(HermesRepository hermesRepository,
      LocalDataStore localDataStore, CoroutineDispatcher ioDispatcher) {
    return new AgentViewModel(hermesRepository, localDataStore, ioDispatcher);
  }
}
