package com.swiply.app.core.database.di;

import android.content.Context;
import com.swiply.app.core.database.SwiplyDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_DatabaseFactory implements Factory<SwiplyDatabase> {
  private final Provider<Context> contextProvider;

  private DatabaseModule_DatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SwiplyDatabase get() {
    return database(contextProvider.get());
  }

  public static DatabaseModule_DatabaseFactory create(Provider<Context> contextProvider) {
    return new DatabaseModule_DatabaseFactory(contextProvider);
  }

  public static SwiplyDatabase database(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.database(context));
  }
}
