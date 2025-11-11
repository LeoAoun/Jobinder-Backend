package br.com.jobinder.chatservice.service;

import br.com.jobinder.chatservice.domain.conversation.Conversation;
import br.com.jobinder.chatservice.domain.conversation.ConversationRepository;
import br.com.jobinder.chatservice.dto.MatchCreatedEvent;
import br.com.jobinder.chatservice.dto.conversation.ConversationCreateDTO;
import br.com.jobinder.chatservice.dto.conversation.ConversationDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Transactional
    public Conversation createConversation(MatchCreatedEvent event) {
        if (conversationRepository.existsByMatchId(event.matchId())) {
            return conversationRepository.findByMatchId(event.matchId()).orElse(null);
        }

        var conversation = new Conversation(
                null,
                event.matchId(),
                event.clientUserId(),
                event.professionalUserId(),
                null,
                null
        );
        return conversationRepository.save(conversation);
    }

    public ConversationDTO findConversationDTOById(UUID conversationId) {
        var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with ID: " + conversationId));

        return new ConversationDTO(
                conversation.getId(),
                conversation.getMatchId(),
                conversation.getClientUserId(),
                conversation.getProfessionalUserId(),
                conversation.getCreatedAt()
        );
    }

    public List<ConversationDTO> findConversationDTOsByUserId(UUID userId) {
        var conversations = conversationRepository.findAllByClientUserIdOrProfessionalUserId(userId, userId);

        return conversations.stream()
                .map(conversation -> new ConversationDTO(
                        conversation.getId(),
                        conversation.getMatchId(),
                        conversation.getClientUserId(),
                        conversation.getProfessionalUserId(),
                        conversation.getCreatedAt()
                ))
                .toList();
    }

    public List<ConversationDTO> findAllConversationDTOs() {
        return conversationRepository.findAll()
                .stream()
                .map(conversation -> new ConversationDTO(
                        conversation.getId(),
                        conversation.getMatchId(),
                        conversation.getClientUserId(),
                        conversation.getProfessionalUserId(),
                        conversation.getCreatedAt()
                ))
                .toList();
    }
}