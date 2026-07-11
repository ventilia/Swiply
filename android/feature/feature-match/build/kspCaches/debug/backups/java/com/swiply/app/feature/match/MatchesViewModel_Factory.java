package com.swiply.app.feature.match;

import com.swiply.app.core.database.dao.ConversationDao;
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
public final class MatchesViewModel_Factory implements Factory<MatchesViewModel> {
  private final Provider<MatchRepository> matchRepositoryProvider;

  private final Provider<MatchRealtimeSync> matchRealtimeSyncProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private MatchesViewModel_Factory(Provider<MatchRepository> matchRepositoryProvider,
      Provider<MatchRealtimeSync> matchRealtimeSyncProvider,
      Provider<ConversationDao> conversationDaoProvider) {
    this.matchRepositoryProvider = matchRepositoryProvider;
    this.matchRealtimeSyncProvider = matchRealtimeSyncProvider;
    this.conversationDaoProvider = conversationDaoProvider;
  }

  @Override
  public MatchesViewModel get() {
    return newInstance(matchRepositoryProvider.get(), matchRealtimeSyncProvider.get(), conversationDaoProvider.get());
  }

  public static MatchesViewModel_Factory create(Provider<MatchRepository> matchRepositoryProvider,
      Provider<MatchRealtimeSync> matchRealtimeSyncProvider,
      Provider<ConversationDao> conversationDaoProvider) {
    return new MatchesViewModel_Factory(matchRepositoryProvider, matchRealtimeSyncProvider, conversationDaoProvider);
  }

  public static MatchesViewModel newInstance(MatchRepository matchRepository,
      MatchRealtimeSync matchRealtimeSync, ConversationDao conversationDao) {
    return new MatchesViewModel(matchRepository, matchRealtimeSync, conversationDao);
  }
}
