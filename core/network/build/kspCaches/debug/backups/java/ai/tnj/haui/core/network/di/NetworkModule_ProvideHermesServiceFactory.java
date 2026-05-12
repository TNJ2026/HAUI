package ai.tnj.haui.core.network.di;

import ai.tnj.haui.core.network.HermesService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideHermesServiceFactory implements Factory<HermesService> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_ProvideHermesServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public HermesService get() {
    return provideHermesService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideHermesServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideHermesServiceFactory(retrofitProvider);
  }

  public static HermesService provideHermesService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideHermesService(retrofit));
  }
}
