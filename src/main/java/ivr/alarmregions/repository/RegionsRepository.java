package ivr.alarmregions.repository;

import ivr.alarmregions.entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RegionsRepository extends JpaRepository<Regions, String> {

    @Query("SELECT t FROM Regions t WHERE t.ivrRegPlace = ?1")
    Optional<Regions> findByIvrRegs(String ivrRegPlace);

    @Query("SELECT t FROM Regions t WHERE t.ivrRegPlace = :ivrRegValue AND t.ivrRegValue = :ivrRegValue")
    List<Regions> findByIvrRegAndIvrId(@Param("ivrRegPlace") String ivrRegPlace,
                                                   @Param("ivrRegValue") String ivrRegValue);
}
