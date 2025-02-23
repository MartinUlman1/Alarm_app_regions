package ivr.alarmregions.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "REGION", schema = "REG")
public class Regions {

    @Column(name = "IVR_REG_PLACE")
    private String ivrRegPlace;

    @Id
    @Column(name = "IVR_REG_VALUE")
    private String ivrRegValue;

    public Regions(String ivrRegValue, String ivrRegPlace) {
        this.ivrRegValue = ivrRegValue;
        this.ivrRegPlace = ivrRegPlace;
    }

    public Regions() {
    }
}
