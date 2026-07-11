package com.swiply.app.core.database.di;

import com.swiply.app.core.database.SwiplyDatabase;
import com.swiply.app.core.database.dao.ConversationDao;
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
public final class DatabaseModule_ConversationDaoFactory implements Factory<ConversationDao> {
  private final Provider<SwiplyDatabase> dbProvider;

  private DatabaseModule_ConversationDaoFactory(Provider<SwiplyDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ConversationDao get() {
    return conversationDao(dbProvider.get());
  }

  public static DatabaseModule_ConversationDaoFactory create(Provider<SwiplyDatabase> dbProvider) {
    return new DatabaseModule_ConversationDaoFactory(dbProvider);
  }

  public static ConversationDao conversationDao(SwiplyDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.conversationDao(db));
  }
}
