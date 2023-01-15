# Spring Event

---
## Spring Event란?

Spring Event란 Spring Framework를 사용할 때 Bean 간 데이터를 주고받는 방식 중 하나로 코드의 관심사를 분리하여 구현할 수 있다는 장점을 가지고 있습니다.

Spring Event는 ApplicationContext에서 지원해주는 기능으로 이벤트를 발행(Publish)하고 이벤트를 수신(Listener)하는 크게 두 가지 기능을 지원해 줍니다.

---

## Spring Event 특징

한 프로젝트 내의 서비스들 간의 호출로 인한 의존성이 생겨 결합도가 높아지는 상황을 Spring Event를 사용함으로써 결합도가 낮아져 코드의 복잡도가 낮아지는 효과를 얻을 수 있습니다.

예를 들어 게시판이란 프로젝트가 존재하고 Aggregate는 Content와 Comment가 존재한다고 가정할 때

Content가 등록 되면 해당 Content의 댓글들이 저장될 수 있는 Comment가 생성되게 하고 싶다고 가정해 보겠습니다.

이때 기존 서비스 간 서로 호출한다 가정하면 Content를 생성하고 Comment를 생성 할 때까지의 과정이 전부 같은 Thread안에서 동작하며 Comment가 생성 완료 후 작업이 종료될 것입니다.
![Api.png](..%2F..%2F..%2F..%2F..%2FDownloads%2FApi.png)

코드로 확인해보면 하나의 비즈니스 로직 안에서 commentService의 create메소드를 호출하여 생성하는 것을 확인할 수 있습니다.

```java
public class ContentService {

	public void create(CreateContent command) {
	    //
	    String newId = UUID.randomUUID().toString();
	    command.setId(newId);
	    Content content = new Content(newId, command.getContent());
	    
	    //Content 생성
	    this.contentStore.create(content);
	    
	    //Comment space 생성
	    this.commentService.create(content.getId());
	}

}
```

Event를 발행하여 구현할 때 ContentCreate와 CommentCreate 가 직접 연결되어 있는 것이 아닌 ApplicationEventPublisher 가 사이에 껴 두 관심사의 연결을 끊어 주는 것을 확인할 수 있습니다.

![Event.png](..%2F..%2F..%2F..%2F..%2FDownloads%2FEvent.png)

ContentService가 CommentService.create 메서드를 직접 호출하지 않고 applicationEventPublisher를 이용하여 ContentCreated 이벤트 객체를 발행합니다.

```java
public class ContentService {

	public void create(CreateContent command) {
	    //
	    String newId = UUID.randomUUID().toString();
	    command.setId(newId);
	    Content content = new Content(newId, command.getContent());
	
	    //Content 생성
	    this.contentStore.create(content);
	
	    //Content 생성 후 Event 발행
	    **ContentCreated event = new ContentCreated(content);**
	    **this.applicationEventPublisher.publishEvent(event);**
	}

}
```

Event가 발행되면  `EventListenerMethodProcessor`에서 `@EventListener`가 붙어있는 Bean 들을 모아 해당 Bean들 중 발행한 Event와 같은 Scope(Type)를 가지고 있는 메소드를 찾아 해당 메소드를 실행시켜 줍니다.

```java
public class ContentEventHandler {

	@EventListener
	public void on(ContentCreated event) {
	    //
	    String contentId = event.getId();
	    CreateComment command = new CreateComment(contentId);
	    this.commentService.create(command);
	}

}
```

이로써 Content를 생성할 때 Comment도 같이 생성하던 것을 Content가 생성되면 Comment도 생성이 되는 결합도가 낮은 코드를 작성할 수 있게 되는 것입니다.

---

## Spring Event 사용방법

### 1- 발행할 Event 객체 생성

ApplicationContext의 .publishEvent()는 Object or ApplicationEvent타입을 가진 인자를 전달받을 수 있습니다. 따라서 Event에 담을 데이터를 정의를 한 다음 편하게 사용하시면 될 것 같습니다.

> `ApplicationEventPublisher` 를 사용하여 event를 발행해야 하지만`ApplicationContext` 가 `ApplicationEventPublisher` 를 상속받고 있으므로 본문에서는 `ApplicationContext` 를 사용하여 구현하도록 하겠습니다.
>

Object정의

```java
@Getter
@Setter
public class DomainCreated {
    //
    private String domainId;

    public DomainCreated(String domainId) {
        //
        this.domainId = domainId;
    }
}
```

ApplicationEvent 상속

```java
@Getter
@Setter
public class DomainCreated extends ApplicationEvent {
    //
    private final String domainId;

    public DomainCreated(Object source, String domainId) {
        //
        super(source);
        this.domainId = domainId;
    }
}
```

### 2- Service에서 Event 발행

Event를 발행하기 위해 ApplicationContext를 주입받아 publishEvent 메서드를 사용하여 Event를 발행할 수 있습니다. 아래 코드처럼 비즈니스 로직 이후 혹은 이전으로 하여 목표에 맞게 Event를 발행하면 될 것 같습니다.

```java
@Service
public class DomainService {
    //
    private final ApplicationContext applicationContext;
    
    public DomainService(ApplicationContext applicationContext) {
        //
        this.applicationContext = applicationContext;
    }
    
    public void createDomain() {
        //Domain 생성 로직 작성
        
        // Domain 생성 이후 Event 발행
        this.applicationContext.publishEvent(new DomainCreated("domain생성 Id"));
    }
}
```

### 3- Event Listen

ApplicationContext에 발행된 Event를 받으려면 @EventListener를 사용하고 파라미터로 받아야 하는 Event의 타입을 명시해 주면 해당 Event가 발행되었을 때 메서드가 작동하여 내부 로직을 실행시킵니다.

```java
@Slf4j
@Component
public class DomainHandler {
    //
    @EventListener
    public void on(DomainCreated event) {
        //
        log.info(String.format("Event Data : %s", event.getDomainId()));
    }
}
```

---

## Spring Event 비동기

Spring Event는 Event라는 단어로 인해 비동기일 것 이다 라고 오해를 할 수 있는데, 사실 Spring Event는 기본이 동기 방식으로 작동합니다.

Event를 Publish하는 Thread와 Event를 Listen 하는 Thread의 Thread id를 확인해 보면 Thread의 id가 서로 같은 것을 확인할 수 있습니다.

![test1.png](..%2F..%2F..%2F..%2F..%2FDownloads%2Ftest1.png)

이러하듯 동기로 작동하는 Spring Event를 비동기로 사용하기 위해서는 몇 가지 설정이 필요합니다.

…Application

Application Class에 `@EnableAsync` 어노테이션을 달아 줍니다.

```java
@EnableAsync
@SpringBootApplication
public class SpringEventApplication {

    public static void main(String[] args) {
        //
        SpringApplication.run(SpringEventApplication.class, args);
    }
}
```

비동기로 작동해야 하는 EventListener에 `@Async`어노테이션을 설정해 줍니다.

```java
@EventListener
@Async
public void on(DomainCreated event) {
    //
    log.info(String.format("Listener thread id: %s", Thread.currentThread().getId()));
}
```

이후 테스트해 보면 할당된 Thread의 id가 서로 다른 것을 확인할 수 있습니다.

![test2.png](..%2F..%2F..%2F..%2F..%2FDownloads%2Ftest2.png)

---

## Spring Event 순서 정하기

Event를 사용하다 보면 EventListener의 작동 순서가 중요한 때도 있습니다.

예를 들어 A를 생성하고 B를 생성한 다음 C는 B를 조회한 다음 생성한다고 가정할 때

C의 EventListener가 B의 Listener보다 먼저 잘동할 때 오류가 발생할 수 있습니다.

이때 `@Order(1)` 어노테이션을 사용할 때 Listener작동 순서를 정할 수 있습니다.

Order에 명시한 숫자가 낮을수록 먼저 실행됩니다.

```java
@EventListener
@Order(1)
public void on(DomainCreated event) {
    //
    log.info(String.format("Event Data : %s", event.getDomainId()));
}
```

---

## Spring Event를 사용하면서…

최근 프로젝트에서 Spring Event 적용하여 Service간의 결합도를 낮출 수 있었고 코드를 구현할 때 해당 관심사만을 생각하며 코딩을 할 수 있어 편리함과 동시에 오류를 찾기 편했다는 장점이 있었습니다.

하지만 사용할 때 무작위한 Event발행은 오히려 혼란을 초래할 수 있어 Event로 발행할 객체의 명칭을 사용 목적에 맞춰 정확하게 명시하는  것이 무엇보다 중요하다고 생각했습니다.
