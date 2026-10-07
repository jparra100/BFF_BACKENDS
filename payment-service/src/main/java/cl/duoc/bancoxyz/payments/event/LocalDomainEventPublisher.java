package cl.duoc.bancoxyz.payments.event;
import cl.duoc.bancoxyz.payments.model.Payment; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="app.kafka.enabled",havingValue="false",matchIfMissing=true) public class LocalDomainEventPublisher implements DomainEventPublisher { public void paymentCompleted(Payment payment){ } }
