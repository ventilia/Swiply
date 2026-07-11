package com.swiply.backend.admin

import com.swiply.backend.auth.UserRepository
import com.swiply.backend.common.UnauthorizedException
import com.swiply.backend.common.UserStatus
import com.swiply.backend.media.PhotoStatus
import com.swiply.backend.moderation.ModerationService
import com.swiply.backend.moderation.ReportStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID


@Controller
class AdminDashboardController(
    private val queryService: AdminQueryService,
    private val moderationService: ModerationService,
    private val userRepository: UserRepository,
) {

    @GetMapping("/admin/login")
    fun login(): String = "admin/login"

    @GetMapping("/admin")
    fun index(): String = "redirect:/admin/dashboard"

    @GetMapping("/admin/dashboard")
    fun dashboard(model: Model): String {
        model.addAttribute("stats", queryService.stats())
        model.addAttribute("active", "dashboard")
        return "admin/dashboard"
    }

    @GetMapping("/admin/users")
    fun users(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) status: UserStatus?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model,
    ): String {
        val safePage = page.coerceAtLeast(0)
        model.addAttribute("users", queryService.searchUsers(query, status, safePage * 25, 25))
        model.addAttribute("query", query ?: "")
        model.addAttribute("status", status?.name ?: "")
        model.addAttribute("page", safePage)
        model.addAttribute("active", "users")
        return "admin/users"
    }

    @GetMapping("/admin/users/{id}")
    fun userDetail(@PathVariable id: UUID, model: Model): String {
        val detail = queryService.userDetail(id) ?: return "redirect:/admin/users"
        model.addAttribute("u", detail)
        model.addAttribute("active", "users")
        return "admin/user_detail"
    }

    @PostMapping("/admin/users/{id}/{action}")
    fun moderateUser(
        @PathVariable id: UUID,
        @PathVariable action: String,
        @RequestParam(required = false) reason: String?,
        authentication: Authentication,
    ): String {
        val moderatorId = moderatorId(authentication)
        when (action) {
            "warn" -> moderationService.warn(moderatorId, id, reason)
            "suspend" -> moderationService.suspend(moderatorId, id, reason)
            "ban" -> moderationService.ban(moderatorId, id, reason)
            "unban" -> moderationService.unban(moderatorId, id, reason)
        }
        return "redirect:/admin/users"
    }

    @GetMapping("/admin/reports")
    fun reports(
        @RequestParam(required = false, defaultValue = "PENDING") status: ReportStatus,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model,
    ): String {
        model.addAttribute("reports", queryService.reports(status, page.coerceAtLeast(0), 25))
        model.addAttribute("status", status.name)
        model.addAttribute("active", "reports")
        return "admin/reports"
    }

    @PostMapping("/admin/reports/{id}/resolve")
    fun resolveReport(
        @PathVariable id: UUID,
        @RequestParam(required = false, defaultValue = "false") dismiss: Boolean,
        @RequestParam(required = false, defaultValue = "false") banTarget: Boolean,
        @RequestParam(required = false) targetUserId: UUID?,
        @RequestParam(required = false) note: String?,
        authentication: Authentication,
    ): String {
        val moderatorId = moderatorId(authentication)
        if (banTarget && targetUserId != null) {
            moderationService.ban(moderatorId, targetUserId, note ?: "По результатам репорта $id")
        }
        moderationService.resolveReport(moderatorId, id, dismiss, note)
        return "redirect:/admin/reports"
    }

    @GetMapping("/admin/photos")
    fun photos(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        model: Model,
    ): String {
        model.addAttribute("photos", queryService.photosForModeration(PhotoStatus.PENDING, page.coerceAtLeast(0), 24))
        model.addAttribute("active", "photos")
        return "admin/photos"
    }

    @PostMapping("/admin/photos/{id}/{action}")
    fun moderatePhoto(
        @PathVariable id: UUID,
        @PathVariable action: String,
        authentication: Authentication,
    ): String {
        val moderatorId = moderatorId(authentication)
        when (action) {
            "approve" -> moderationService.approvePhoto(moderatorId, id)
            "reject" -> moderationService.rejectPhoto(moderatorId, id)
        }
        return "redirect:/admin/photos"
    }

    private fun moderatorId(authentication: Authentication): UUID =
        userRepository.findByEmail(authentication.name)?.id
            ?: throw UnauthorizedException("Модератор не найден")
}
