package cl.duoc.bancoxyz.payments.event;
import cl.duoc.bancoxyz.payments.model.Payment; import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; import org.springframework.kafka.core.KafkaTemplate; import org.springframework.stereotype.Component; import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Component @ConditionalOnProperty(name="app.kafka.enabled",havingValue="true")
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private final KafkaTemplate<String,Object> kafka; public KafkaDomainEventPublisher(KafkaTemplate<String,Object> kafka){this.kafka=kafka;}
    public void paymentCompleted(Payment p){var event=new PaymentCompletedEvent(UUID.randomUUID(),p.getOperationId(),p.getSourceAccountId(),p.getTargetAccountId(),p.getAmount(),p.getType(),Instant.now());kafka.send("pagos.realizados",p.getOperationId(),event);if(p.getAmount().compareTo(new BigDecimal("1000000"))>0){kafka.send("alertas.seguridad",p.getOperationId(),new SecurityAlertEvent(UUID.randomUUID(),p.getOperationId(),p.getSourceAccountId(),p.getAmount(),"Monto superior al límite de control",Instant.now()));}}
}
