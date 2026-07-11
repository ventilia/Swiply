package com.swiply.app.feature.match.likes;

import com.swiply.app.feature.match.MatchRepository;
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
public final class LikesViewModel_Factory implements Factory<LikesViewModel> {
  private final Provider<MatchRepository> matchRepositoryProvider;

  private LikesViewModel_Factory(Provider<MatchRepository> matchRepositoryProvider) {
    this.matchRepositoryProvider = matchRepositoryProvider;
  }

  @Override
  public LikesViewModel get() {
    return newInstance(matchRepositoryProvider.get());
  }

  public static LikesViewModel_Factory create(Provider<MatchRepository> matchRepositoryProvider) {
    return new LikesViewModel_Factory(matchRepositoryProvider);
  }

  public static LikesViewModel newInstance(MatchRepository matchRepository) {
    return new LikesViewModel(matchRepository);
  }
}
