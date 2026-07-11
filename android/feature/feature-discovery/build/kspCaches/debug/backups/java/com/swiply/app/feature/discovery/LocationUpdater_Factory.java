package com.swiply.app.feature.discovery;

import android.content.Context;
import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.api.ProfileApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "com.swiply.app.core.common.IoDispatcher"
})
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
public final class LocationUpdater_Factory implements Factory<LocationUpdater> {
  private final Provider<Context> contextProvider;

  private final Provider<ProfileApi> profileApiProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private LocationUpdater_Factory(Provider<Context> contextProvider,
      Provider<ProfileApi> profileApiProvider, Provider<ApiCaller> apiCallerProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.contextProvider = contextProvider;
    this.profileApiProvider = profileApiProvider;
    this.apiCallerProvider = apiCallerProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public LocationUpdater get() {
    return newInstance(contextProvider.get(), profileApiProvider.get(), apiCallerProvider.get(), ioDispatcherProvider.get());
  }

  public static LocationUpdater_Factory create(Provider<Context> contextProvider,
      Provider<ProfileApi> profileApiProvider, Provider<ApiCaller> apiCallerProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new LocationUpdater_Factory(contextProvider, profileApiProvider, apiCallerProvider, ioDispatcherProvider);
  }

  public static LocationUpdater newInstance(Context context, ProfileApi profileApi,
      ApiCaller apiCaller, CoroutineDispatcher ioDispatcher) {
    return new LocationUpdater(context, profileApi, apiCaller, ioDispatcher);
  }
}
