package com.swiply.app.feature.chat;

import androidx.lifecycle.SavedStateHandle;
import com.swiply.app.core.network.SessionManager;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ChatRepository> chatRepositoryProvider;

  private final Provider<ChatRealtimeSync> chatRealtimeSyncProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private ChatViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ChatRealtimeSync> chatRealtimeSyncProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.chatRepositoryProvider = chatRepositoryProvider;
    this.chatRealtimeSyncProvider = chatRealtimeSyncProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(savedStateHandleProvider.get(), chatRepositoryProvider.get(), chatRealtimeSyncProvider.get(), sessionManagerProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ChatRepository> chatRepositoryProvider,
      Provider<ChatRealtimeSync> chatRealtimeSyncProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new ChatViewModel_Factory(savedStateHandleProvider, chatRepositoryProvider, chatRealtimeSyncProvider, sessionManagerProvider);
  }

  public static ChatViewModel newInstance(SavedStateHandle savedStateHandle,
      ChatRepository chatRepository, ChatRealtimeSync chatRealtimeSync,
      SessionManager sessionManager) {
    return new ChatViewModel(savedStateHandle, chatRepository, chatRealtimeSync, sessionManager);
  }
}
