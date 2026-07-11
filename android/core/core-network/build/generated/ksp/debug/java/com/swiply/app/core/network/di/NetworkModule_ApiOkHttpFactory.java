package com.swiply.app.core.network.di;

import com.swiply.app.core.network.auth.AuthInterceptor;
import com.swiply.app.core.network.auth.TokenAuthenticator;
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
public final class NetworkModule_ApiOkHttpFactory implements Factory<OkHttpClient> {
  private final Provider<OkHttpClient> baseProvider;

  private final Provider<AuthInterceptor> authInterceptorProvider;

  private final Provider<TokenAuthenticator> tokenAuthenticatorProvider;

  private NetworkModule_ApiOkHttpFactory(Provider<OkHttpClient> baseProvider,
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    this.baseProvider = baseProvider;
    this.authInterceptorProvider = authInterceptorProvider;
    this.tokenAuthenticatorProvider = tokenAuthenticatorProvider;
  }

  @Override
  public OkHttpClient get() {
    return apiOkHttp(baseProvider.get(), authInterceptorProvider.get(), tokenAuthenticatorProvider.get());
  }

  public static NetworkModule_ApiOkHttpFactory create(Provider<OkHttpClient> baseProvider,
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<TokenAuthenticator> tokenAuthenticatorProvider) {
    return new NetworkModule_ApiOkHttpFactory(baseProvider, authInterceptorProvider, tokenAuthenticatorProvider);
  }

  public static OkHttpClient apiOkHttp(OkHttpClient base, AuthInterceptor authInterceptor,
      TokenAuthenticator tokenAuthenticator) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.apiOkHttp(base, authInterceptor, tokenAuthenticator));
  }
}
