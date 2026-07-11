package com.swiply.app.core.network.auth;

import com.swiply.app.core.datastore.TokenStorage;
import com.swiply.app.core.network.SessionManager;
import com.swiply.app.core.network.api.TokenRefreshApi;
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
public final class TokenAuthenticator_Factory implements Factory<TokenAuthenticator> {
  private final Provider<TokenStorage> tokenStorageProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<TokenRefreshApi> refreshApiProvider;

  private TokenAuthenticator_Factory(Provider<TokenStorage> tokenStorageProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<TokenRefreshApi> refreshApiProvider) {
    this.tokenStorageProvider = tokenStorageProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.refreshApiProvider = refreshApiProvider;
  }

  @Override
  public TokenAuthenticator get() {
    return newInstance(tokenStorageProvider.get(), sessionManagerProvider.get(), refreshApiProvider);
  }

  public static TokenAuthenticator_Factory create(Provider<TokenStorage> tokenStorageProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<TokenRefreshApi> refreshApiProvider) {
    return new TokenAuthenticator_Factory(tokenStorageProvider, sessionManagerProvider, refreshApiProvider);
  }

  public static TokenAuthenticator newInstance(TokenStorage tokenStorage,
      SessionManager sessionManager, javax.inject.Provider<TokenRefreshApi> refreshApi) {
    return new TokenAuthenticator(tokenStorage, sessionManager, refreshApi);
  }
}
