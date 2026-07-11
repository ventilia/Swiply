package com.swiply.app.feature.profile.edit;

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
public final class EditProfileViewModel_Factory implements Factory<EditProfileViewModel> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  private EditProfileViewModel_Factory(Provider<ProfileRepository> profileRepositoryProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
  }

  @Override
  public EditProfileViewModel get() {
    return newInstance(profileRepositoryProvider.get());
  }

  public static EditProfileViewModel_Factory create(
      Provider<ProfileRepository> profileRepositoryProvider) {
    return new EditProfileViewModel_Factory(profileRepositoryProvider);
  }

  public static EditProfileViewModel newInstance(ProfileRepository profileRepository) {
    return new EditProfileViewModel(profileRepository);
  }
}
