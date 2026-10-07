package cl.duoc.bancoxyz.payments.service;
import cl.duoc.bancoxyz.payments.dto.*; import cl.duoc.bancoxyz.payments.model.Payment; import cl.duoc.bancoxyz.payments.repository.PaymentRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service @Transactional public class PaymentProcessingService { private final PaymentRepository repository; public PaymentProcessingService(PaymentRepository repository){this.repository=repository;}
    public PaymentResponse process(PaymentRequest r){return repository.findByOperationId(r.operationId()).map(this::response).orElseGet(()->response(repository.save(new Payment(r.operationId(),r.sourceAccountId(),r.targetAccountId(),r.amount(),r.type().trim().toUpperCase(Locale.ROOT)))));}
    @Transactional(readOnly=true) public List<PaymentResponse> all(){return repository.findAll().stream().map(this::response).toList();}
    private PaymentResponse response(Payment p){return new PaymentResponse(p.getId(),p.getOperationId(),p.getSourceAccountId(),p.getTargetAccountId(),p.getAmount(),p.getType(),p.getStatus(),p.getCreatedAt());}
}
