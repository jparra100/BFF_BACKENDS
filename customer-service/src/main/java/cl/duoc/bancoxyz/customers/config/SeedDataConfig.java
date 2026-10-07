package cl.duoc.bancoxyz.customers.config;
import cl.duoc.bancoxyz.customers.model.Customer; import cl.duoc.bancoxyz.customers.repository.CustomerRepository; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.*;
@Configuration public class SeedDataConfig { @Bean CommandLineRunner seedCustomers(CustomerRepository r){return args->{if(r.count()==0){r.save(new Customer("Ana Pérez","ana@example.com","PERSONA"));r.save(new Customer("Luis Soto","luis@example.com","PERSONA"));r.save(new Customer("Comercial XYZ","contacto@xyz.cl","EMPRESA"));}};} }
