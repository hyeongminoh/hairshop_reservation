package hairshop.reservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Hairshop_table")
public class Hairshop {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long reservationId;
    private String designerId;

    @PostPersist
    public void onPostPersist(){
        ReservationCompleted reservationCompleted = new ReservationCompleted();
        BeanUtils.copyProperties(this, reservationCompleted);
        reservationCompleted.publishAfterCommit();

    }
    @PostUpdate
    public void onPostUpdate(){
        CancelCompleted cancelCompleted = new CancelCompleted();
        BeanUtils.copyProperties(this, cancelCompleted);
        cancelCompleted.publishAfterCommit();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }


    public String getDesignerId() {
        return designerId;
    }

    public void setDesignerId(String designerId) {
        this.designerId = designerId;
    }




}