package com.swiply.app.core.database.di;

import com.swiply.app.core.database.SwiplyDatabase;
import com.swiply.app.core.database.dao.MessageDao;
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
public final class DatabaseModule_MessageDaoFactory implements Factory<MessageDao> {
  private final Provider<SwiplyDatabase> dbProvider;

  private DatabaseModule_MessageDaoFactory(Provider<SwiplyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MessageDao get() {
    return messageDao(dbProvider.get());
  }

  public static DatabaseModule_MessageDaoFactory create(Provider<SwiplyDatabase> dbProvider) {
    return new DatabaseModule_MessageDaoFactory(dbProvider);
  }

  public static MessageDao messageDao(SwiplyDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.messageDao(db));
  }
}
