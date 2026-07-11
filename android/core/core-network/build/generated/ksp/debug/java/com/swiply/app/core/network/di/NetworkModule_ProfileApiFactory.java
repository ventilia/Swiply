package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.ProfileApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProfileApiFactory implements Factory<ProfileApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_ProfileApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ProfileApi get() {
    return profileApi(retrofitProvider.get());
  }

  public static NetworkModule_ProfileApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProfileApiFactory(retrofitProvider);
  }

  public static ProfileApi profileApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.profileApi(retrofit));
  }
}
