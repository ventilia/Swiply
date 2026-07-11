package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.DiscoveryApi;
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
public final class NetworkModule_DiscoveryApiFactory implements Factory<DiscoveryApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_DiscoveryApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public DiscoveryApi get() {
    return discoveryApi(retrofitProvider.get());
  }

  public static NetworkModule_DiscoveryApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_DiscoveryApiFactory(retrofitProvider);
  }

  public static DiscoveryApi discoveryApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.discoveryApi(retrofit));
  }
}
