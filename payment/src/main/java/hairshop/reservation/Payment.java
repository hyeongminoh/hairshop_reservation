package hairshop.reservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long reservationId;
    private String status;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 예약 결제 요청 ============== ");

        PaymentFinished paymentFinished = new PaymentFinished();
        BeanUtils.copyProperties(this, paymentFinished);
        paymentFinished.publishAfterCommit();

    }

    @PrePersist
    public void onPrePersist(){
        System.out.println(" ============== 예약 결제 전 ============== ");
        status = "PAY_COMPLETED";
    }

    @PostUpdate
    public void onPostUpdate(){
        PaymentDenied paymentDenied = new PaymentDenied();
        BeanUtils.copyProperties(this, paymentDenied);
        paymentDenied.publishAfterCommit();

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
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}