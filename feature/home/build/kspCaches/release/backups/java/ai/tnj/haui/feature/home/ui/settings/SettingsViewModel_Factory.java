package ai.tnj.haui.feature.home.ui.settings;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<LocalDataStore> localDataStoreProvider;

  private final Provider<HermesRepository> hermesRepositoryProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private SettingsViewModel_Factory(Provider<LocalDataStore> localDataStoreProvider,
      Provider<HermesRepository> hermesRepositoryProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.localDataStoreProvider = localDataStoreProvider;
    this.hermesRepositoryProvider = hermesRepositoryProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(localDataStoreProvider.get(), hermesRepositoryProvider.get(), ioDispatcherProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<LocalDataStore> localDataStoreProvider,
      Provider<HermesRepository> hermesRepositoryProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new SettingsViewModel_Factory(localDataStoreProvider, hermesRepositoryProvider, ioDispatcherProvider);
  }

  public static SettingsViewModel newInstance(LocalDataStore localDataStore,
      HermesRepository hermesRepository, CoroutineDispatcher ioDispatcher) {
    return new SettingsViewModel(localDataStore, hermesRepository, ioDispatcher);
  }
}
