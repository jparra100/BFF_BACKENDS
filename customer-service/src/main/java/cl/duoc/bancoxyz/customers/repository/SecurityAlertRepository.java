package cl.duoc.bancoxyz.customers.repository;
import cl.duoc.bancoxyz.customers.model.SecurityAlert; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface SecurityAlertRepository extends JpaRepository<SecurityAlert,UUID>{}
