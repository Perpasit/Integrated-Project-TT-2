package sit.int221.intregratedapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.intregratedapi.entities.GlobalSetting;

import java.util.Optional;

@Repository
public interface GlobalSettingRepository extends JpaRepository<GlobalSetting, Integer> {
    Optional<GlobalSetting> findByName(String name);
}