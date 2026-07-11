package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.TokenRefreshApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
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
public final class NetworkModule_TokenRefreshApiFactory implements Factory<TokenRefreshApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_TokenRefreshApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TokenRefreshApi get() {
    return tokenRefreshApi(retrofitProvider.get());
  }

  public static NetworkModule_TokenRefreshApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_TokenRefreshApiFactory(retrofitProvider);
  }

  public static TokenRefreshApi tokenRefreshApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.tokenRefreshApi(retrofit));
  }
}
