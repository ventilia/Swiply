package com.swiply.app.core.common.media;

import android.content.Context;
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
public final class ImageCompressor_Factory implements Factory<ImageCompressor> {
  private final Provider<Context> contextProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  private ImageCompressor_Factory(Provider<Context> contextProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.contextProvider = contextProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public ImageCompressor get() {
    return newInstance(contextProvider.get(), ioDispatcherProvider.get());
  }

  public static ImageCompressor_Factory create(Provider<Context> contextProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new ImageCompressor_Factory(contextProvider, ioDispatcherProvider);
  }

  public static ImageCompressor newInstance(Context context, CoroutineDispatcher ioDispatcher) {
    return new ImageCompressor(context, ioDispatcher);
  }
}
