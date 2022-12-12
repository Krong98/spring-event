package org.springEvent.space.service;

import lombok.extern.slf4j.Slf4j;
import org.springEvent.space.command.CreateSpace;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class SpaceService {
    //
    public void create(CreateSpace command) {
        //
        String newId = UUID.randomUUID().toString();
        command.setId(newId);
        log.info("Space Create");
        log.info(String.format("Tread Id : %s", Thread.currentThread().getId()));
    }
}
