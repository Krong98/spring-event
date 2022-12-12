package org.springEvent.content.service;

import lombok.extern.slf4j.Slf4j;
import org.springEvent.content.aggregate.Content;
import org.springEvent.content.command.CreateContent;
import org.springEvent.content.event.ContentCreated;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ContentService {
    //
    private final ApplicationEventPublisher applicationEventPublisher;

    public ContentService(ApplicationEventPublisher applicationEventPublisher) {
        //
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void create(CreateContent command) {
        //
        String newId = UUID.randomUUID().toString();
        command.setId(newId);
        Content content = new Content(newId, command.getContent());
        ContentCreated event = new ContentCreated(content);

        log.info("Content Create");
        log.info(String.format("Tread Id : %s", Thread.currentThread().getId()));

        this.applicationEventPublisher.publishEvent(event);
    }
}
