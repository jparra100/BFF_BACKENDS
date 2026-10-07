package cl.duoc.bancoxyz.customers.controller;
import cl.duoc.bancoxyz.customers.dto.*; import cl.duoc.bancoxyz.customers.service.CustomerManagementService; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerManagementService service; public CustomerController(CustomerManagementService service){this.service=service;}
    @GetMapping public List<CustomerResponse> all(){return service.all();} @GetMapping("/{id}") public CustomerResponse one(@PathVariable long id){return service.one(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public CustomerResponse create(@Valid @RequestBody CustomerRequest r){return service.create(r);}
    @PutMapping("/{id}") public CustomerResponse update(@PathVariable long id,@Valid @RequestBody CustomerRequest r){return service.update(id,r);}
}
