package com.swiply.app.core.network;

import com.swiply.app.core.datastore.TokenStorage;
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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<TokenStorage> tokenStorageProvider;

  private SessionManager_Factory(Provider<TokenStorage> tokenStorageProvider) {
    this.tokenStorageProvider = tokenStorageProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(tokenStorageProvider.get());
  }

  public static SessionManager_Factory create(Provider<TokenStorage> tokenStorageProvider) {
    return new SessionManager_Factory(tokenStorageProvider);
  }

  public static SessionManager newInstance(TokenStorage tokenStorage) {
    return new SessionManager(tokenStorage);
  }
}
