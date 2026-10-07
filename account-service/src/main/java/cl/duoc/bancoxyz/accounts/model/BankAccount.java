package cl.duoc.bancoxyz.accounts.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String type;
    private BigDecimal balance;
    private String status;
    @Version
    private long version;

    protected BankAccount() { }

    public BankAccount(Long customerId, String type, BigDecimal balance) {
        this.customerId = customerId;
        this.type = type;
        this.balance = balance;
        this.status = "ACTIVE";
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public String getStatus() { return status; }
    public void setType(String type) { this.type = type; }
    public void close() { this.status = "CLOSED"; }
    public void credit(BigDecimal amount) { this.balance = this.balance.add(amount); }
    public void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) throw new IllegalStateException("Saldo insuficiente");
        this.balance = this.balance.subtract(amount);
    }
}
