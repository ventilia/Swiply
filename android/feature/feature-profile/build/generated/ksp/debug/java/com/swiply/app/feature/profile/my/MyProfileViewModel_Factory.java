package com.swiply.app.feature.profile.my;

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
public final class MyProfileViewModel_Factory implements Factory<MyProfileViewModel> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  private MyProfileViewModel_Factory(Provider<ProfileRepository> profileRepositoryProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
  }

  @Override
  public MyProfileViewModel get() {
    return newInstance(profileRepositoryProvider.get());
  }

  public static MyProfileViewModel_Factory create(
      Provider<ProfileRepository> profileRepositoryProvider) {
    return new MyProfileViewModel_Factory(profileRepositoryProvider);
  }

  public static MyProfileViewModel newInstance(ProfileRepository profileRepository) {
    return new MyProfileViewModel(profileRepository);
  }
}
