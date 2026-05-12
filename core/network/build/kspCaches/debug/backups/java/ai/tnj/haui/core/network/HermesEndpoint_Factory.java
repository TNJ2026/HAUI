package ai.tnj.haui.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class HermesEndpoint_Factory implements Factory<HermesEndpoint> {
  @Override
  public HermesEndpoint get() {
    return newInstance();
  }

  public static HermesEndpoint_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HermesEndpoint newInstance() {
    return new HermesEndpoint();
  }

  private static final class InstanceHolder {
    static final HermesEndpoint_Factory INSTANCE = new HermesEndpoint_Factory();
  }
}
