package com.swiply.app.core.network.di;

import com.swiply.app.core.network.api.ChatApi;
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
public final class NetworkModule_ChatApiFactory implements Factory<ChatApi> {
  private final Provider<Retrofit> retrofitProvider;

  private NetworkModule_ChatApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ChatApi get() {
    return chatApi(retrofitProvider.get());
  }

  public static NetworkModule_ChatApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ChatApiFactory(retrofitProvider);
  }

  public static ChatApi chatApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.chatApi(retrofit));
  }
}
