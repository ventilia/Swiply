package com.swiply.app.feature.settings;

import com.swiply.app.core.database.SwiplyDatabase;
import com.swiply.app.core.datastore.TokenStorage;
import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.SessionManager;
import com.swiply.app.core.network.api.AuthApi;
import com.swiply.app.core.network.api.ProfileApi;
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
public final class SettingsRepository_Factory implements Factory<SettingsRepository> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<ProfileApi> profileApiProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private final Provider<TokenStorage> tokenStorageProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<SwiplyDatabase> databaseProvider;

  private SettingsRepository_Factory(Provider<AuthApi> authApiProvider,
      Provider<ProfileApi> profileApiProvider, Provider<ApiCaller> apiCallerProvider,
      Provider<TokenStorage> tokenStorageProvider, Provider<SessionManager> sessionManagerProvider,
      Provider<SwiplyDatabase> databaseProvider) {
    this.authApiProvider = authApiProvider;
    this.profileApiProvider = profileApiProvider;
    this.apiCallerProvider = apiCallerProvider;
    this.tokenStorageProvider = tokenStorageProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SettingsRepository get() {
    return newInstance(authApiProvider.get(), profileApiProvider.get(), apiCallerProvider.get(), tokenStorageProvider.get(), sessionManagerProvider.get(), databaseProvider.get());
  }

  public static SettingsRepository_Factory create(Provider<AuthApi> authApiProvider,
      Provider<ProfileApi> profileApiProvider, Provider<ApiCaller> apiCallerProvider,
      Provider<TokenStorage> tokenStorageProvider, Provider<SessionManager> sessionManagerProvider,
      Provider<SwiplyDatabase> databaseProvider) {
    return new SettingsRepository_Factory(authApiProvider, profileApiProvider, apiCallerProvider, tokenStorageProvider, sessionManagerProvider, databaseProvider);
  }

  public static SettingsRepository newInstance(AuthApi authApi, ProfileApi profileApi,
      ApiCaller apiCaller, TokenStorage tokenStorage, SessionManager sessionManager,
      SwiplyDatabase database) {
    return new SettingsRepository(authApi, profileApi, apiCaller, tokenStorage, sessionManager, database);
  }
}
