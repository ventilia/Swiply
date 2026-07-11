package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class NetworkModule_AuthApiFactory implements Factory<AuthApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_AuthApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public AuthApi get() {
    return authApi(retrofitProvider.get());
  }

  public static NetworkModule_AuthApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_AuthApiFactory(retrofitProvider);
  }

  public static AuthApi authApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.authApi(retrofit));
  }
}
