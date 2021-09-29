package hairshop.reservation;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerId;
    private String date;
    private String stylingType;
    private String status;
    private String shopstatus;
    private String paystatus;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 미용실 예약 요청 ============== ");
        ReservationPlaced reservationPlaced = new ReservationPlaced();
        BeanUtils.copyProperties(this, reservationPlaced);
        reservationPlaced.publishAfterCommit();



        hairshop.reservation.external.Payment payment = new hairshop.reservation.external.Payment();
        // mappings goes here
        /* 결제(payment) 동기 호출 진행 */
        /* 결제 진행 가능 여부 확인 후 미용실매핑 */

        if(this.getStatus().equals("RSV_REQUESTED")){

            payment.setReservationId(this.getId());
            payment.setStatus("PAYMENT_REQUESTED");
        }
        
        ReservationApplication.applicationContext.getBean(hairshop.reservation.external.PaymentService.class)
            .requestpayment(payment);

    }

    @PrePersist
    public void onPrePersist(){
        System.out.println(" ============== 미용실 예약 요청 전 ============== ");
        status = "RSV_REQUESTED";
        shopstatus = "NULL";
        paystatus = "NULL";

    }

    @PostUpdate
    public void onPostUpdate(){

        System.out.println(" ============== 예약 취소 요청 ============== ");

        if(this.getStatus().equals("CANCEL_REQUESTED") ){
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            reservationCanceled.publishAfterCommit();
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getStylingType() {
        return stylingType;
    }

    public void setStylingType(String stylingType) {
        this.stylingType = stylingType;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShopStatus() {
        return shopstatus;
    }

    public void setShopStatus(String shopstatus) {
        this.shopstatus = shopstatus;
    }

    
    public String getPayStatus() {
        return paystatus;
    }

    public void setPayStatus(String paystatus) {
        this.paystatus = paystatus;
    }

}