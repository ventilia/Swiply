package com.swiply.app.core.network.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class NetworkModule_WsOkHttpFactory implements Factory<OkHttpClient> {
  private final Provider<OkHttpClient> baseProvider;

  private NetworkModule_WsOkHttpFactory(Provider<OkHttpClient> baseProvider) {
    this.baseProvider = baseProvider;
  }

  @Override
  public OkHttpClient get() {
    return wsOkHttp(baseProvider.get());
  }

  public static NetworkModule_WsOkHttpFactory create(Provider<OkHttpClient> baseProvider) {
    return new NetworkModule_WsOkHttpFactory(baseProvider);
  }

  public static OkHttpClient wsOkHttp(OkHttpClient base) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.wsOkHttp(base));
  }
}
