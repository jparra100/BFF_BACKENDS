package cl.duoc.bancoxyz.payments.repository;
import cl.duoc.bancoxyz.payments.model.Payment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,UUID>{Optional<Payment> findByOperationId(String operationId);}
