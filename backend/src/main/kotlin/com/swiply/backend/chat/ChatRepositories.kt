package com.swiply.backend.chat

import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface ConversationRepository : MongoRepository<Conversation, String> {
    fun findByMatchId(matchId: UUID): Conversation?
    fun findByParticipantIdsOrderByLastMessageAtDesc(participantId: UUID): List<Conversation>
}

interface ChatMessageRepository : MongoRepository<ChatMessage, String>
