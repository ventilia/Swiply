package com.swiply.app.core.network.api

import com.swiply.app.core.network.dto.AttachmentDto
import com.swiply.app.core.network.dto.BlockedUserItemDto
import com.swiply.app.core.network.dto.CandidateDto
import com.swiply.app.core.network.dto.ConversationDto
import com.swiply.app.core.network.dto.DeleteAccountRequestDto
import com.swiply.app.core.network.dto.ForgotPasswordRequestDto
import com.swiply.app.core.network.dto.LikeReceivedDto
import com.swiply.app.core.network.dto.LoginRequestDto
import com.swiply.app.core.network.dto.LogoutRequestDto
import com.swiply.app.core.network.dto.MatchItemDto
import com.swiply.app.core.network.dto.MessageDto
import com.swiply.app.core.network.dto.MyProfileDto
import com.swiply.app.core.network.dto.NotificationDto
import com.swiply.app.core.network.dto.OkDto
import com.swiply.app.core.network.dto.PageDto
import com.swiply.app.core.network.dto.PhotoDto
import com.swiply.app.core.network.dto.PublicProfileDto
import com.swiply.app.core.network.dto.RefreshRequestDto
import com.swiply.app.core.network.dto.RegisterRequestDto
import com.swiply.app.core.network.dto.ReorderPhotosRequestDto
import com.swiply.app.core.network.dto.ReportRequestDto
import com.swiply.app.core.network.dto.ResetPasswordRequestDto
import com.swiply.app.core.network.dto.SwipeRequestDto
import com.swiply.app.core.network.dto.SwipeResponseDto
import com.swiply.app.core.network.dto.TokenResponseDto
import com.swiply.app.core.network.dto.UndoSwipeResponseDto
import com.swiply.app.core.network.dto.UnreadCountDto
import com.swiply.app.core.network.dto.UpdateLocationRequestDto
import com.swiply.app.core.network.dto.UpdatePreferencesRequestDto
import com.swiply.app.core.network.dto.UpdateProfileRequestDto
import com.swiply.app.core.network.dto.VerifyEmailRequestDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): TokenResponseDto

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): TokenResponseDto

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): OkDto

    @POST("api/v1/auth/email/verify")
    suspend fun verifyEmail(@Body body: VerifyEmailRequestDto): OkDto

    @POST("api/v1/auth/email/resend")
    suspend fun resendVerification(): OkDto

    @POST("api/v1/auth/password/forgot")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto): OkDto

    @POST("api/v1/auth/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto): OkDto
}

/** Отдельный интерфейс: refresh ходит через «голый» OkHttp без Authenticator */
interface TokenRefreshApi {
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): TokenResponseDto
}

interface ProfileApi {
    @GET("api/v1/users/me")
    suspend fun me(): MyProfileDto

    @PUT("api/v1/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequestDto): MyProfileDto

    @PUT("api/v1/users/me/preferences")
    suspend fun updatePreferences(@Body body: UpdatePreferencesRequestDto): MyProfileDto

    @PUT("api/v1/users/me/location")
    suspend fun updateLocation(@Body body: UpdateLocationRequestDto): OkDto

    @GET("api/v1/users/{id}")
    suspend fun publicProfile(@Path("id") userId: String): PublicProfileDto

    @HTTP(method = "DELETE", path = "api/v1/users/me", hasBody = true)
    suspend fun deleteAccount(@Body body: DeleteAccountRequestDto): OkDto

    @POST("api/v1/users/{id}/block")
    suspend fun block(@Path("id") userId: String): OkDto

    @DELETE("api/v1/users/{id}/block")
    suspend fun unblock(@Path("id") userId: String): OkDto

    @GET("api/v1/users/me/blocked")
    suspend fun blockedUsers(): List<BlockedUserItemDto>
}

interface MediaApi {
    @Multipart
    @POST("api/v1/users/me/photos")
    suspend fun uploadPhoto(@Part file: MultipartBody.Part): PhotoDto

    @DELETE("api/v1/users/me/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String)

    @PUT("api/v1/users/me/photos/order")
    suspend fun reorderPhotos(@Body body: ReorderPhotosRequestDto): List<PhotoDto>
}

interface DiscoveryApi {
    @GET("api/v1/discovery/candidates")
    suspend fun candidates(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 10,
    ): PageDto<CandidateDto>
}

interface MatchingApi {
    @POST("api/v1/swipes")
    suspend fun swipe(@Body body: SwipeRequestDto): SwipeResponseDto

    @DELETE("api/v1/swipes/last")
    suspend fun undoLastSwipe(): UndoSwipeResponseDto

    @GET("api/v1/matches")
    suspend fun matches(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 30,
    ): PageDto<MatchItemDto>

    @DELETE("api/v1/matches/{id}")
    suspend fun unmatch(@Path("id") matchId: String)

    @GET("api/v1/likes/received")
    suspend fun likesReceived(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 30,
    ): PageDto<LikeReceivedDto>
}

interface ChatApi {
    @GET("api/v1/conversations")
    suspend fun conversations(): List<ConversationDto>

    @POST("api/v1/conversations/by-match/{matchId}")
    suspend fun conversationByMatch(@Path("matchId") matchId: String): ConversationDto

    @GET("api/v1/conversations/{id}/messages")
    suspend fun messages(
        @Path("id") conversationId: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 30,
    ): PageDto<MessageDto>

    @POST("api/v1/conversations/{id}/read")
    suspend fun markRead(@Path("id") conversationId: String): OkDto

    @Multipart
    @POST("api/v1/conversations/{id}/attachments")
    suspend fun uploadAttachment(
        @Path("id") conversationId: String,
        @Part file: MultipartBody.Part,
    ): AttachmentDto
}

interface NotificationApi {
    @GET("api/v1/notifications")
    suspend fun notifications(
        @Query("unread") unread: Boolean = false,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): PageDto<NotificationDto>

    @GET("api/v1/notifications/unread-count")
    suspend fun unreadCount(): UnreadCountDto

    @POST("api/v1/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): OkDto

    @POST("api/v1/notifications/read-all")
    suspend fun markAllRead(): OkDto
}

interface ReportApi {
    @POST("api/v1/reports")
    suspend fun submitReport(@Body body: ReportRequestDto): OkDto
}
