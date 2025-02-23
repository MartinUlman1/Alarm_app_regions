package ivr.alarmregions.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "PREGION", schema = "REG")
public class PragueReg {

    @Id
    @Column(name = "IVR_PRAGUE_REG")
    private String ivrPragueReg;

    @Column(name = "IVR_PRAGUE_VALUE")
    private String ivrPragueValue;
}
