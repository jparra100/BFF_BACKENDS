package cl.duoc.bancoxyz.payments.event;
import cl.duoc.bancoxyz.payments.model.Payment;
public interface DomainEventPublisher { void paymentCompleted(Payment payment); }
