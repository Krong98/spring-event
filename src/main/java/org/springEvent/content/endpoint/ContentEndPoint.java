package org.springEvent.content.endpoint;

import org.springEvent.content.command.CreateContent;
import org.springEvent.content.service.ContentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
public class ContentEndPoint {
    //
    private final ContentService contentService;

    public ContentEndPoint(ContentService contentService) {
        //
        this.contentService = contentService;
    }

    @PostMapping
    public void createContent(@RequestBody CreateContent command) {
        //
        contentService.create(command);
    }
}
