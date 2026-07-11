package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.NotificationApi;
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
public final class NetworkModule_NotificationApiFactory implements Factory<NotificationApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_NotificationApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public NotificationApi get() {
    return notificationApi(retrofitProvider.get());
  }

  public static NetworkModule_NotificationApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_NotificationApiFactory(retrofitProvider);
  }

  public static NotificationApi notificationApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.notificationApi(retrofit));
  }
}
