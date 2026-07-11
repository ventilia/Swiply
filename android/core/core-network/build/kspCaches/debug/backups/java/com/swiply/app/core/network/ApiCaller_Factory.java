package com.swiply.app.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;

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
public final class ApiCaller_Factory implements Factory<ApiCaller> {
  private final Provider<Json> jsonProvider;

  private ApiCaller_Factory(Provider<Json> jsonProvider) {
    this.jsonProvider = jsonProvider;
  }

  @Override
  public ApiCaller get() {
    return newInstance(jsonProvider.get());
  }

  public static ApiCaller_Factory create(Provider<Json> jsonProvider) {
    return new ApiCaller_Factory(jsonProvider);
  }

  public static ApiCaller newInstance(Json json) {
    return new ApiCaller(json);
  }
}
