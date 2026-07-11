package com.swiply.app.feature.profile.publicprofile;

import androidx.lifecycle.SavedStateHandle;
import com.swiply.app.feature.profile.ProfileRepository;
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
public final class PublicProfileViewModel_Factory implements Factory<PublicProfileViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ProfileRepository> profileRepositoryProvider;

  private PublicProfileViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ProfileRepository> profileRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.profileRepositoryProvider = profileRepositoryProvider;
  }

  @Override
  public PublicProfileViewModel get() {
    return newInstance(savedStateHandleProvider.get(), profileRepositoryProvider.get());
  }

  public static PublicProfileViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ProfileRepository> profileRepositoryProvider) {
    return new PublicProfileViewModel_Factory(savedStateHandleProvider, profileRepositoryProvider);
  }

  public static PublicProfileViewModel newInstance(SavedStateHandle savedStateHandle,
      ProfileRepository profileRepository) {
    return new PublicProfileViewModel(savedStateHandle, profileRepository);
  }
}
