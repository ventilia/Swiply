package com.swiply.app.feature.profile.verify;

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
public final class EmailVerificationViewModel_Factory implements Factory<EmailVerificationViewModel> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  private EmailVerificationViewModel_Factory(
      Provider<ProfileRepository> profileRepositoryProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
  }

  @Override
  public EmailVerificationViewModel get() {
    return newInstance(profileRepositoryProvider.get());
  }

  public static EmailVerificationViewModel_Factory create(
      Provider<ProfileRepository> profileRepositoryProvider) {
    return new EmailVerificationViewModel_Factory(profileRepositoryProvider);
  }

  public static EmailVerificationViewModel newInstance(ProfileRepository profileRepository) {
    return new EmailVerificationViewModel(profileRepository);
  }
}
