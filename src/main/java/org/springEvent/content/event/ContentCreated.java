package org.springEvent.content.event;

import lombok.Getter;
import lombok.Setter;
import org.springEvent.content.aggregate.Content;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class ContentCreated extends ApplicationEvent {
    //
    private String id;
    private String content;

    public ContentCreated(Content source) {
        //
        super(source);
        this.id = source.getId();
        this.content = source.getContent();
    }
}
