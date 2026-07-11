package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.MediaApi;
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
public final class NetworkModule_MediaApiFactory implements Factory<MediaApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_MediaApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MediaApi get() {
    return mediaApi(retrofitProvider.get());
  }

  public static NetworkModule_MediaApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_MediaApiFactory(retrofitProvider);
  }

  public static MediaApi mediaApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.mediaApi(retrofit));
  }
}
