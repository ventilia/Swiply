package com.swiply.app.feature.chat;

import com.swiply.app.core.common.media.ImageCompressor;
import com.swiply.app.core.database.dao.ConversationDao;
import com.swiply.app.core.database.dao.MessageDao;
import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.SessionManager;
import com.swiply.app.core.network.api.ChatApi;
import com.swiply.app.core.network.realtime.RealtimeClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ChatRepository_Factory implements Factory<ChatRepository> {
  private final Provider<ChatApi> chatApiProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<RealtimeClient> realtimeClientProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<ImageCompressor> imageCompressorProvider;

  private ChatRepository_Factory(Provider<ChatApi> chatApiProvider,
      Provider<ApiCaller> apiCallerProvider, Provider<ConversationDao> conversationDaoProvider,
      Provider<MessageDao> messageDaoProvider, Provider<RealtimeClient> realtimeClientProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    this.chatApiProvider = chatApiProvider;
    this.apiCallerProvider = apiCallerProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.realtimeClientProvider = realtimeClientProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.imageCompressorProvider = imageCompressorProvider;
  }

  @Override
  public ChatRepository get() {
    return newInstance(chatApiProvider.get(), apiCallerProvider.get(), conversationDaoProvider.get(), messageDaoProvider.get(), realtimeClientProvider.get(), sessionManagerProvider.get(), imageCompressorProvider.get());
  }

  public static ChatRepository_Factory create(Provider<ChatApi> chatApiProvider,
      Provider<ApiCaller> apiCallerProvider, Provider<ConversationDao> conversationDaoProvider,
      Provider<MessageDao> messageDaoProvider, Provider<RealtimeClient> realtimeClientProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<ImageCompressor> imageCompressorProvider) {
    return new ChatRepository_Factory(chatApiProvider, apiCallerProvider, conversationDaoProvider, messageDaoProvider, realtimeClientProvider, sessionManagerProvider, imageCompressorProvider);
  }

  public static ChatRepository newInstance(ChatApi chatApi, ApiCaller apiCaller,
      ConversationDao conversationDao, MessageDao messageDao, RealtimeClient realtimeClient,
      SessionManager sessionManager, ImageCompressor imageCompressor) {
    return new ChatRepository(chatApi, apiCaller, conversationDao, messageDao, realtimeClient, sessionManager, imageCompressor);
  }
}
