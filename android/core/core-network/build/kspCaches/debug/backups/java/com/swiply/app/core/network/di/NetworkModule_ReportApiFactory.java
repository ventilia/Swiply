package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.ReportApi;
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
public final class NetworkModule_ReportApiFactory implements Factory<ReportApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_ReportApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ReportApi get() {
    return reportApi(retrofitProvider.get());
  }

  public static NetworkModule_ReportApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ReportApiFactory(retrofitProvider);
  }

  public static ReportApi reportApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.reportApi(retrofit));
  }
}
