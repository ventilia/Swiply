package com.swiply.app.feature.discovery;

import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.api.DiscoveryApi;
import com.swiply.app.core.network.api.MatchingApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class DiscoveryRepository_Factory implements Factory<DiscoveryRepository> {
  private final Provider<DiscoveryApi> discoveryApiProvider;

  private final Provider<MatchingApi> matchingApiProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private DiscoveryRepository_Factory(Provider<DiscoveryApi> discoveryApiProvider,
      Provider<MatchingApi> matchingApiProvider, Provider<ApiCaller> apiCallerProvider) {
    this.discoveryApiProvider = discoveryApiProvider;
    this.matchingApiProvider = matchingApiProvider;
    this.apiCallerProvider = apiCallerProvider;
  }

  @Override
  public DiscoveryRepository get() {
    return newInstance(discoveryApiProvider.get(), matchingApiProvider.get(), apiCallerProvider.get());
  }

  public static DiscoveryRepository_Factory create(Provider<DiscoveryApi> discoveryApiProvider,
      Provider<MatchingApi> matchingApiProvider, Provider<ApiCaller> apiCallerProvider) {
    return new DiscoveryRepository_Factory(discoveryApiProvider, matchingApiProvider, apiCallerProvider);
  }

  public static DiscoveryRepository newInstance(DiscoveryApi discoveryApi, MatchingApi matchingApi,
      ApiCaller apiCaller) {
    return new DiscoveryRepository(discoveryApi, matchingApi, apiCaller);
  }
}
