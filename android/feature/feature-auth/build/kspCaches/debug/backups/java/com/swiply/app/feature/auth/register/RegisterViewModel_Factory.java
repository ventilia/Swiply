package com.swiply.app.feature.auth.register;

import com.swiply.app.core.datastore.SettingsDataStore;
import com.swiply.app.feature.auth.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private RegisterViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(authRepositoryProvider.get(), settingsDataStoreProvider.get());
  }

  public static RegisterViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    return new RegisterViewModel_Factory(authRepositoryProvider, settingsDataStoreProvider);
  }

  public static RegisterViewModel newInstance(AuthRepository authRepository,
      SettingsDataStore settingsDataStore) {
    return new RegisterViewModel(authRepository, settingsDataStore);
  }
}
