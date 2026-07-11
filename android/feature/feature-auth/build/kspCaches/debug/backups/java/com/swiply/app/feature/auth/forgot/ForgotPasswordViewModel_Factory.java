package com.swiply.app.feature.auth.forgot;

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
public final class ForgotPasswordViewModel_Factory implements Factory<ForgotPasswordViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private ForgotPasswordViewModel_Factory(Provider<AuthRepository> authRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public ForgotPasswordViewModel get() {
    return newInstance(authRepositoryProvider.get());
  }

  public static ForgotPasswordViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider) {
    return new ForgotPasswordViewModel_Factory(authRepositoryProvider);
  }

  public static ForgotPasswordViewModel newInstance(AuthRepository authRepository) {
    return new ForgotPasswordViewModel(authRepository);
  }
}
