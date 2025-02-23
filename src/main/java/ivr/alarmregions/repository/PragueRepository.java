package ivr.alarmregions.repository;

import ivr.alarmregions.entity.PragueReg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PragueRepository extends JpaRepository<PragueReg, String> {


}
