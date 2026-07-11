package com.swiply.app.feature.match;

import com.swiply.app.core.database.dao.ConversationDao;
import com.swiply.app.core.database.dao.MatchDao;
import com.swiply.app.core.network.realtime.RealtimeClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.swiply.app.core.common.ApplicationScope")
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
public final class MatchRealtimeSync_Factory implements Factory<MatchRealtimeSync> {
  private final Provider<RealtimeClient> realtimeClientProvider;

  private final Provider<MatchRepository> matchRepositoryProvider;

  private final Provider<MatchDao> matchDaoProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<CoroutineScope> scopeProvider;

  private MatchRealtimeSync_Factory(Provider<RealtimeClient> realtimeClientProvider,
      Provider<MatchRepository> matchRepositoryProvider, Provider<MatchDao> matchDaoProvider,
      Provider<ConversationDao> conversationDaoProvider, Provider<CoroutineScope> scopeProvider) {
    this.realtimeClientProvider = realtimeClientProvider;
    this.matchRepositoryProvider = matchRepositoryProvider;
    this.matchDaoProvider = matchDaoProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public MatchRealtimeSync get() {
    return newInstance(realtimeClientProvider.get(), matchRepositoryProvider.get(), matchDaoProvider.get(), conversationDaoProvider.get(), scopeProvider.get());
  }

  public static MatchRealtimeSync_Factory create(Provider<RealtimeClient> realtimeClientProvider,
      Provider<MatchRepository> matchRepositoryProvider, Provider<MatchDao> matchDaoProvider,
      Provider<ConversationDao> conversationDaoProvider, Provider<CoroutineScope> scopeProvider) {
    return new MatchRealtimeSync_Factory(realtimeClientProvider, matchRepositoryProvider, matchDaoProvider, conversationDaoProvider, scopeProvider);
  }

  public static MatchRealtimeSync newInstance(RealtimeClient realtimeClient,
      MatchRepository matchRepository, MatchDao matchDao, ConversationDao conversationDao,
      CoroutineScope scope) {
    return new MatchRealtimeSync(realtimeClient, matchRepository, matchDao, conversationDao, scope);
  }
}
