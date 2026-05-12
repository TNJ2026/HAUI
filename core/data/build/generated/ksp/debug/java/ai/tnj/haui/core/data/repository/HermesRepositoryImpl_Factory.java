package ai.tnj.haui.core.data.repository;

import ai.tnj.haui.core.network.HermesEndpoint;
import ai.tnj.haui.core.network.HermesService;
import ai.tnj.haui.core.network.NetworkManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class HermesRepositoryImpl_Factory implements Factory<HermesRepositoryImpl> {
  private final Provider<NetworkManager> networkManagerProvider;

  private final Provider<HermesService> serviceProvider;

  private final Provider<HermesEndpoint> endpointProvider;

  private HermesRepositoryImpl_Factory(Provider<NetworkManager> networkManagerProvider,
      Provider<HermesService> serviceProvider, Provider<HermesEndpoint> endpointProvider) {
    this.networkManagerProvider = networkManagerProvider;
    this.serviceProvider = serviceProvider;
    this.endpointProvider = endpointProvider;
  }

  @Override
  public HermesRepositoryImpl get() {
    return newInstance(networkManagerProvider.get(), serviceProvider.get(), endpointProvider.get());
  }

  public static HermesRepositoryImpl_Factory create(Provider<NetworkManager> networkManagerProvider,
      Provider<HermesService> serviceProvider, Provider<HermesEndpoint> endpointProvider) {
    return new HermesRepositoryImpl_Factory(networkManagerProvider, serviceProvider, endpointProvider);
  }

  public static HermesRepositoryImpl newInstance(NetworkManager networkManager,
      HermesService service, HermesEndpoint endpoint) {
    return new HermesRepositoryImpl(networkManager, service, endpoint);
  }
}
