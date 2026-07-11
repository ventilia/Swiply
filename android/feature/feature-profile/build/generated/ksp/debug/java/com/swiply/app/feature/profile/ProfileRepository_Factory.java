package com.swiply.app.feature.profile;

import com.swiply.app.core.common.media.ImageCompressor;
import com.swiply.app.core.database.dao.MyProfileDao;
import com.swiply.app.core.network.ApiCaller;
import com.swiply.app.core.network.api.AuthApi;
import com.swiply.app.core.network.api.MediaApi;
import com.swiply.app.core.network.api.ProfileApi;
import com.swiply.app.core.network.api.ReportApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.serialization.json.Json;

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
public final class ProfileRepository_Factory implements Factory<ProfileRepository> {
  private final Provider<ProfileApi> profileApiProvider;

  private final Provider<AuthApi> authApiProvider;

  private final Provider<MediaApi> mediaApiProvider;

  private final Provider<ReportApi> reportApiProvider;

  private final Provider<ApiCaller> apiCallerProvider;

  private final Provider<MyProfileDao> myProfileDaoProvider;

  private final Provider<ImageCompressor> imageCompressorProvider;

  private final Provider<Json> jsonProvider;

  private ProfileRepository_Factory(Provider<ProfileApi> profileApiProvider,
      Provider<AuthApi> authApiProvider, Provider<MediaApi> mediaApiProvider,
      Provider<ReportApi> reportApiProvider, Provider<ApiCaller> apiCallerProvider,
      Provider<MyProfileDao> myProfileDaoProvider,
      Provider<ImageCompressor> imageCompressorProvider, Provider<Json> jsonProvider) {
    this.profileApiProvider = profileApiProvider;
    this.authApiProvider = authApiProvider;
    this.mediaApiProvider = mediaApiProvider;
    this.reportApiProvider = reportApiProvider;
    this.apiCallerProvider = apiCallerProvider;
    this.myProfileDaoProvider = myProfileDaoProvider;
    this.imageCompressorProvider = imageCompressorProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public ProfileRepository get() {
    return newInstance(profileApiProvider.get(), authApiProvider.get(), mediaApiProvider.get(), reportApiProvider.get(), apiCallerProvider.get(), myProfileDaoProvider.get(), imageCompressorProvider.get(), jsonProvider.get());
  }

  public static ProfileRepository_Factory create(Provider<ProfileApi> profileApiProvider,
      Provider<AuthApi> authApiProvider, Provider<MediaApi> mediaApiProvider,
      Provider<ReportApi> reportApiProvider, Provider<ApiCaller> apiCallerProvider,
      Provider<MyProfileDao> myProfileDaoProvider,
      Provider<ImageCompressor> imageCompressorProvider, Provider<Json> jsonProvider) {
    return new ProfileRepository_Factory(profileApiProvider, authApiProvider, mediaApiProvider, reportApiProvider, apiCallerProvider, myProfileDaoProvider, imageCompressorProvider, jsonProvider);
  }

  public static ProfileRepository newInstance(ProfileApi profileApi, AuthApi authApi,
      MediaApi mediaApi, ReportApi reportApi, ApiCaller apiCaller, MyProfileDao myProfileDao,
      ImageCompressor imageCompressor, Json json) {
    return new ProfileRepository(profileApi, authApi, mediaApi, reportApi, apiCaller, myProfileDao, imageCompressor, json);
  }
}
