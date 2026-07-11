package com.swiply.app.feature.auth;

import com.swiply.app.core.datastore.TokenStorage;
import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.SessionManager;
import com.swiply.app.core.network.api.AuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<TokenStorage> tokenStorageProvider;

  private AuthRepository_Factory(Provider<AuthApi> authApiProvider,
      Provider<ApiCaller> apiCallerProvider, Provider<SessionManager> sessionManagerProvider,
      Provider<TokenStorage> tokenStorageProvider) {
    this.authApiProvider = authApiProvider;
    this.apiCallerProvider = apiCallerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.tokenStorageProvider = tokenStorageProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(authApiProvider.get(), apiCallerProvider.get(), sessionManagerProvider.get(), tokenStorageProvider.get());
  }

  public static AuthRepository_Factory create(Provider<AuthApi> authApiProvider,
      Provider<ApiCaller> apiCallerProvider, Provider<SessionManager> sessionManagerProvider,
      Provider<TokenStorage> tokenStorageProvider) {
    return new AuthRepository_Factory(authApiProvider, apiCallerProvider, sessionManagerProvider, tokenStorageProvider);
  }

  public static AuthRepository newInstance(AuthApi authApi, ApiCaller apiCaller,
      SessionManager sessionManager, TokenStorage tokenStorage) {
    return new AuthRepository(authApi, apiCaller, sessionManager, tokenStorage);
  }
}
