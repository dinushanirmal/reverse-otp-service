package com.example.reverseotp.repository;

import com.example.reverseotp.entity.IncomingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomingMessageRepository extends JpaRepository<IncomingMessage, Long> {

    List<IncomingMessage> findByExtractedRequestId(String extractedRequestId);

    List<IncomingMessage> findBySenderNumber(String senderNumber);
}
