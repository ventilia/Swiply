package com.swiply.app.core.network.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_WsUrlFactory implements Factory<String> {
  private final Provider<String> baseUrlProvider;

  private NetworkModule_WsUrlFactory(Provider<String> baseUrlProvider) {
    this.baseUrlProvider = baseUrlProvider;
  }

  @Override
  public String get() {
    return wsUrl(baseUrlProvider.get());
  }

  public static NetworkModule_WsUrlFactory create(Provider<String> baseUrlProvider) {
    return new NetworkModule_WsUrlFactory(baseUrlProvider);
  }

  public static String wsUrl(String baseUrl) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.wsUrl(baseUrl));
  }
}
