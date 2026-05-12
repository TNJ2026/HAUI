package ai.tnj.haui.core.network;

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
public final class HermesEndpointInterceptor_Factory implements Factory<HermesEndpointInterceptor> {
  private final Provider<HermesEndpoint> endpointProvider;

  private HermesEndpointInterceptor_Factory(Provider<HermesEndpoint> endpointProvider) {
    this.endpointProvider = endpointProvider;
  }

  @Override
  public HermesEndpointInterceptor get() {
    return newInstance(endpointProvider.get());
  }

  public static HermesEndpointInterceptor_Factory create(
      Provider<HermesEndpoint> endpointProvider) {
    return new HermesEndpointInterceptor_Factory(endpointProvider);
  }

  public static HermesEndpointInterceptor newInstance(HermesEndpoint endpoint) {
    return new HermesEndpointInterceptor(endpoint);
  }
}
