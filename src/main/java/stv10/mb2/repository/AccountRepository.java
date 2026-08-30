package stv10.mb2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stv10.mb2.model.Account;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
