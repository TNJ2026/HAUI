package ai.tnj.haui.core.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class LocalDataStore_Factory implements Factory<LocalDataStore> {
  private final Provider<Context> contextProvider;

  private LocalDataStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LocalDataStore get() {
    return newInstance(contextProvider.get());
  }

  public static LocalDataStore_Factory create(Provider<Context> contextProvider) {
    return new LocalDataStore_Factory(contextProvider);
  }

  public static LocalDataStore newInstance(Context context) {
    return new LocalDataStore(context);
  }
}
