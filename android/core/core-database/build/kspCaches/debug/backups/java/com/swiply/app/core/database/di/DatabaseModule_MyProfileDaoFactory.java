package com.swiply.app.core.database.di;

import com.swiply.app.core.database.SwiplyDatabase;
import com.swiply.app.core.database.dao.MyProfileDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_MyProfileDaoFactory implements Factory<MyProfileDao> {
  private final Provider<SwiplyDatabase> dbProvider;

  private DatabaseModule_MyProfileDaoFactory(Provider<SwiplyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MyProfileDao get() {
    return myProfileDao(dbProvider.get());
  }

  public static DatabaseModule_MyProfileDaoFactory create(Provider<SwiplyDatabase> dbProvider) {
    return new DatabaseModule_MyProfileDaoFactory(dbProvider);
  }

  public static MyProfileDao myProfileDao(SwiplyDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.myProfileDao(db));
  }
}
