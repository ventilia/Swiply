package com.swiply.app.core.network.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class NetworkModule_RefreshRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<String> baseUrlProvider;

  private final Provider<Json> jsonProvider;

  private NetworkModule_RefreshRetrofitFactory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<String> baseUrlProvider, Provider<Json> jsonProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.baseUrlProvider = baseUrlProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public Retrofit get() {
    return refreshRetrofit(okHttpClientProvider.get(), baseUrlProvider.get(), jsonProvider.get());
  }

  public static NetworkModule_RefreshRetrofitFactory create(
      Provider<OkHttpClient> okHttpClientProvider, Provider<String> baseUrlProvider,
      Provider<Json> jsonProvider) {
    return new NetworkModule_RefreshRetrofitFactory(okHttpClientProvider, baseUrlProvider, jsonProvider);
  }

  public static Retrofit refreshRetrofit(OkHttpClient okHttpClient, String baseUrl, Json json) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.refreshRetrofit(okHttpClient, baseUrl, json));
  }
}
