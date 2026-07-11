package com.swiply.app.core.model

enum class Gender { MALE, FEMALE, OTHER }

enum class SwipeAction { LIKE, DISLIKE, SUPERLIKE }

enum class PhotoStatus { PENDING, APPROVED, REJECTED }

enum class MessageType { TEXT, IMAGE, SYSTEM }

enum class MessageStatus { SENT, DELIVERED, READ }

enum class NotificationType { NEW_MATCH, NEW_MESSAGE, NEW_LIKE, MODERATION, SYSTEM }

enum class ReportReason { SPAM, FAKE_PROFILE, INAPPROPRIATE_CONTENT, HARASSMENT, UNDERAGE, OTHER }
