package ai.tnj.haui;

import ai.tnj.haui.core.data.LocalDataStore;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class HAUIApplication_MembersInjector implements MembersInjector<HAUIApplication> {
  private final Provider<LocalDataStore> localDataStoreProvider;

  private HAUIApplication_MembersInjector(Provider<LocalDataStore> localDataStoreProvider) {
    this.localDataStoreProvider = localDataStoreProvider;
  }

  @Override
  public void injectMembers(HAUIApplication instance) {
    injectLocalDataStore(instance, localDataStoreProvider.get());
  }

  public static MembersInjector<HAUIApplication> create(
      Provider<LocalDataStore> localDataStoreProvider) {
    return new HAUIApplication_MembersInjector(localDataStoreProvider);
  }

  @InjectedFieldSignature("ai.tnj.haui.HAUIApplication.localDataStore")
  public static void injectLocalDataStore(HAUIApplication instance, LocalDataStore localDataStore) {
    instance.localDataStore = localDataStore;
  }
}
