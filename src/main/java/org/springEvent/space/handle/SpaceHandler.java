package org.springEvent.space.handle;

import org.springEvent.content.event.ContentCreated;
import org.springEvent.space.command.CreateSpace;
import org.springEvent.space.service.SpaceService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SpaceHandler {
    //
    private final SpaceService spaceService;


    public SpaceHandler(SpaceService spaceService) {
        //
        this.spaceService = spaceService;
    }

    @Async
    @EventListener
    public void on (ContentCreated event) {
        CreateSpace command = new CreateSpace("", event.getId());
        spaceService.create(command);
    }
}
