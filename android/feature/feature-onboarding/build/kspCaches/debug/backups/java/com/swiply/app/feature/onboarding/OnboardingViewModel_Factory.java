package com.swiply.app.feature.onboarding;

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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private OnboardingViewModel_Factory(Provider<SettingsDataStore> settingsDataStoreProvider) {
    this.settingsDataStoreProvider = settingsDataStoreProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(settingsDataStoreProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    return new OnboardingViewModel_Factory(settingsDataStoreProvider);
  }

  public static OnboardingViewModel newInstance(SettingsDataStore settingsDataStore) {
    return new OnboardingViewModel(settingsDataStore);
  }
}
