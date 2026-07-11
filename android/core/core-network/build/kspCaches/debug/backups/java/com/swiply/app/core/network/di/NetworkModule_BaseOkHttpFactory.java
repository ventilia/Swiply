package com.swiply.app.core.network.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_BaseOkHttpFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return baseOkHttp();
  }

  public static NetworkModule_BaseOkHttpFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient baseOkHttp() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.baseOkHttp());
  }

  private static final class InstanceHolder {
    static final NetworkModule_BaseOkHttpFactory INSTANCE = new NetworkModule_BaseOkHttpFactory();
  }
}
