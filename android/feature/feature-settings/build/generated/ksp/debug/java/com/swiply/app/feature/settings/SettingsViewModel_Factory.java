package com.swiply.app.feature.settings;

import com.swiply.app.core.datastore.SettingsDataStore;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private SettingsViewModel_Factory(Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsDataStoreProvider = settingsDataStoreProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsDataStoreProvider.get(), settingsRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SettingsViewModel_Factory(settingsDataStoreProvider, settingsRepositoryProvider);
  }

  public static SettingsViewModel newInstance(SettingsDataStore settingsDataStore,
      SettingsRepository settingsRepository) {
    return new SettingsViewModel(settingsDataStore, settingsRepository);
  }
}
