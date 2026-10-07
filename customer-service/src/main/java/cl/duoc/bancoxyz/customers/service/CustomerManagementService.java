package cl.duoc.bancoxyz.customers.service;
import cl.duoc.bancoxyz.customers.dto.*; import cl.duoc.bancoxyz.customers.model.Customer; import cl.duoc.bancoxyz.customers.repository.CustomerRepository;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service @Transactional
public class CustomerManagementService {
    private final CustomerRepository repository; public CustomerManagementService(CustomerRepository repository){this.repository=repository;}
    @Transactional(readOnly=true) public List<CustomerResponse> all(){return repository.findAll().stream().map(this::response).toList();}
    @Transactional(readOnly=true) public CustomerResponse one(long id){return response(require(id));}
    public CustomerResponse create(CustomerRequest r){return response(repository.save(new Customer(r.name(),r.email(),r.profile())));}
    public CustomerResponse update(long id,CustomerRequest r){Customer c=require(id);c.update(r.name(),r.email(),r.profile());return response(c);}
    private Customer require(long id){return repository.findById(id).orElseThrow(()->new IllegalArgumentException("Cliente no encontrado"));}
    private CustomerResponse response(Customer c){return new CustomerResponse(c.getId(),c.getName(),c.getEmail(),c.getProfile());}
}
