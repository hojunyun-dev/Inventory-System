package com.inventory.repository;

import com.inventory.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipient(String recipient);
    List<Message> findBySender(String sender);
    List<Message> findByIsReadFalse();
    Long countByIsReadFalse();
}

