package com.swiply.app.feature.match;

import com.swiply.app.core.database.dao.MatchDao;
import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.api.MatchingApi;
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
public final class MatchRepository_Factory implements Factory<MatchRepository> {
  private final Provider<MatchingApi> matchingApiProvider;

  private final Provider<MatchDao> matchDaoProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private MatchRepository_Factory(Provider<MatchingApi> matchingApiProvider,
      Provider<MatchDao> matchDaoProvider, Provider<ApiCaller> apiCallerProvider) {
    this.matchingApiProvider = matchingApiProvider;
    this.matchDaoProvider = matchDaoProvider;
    this.apiCallerProvider = apiCallerProvider;
  }

  @Override
  public MatchRepository get() {
    return newInstance(matchingApiProvider.get(), matchDaoProvider.get(), apiCallerProvider.get());
  }

  public static MatchRepository_Factory create(Provider<MatchingApi> matchingApiProvider,
      Provider<MatchDao> matchDaoProvider, Provider<ApiCaller> apiCallerProvider) {
    return new MatchRepository_Factory(matchingApiProvider, matchDaoProvider, apiCallerProvider);
  }

  public static MatchRepository newInstance(MatchingApi matchingApi, MatchDao matchDao,
      ApiCaller apiCaller) {
    return new MatchRepository(matchingApi, matchDao, apiCaller);
  }
}
