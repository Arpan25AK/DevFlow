package com.dev.chat_service.repo;
import com.dev.chat_service.entity.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByPullrequestIdOrderByTimeStampAsc(String pullrequestId);

}