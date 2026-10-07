package cl.duoc.bancoxyz.payments.model;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="payment")
public class Payment { @Id private UUID id; @Column(unique=true,nullable=false) private String operationId; private Long sourceAccountId; private Long targetAccountId; private BigDecimal amount; private String type; private String status; private Instant createdAt;
    protected Payment(){} public Payment(String operationId,Long source,Long target,BigDecimal amount,String type){this.id=UUID.randomUUID();this.operationId=operationId;this.sourceAccountId=source;this.targetAccountId=target;this.amount=amount;this.type=type;this.status="COMPLETED";this.createdAt=Instant.now();}
    public UUID getId(){return id;} public String getOperationId(){return operationId;} public Long getSourceAccountId(){return sourceAccountId;} public Long getTargetAccountId(){return targetAccountId;} public BigDecimal getAmount(){return amount;} public String getType(){return type;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
}
