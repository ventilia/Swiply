package com.swiply.app.feature.chat;

import com.swiply.app.core.database.dao.ConversationDao;
import com.swiply.app.core.database.dao.MessageDao;
import com.swiply.app.core.network.SessionManager;
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
public final class ChatRealtimeSync_Factory implements Factory<ChatRealtimeSync> {
  private final Provider<RealtimeClient> realtimeClientProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<CoroutineScope> scopeProvider;

  private ChatRealtimeSync_Factory(Provider<RealtimeClient> realtimeClientProvider,
      Provider<ChatRepository> chatRepositoryProvider, Provider<MessageDao> messageDaoProvider,
      Provider<ConversationDao> conversationDaoProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<CoroutineScope> scopeProvider) {
    this.realtimeClientProvider = realtimeClientProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.scopeProvider = scopeProvider;
  }

  @Override
  public ChatRealtimeSync get() {
    return newInstance(realtimeClientProvider.get(), chatRepositoryProvider.get(), messageDaoProvider.get(), conversationDaoProvider.get(), sessionManagerProvider.get(), scopeProvider.get());
  }

  public static ChatRealtimeSync_Factory create(Provider<RealtimeClient> realtimeClientProvider,
      Provider<ChatRepository> chatRepositoryProvider, Provider<MessageDao> messageDaoProvider,
      Provider<ConversationDao> conversationDaoProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<CoroutineScope> scopeProvider) {
    return new ChatRealtimeSync_Factory(realtimeClientProvider, chatRepositoryProvider, messageDaoProvider, conversationDaoProvider, sessionManagerProvider, scopeProvider);
  }

  public static ChatRealtimeSync newInstance(RealtimeClient realtimeClient,
      ChatRepository chatRepository, MessageDao messageDao, ConversationDao conversationDao,
      SessionManager sessionManager, CoroutineScope scope) {
    return new ChatRealtimeSync(realtimeClient, chatRepository, messageDao, conversationDao, sessionManager, scope);
  }
}
