package ai.tnj.haui.core.data.di;

import ai.tnj.haui.core.data.db.HauiDatabase;
import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideHauiDatabaseFactory implements Factory<HauiDatabase> {
  private final Provider<Context> contextProvider;

  private DatabaseModule_ProvideHauiDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public HauiDatabase get() {
    return provideHauiDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideHauiDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideHauiDatabaseFactory(contextProvider);
  }

  public static HauiDatabase provideHauiDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideHauiDatabase(context));
  }
}
