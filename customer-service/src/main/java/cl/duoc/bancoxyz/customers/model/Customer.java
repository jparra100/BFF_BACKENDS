package cl.duoc.bancoxyz.customers.model;
import jakarta.persistence.*;
@Entity @Table(name="bank_customer")
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String name; private String email; private String profile;
    protected Customer() { }
    public Customer(String name, String email, String profile) { this.name=name; this.email=email; this.profile=profile; }
    public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;} public String getProfile(){return profile;}
    public void update(String name,String email,String profile){this.name=name;this.email=email;this.profile=profile;}
}
