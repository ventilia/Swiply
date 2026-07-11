package com.swiply.app.feature.discovery;

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
public final class DiscoveryViewModel_Factory implements Factory<DiscoveryViewModel> {
  private final Provider<DiscoveryRepository> repositoryProvider;

  private final Provider<LocationUpdater> locationUpdaterProvider;

  private DiscoveryViewModel_Factory(Provider<DiscoveryRepository> repositoryProvider,
      Provider<LocationUpdater> locationUpdaterProvider) {
    this.repositoryProvider = repositoryProvider;
    this.locationUpdaterProvider = locationUpdaterProvider;
  }

  @Override
  public DiscoveryViewModel get() {
    return newInstance(repositoryProvider.get(), locationUpdaterProvider.get());
  }

  public static DiscoveryViewModel_Factory create(Provider<DiscoveryRepository> repositoryProvider,
      Provider<LocationUpdater> locationUpdaterProvider) {
    return new DiscoveryViewModel_Factory(repositoryProvider, locationUpdaterProvider);
  }

  public static DiscoveryViewModel newInstance(DiscoveryRepository repository,
      LocationUpdater locationUpdater) {
    return new DiscoveryViewModel(repository, locationUpdater);
  }
}
