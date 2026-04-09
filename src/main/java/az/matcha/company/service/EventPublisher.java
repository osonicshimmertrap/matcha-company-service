package az.matcha.company.service;

import az.matcha.company.config.RabbitMqConfig;
import az.matcha.company.event.CompanyMemberJoinedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMemberJoined(CompanyMemberJoinedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE,
                    RabbitMqConfig.MEMBER_JOINED_KEY,
                    event
            );
            log.debug("Published company.member.joined for userId={}", event.userId());
        } catch (Exception ex) {
            log.error("Failed to publish company.member.joined for userId={}: {}",
                    event.userId(), ex.getMessage());
        }
    }
}
