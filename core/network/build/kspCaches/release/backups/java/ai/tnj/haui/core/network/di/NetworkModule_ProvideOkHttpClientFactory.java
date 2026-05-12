package ai.tnj.haui.core.network.di;

import ai.tnj.haui.core.network.HermesEndpointInterceptor;
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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<HermesEndpointInterceptor> endpointInterceptorProvider;

  private NetworkModule_ProvideOkHttpClientFactory(
      Provider<HermesEndpointInterceptor> endpointInterceptorProvider) {
    this.endpointInterceptorProvider = endpointInterceptorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(endpointInterceptorProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(
      Provider<HermesEndpointInterceptor> endpointInterceptorProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(endpointInterceptorProvider);
  }

  public static OkHttpClient provideOkHttpClient(HermesEndpointInterceptor endpointInterceptor) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideOkHttpClient(endpointInterceptor));
  }
}
