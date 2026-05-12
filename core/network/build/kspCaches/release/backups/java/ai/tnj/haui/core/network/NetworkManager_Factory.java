package ai.tnj.haui.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NetworkManager_Factory implements Factory<NetworkManager> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private NetworkManager_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public NetworkManager get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static NetworkManager_Factory create(Provider<OkHttpClient> okHttpClientProvider) {
    return new NetworkManager_Factory(okHttpClientProvider);
  }

  public static NetworkManager newInstance(OkHttpClient okHttpClient) {
    return new NetworkManager(okHttpClient);
  }
}
