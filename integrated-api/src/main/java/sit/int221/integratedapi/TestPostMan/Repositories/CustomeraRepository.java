package sit.int221.integratedapi.TestPostMan.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.integratedapi.TestPostMan.Entities.Customera;

import java.util.List;

public interface CustomeraRepository extends JpaRepository<Customera,Long> {
    List<Customera> findByFirstNameContains(String param);
}
