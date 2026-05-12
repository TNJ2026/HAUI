package ai.tnj.haui.core.data.di;

import ai.tnj.haui.core.data.db.HauiDatabase;
import ai.tnj.haui.core.data.db.dao.ChatMessageDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class DatabaseModule_ProvideChatMessageDaoFactory implements Factory<ChatMessageDao> {
  private final Provider<HauiDatabase> databaseProvider;

  private DatabaseModule_ProvideChatMessageDaoFactory(Provider<HauiDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ChatMessageDao get() {
    return provideChatMessageDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideChatMessageDaoFactory create(
      Provider<HauiDatabase> databaseProvider) {
    return new DatabaseModule_ProvideChatMessageDaoFactory(databaseProvider);
  }

  public static ChatMessageDao provideChatMessageDao(HauiDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideChatMessageDao(database));
  }
}
