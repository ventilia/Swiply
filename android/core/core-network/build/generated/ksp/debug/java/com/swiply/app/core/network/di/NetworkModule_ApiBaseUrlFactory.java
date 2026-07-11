package com.swiply.app.core.network.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ApiBaseUrlFactory implements Factory<String> {
  @Override
  public String get() {
    return apiBaseUrl();
  }

  public static NetworkModule_ApiBaseUrlFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static String apiBaseUrl() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.apiBaseUrl());
  }

  private static final class InstanceHolder {
    static final NetworkModule_ApiBaseUrlFactory INSTANCE = new NetworkModule_ApiBaseUrlFactory();
  }
}
