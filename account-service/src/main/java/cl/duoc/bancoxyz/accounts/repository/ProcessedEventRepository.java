package cl.duoc.bancoxyz.accounts.repository;
import cl.duoc.bancoxyz.accounts.model.ProcessedEvent; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent,UUID>{}
