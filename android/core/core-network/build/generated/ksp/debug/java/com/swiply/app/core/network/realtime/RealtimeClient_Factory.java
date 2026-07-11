package com.swiply.app.core.network.realtime;

import com.swiply.app.core.datastore.TokenStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "javax.inject.Named",
    "com.swiply.app.core.common.ApplicationScope"
})
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
public final class RealtimeClient_Factory implements Factory<RealtimeClient> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<String> wsUrlProvider;

  private final Provider<TokenStorage> tokenStorageProvider;

  private final Provider<Json> jsonProvider;

  private final Provider<CoroutineScope> scopeProvider;

  private RealtimeClient_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<String> wsUrlProvider, Provider<TokenStorage> tokenStorageProvider,
      Provider<Json> jsonProvider, Provider<CoroutineScope> scopeProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.wsUrlProvider = wsUrlProvider;
    this.tokenStorageProvider = tokenStorageProvider;
    this.jsonProvider = jsonProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public RealtimeClient get() {
    return newInstance(okHttpClientProvider.get(), wsUrlProvider.get(), tokenStorageProvider.get(), jsonProvider.get(), scopeProvider.get());
  }

  public static RealtimeClient_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<String> wsUrlProvider, Provider<TokenStorage> tokenStorageProvider,
      Provider<Json> jsonProvider, Provider<CoroutineScope> scopeProvider) {
    return new RealtimeClient_Factory(okHttpClientProvider, wsUrlProvider, tokenStorageProvider, jsonProvider, scopeProvider);
  }

  public static RealtimeClient newInstance(OkHttpClient okHttpClient, String wsUrl,
      TokenStorage tokenStorage, Json json, CoroutineScope scope) {
    return new RealtimeClient(okHttpClient, wsUrl, tokenStorage, json, scope);
  }
}
