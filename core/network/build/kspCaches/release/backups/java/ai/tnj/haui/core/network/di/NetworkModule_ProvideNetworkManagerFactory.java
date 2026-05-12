package ai.tnj.haui.core.network.di;

import ai.tnj.haui.core.network.NetworkManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class NetworkModule_ProvideNetworkManagerFactory implements Factory<NetworkManager> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private NetworkModule_ProvideNetworkManagerFactory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public NetworkManager get() {
    return provideNetworkManager(okHttpClientProvider.get());
  }

  public static NetworkModule_ProvideNetworkManagerFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new NetworkModule_ProvideNetworkManagerFactory(okHttpClientProvider);
  }

  public static NetworkManager provideNetworkManager(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideNetworkManager(okHttpClient));
  }
}
