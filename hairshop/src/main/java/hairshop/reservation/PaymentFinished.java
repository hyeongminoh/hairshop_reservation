package hairshop.reservation;

public class PaymentFinished extends AbstractEvent {

    private Long id;
    private Long reservation_id;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getReservationId() {
        return reservation_id;
    }

    public void setReservationId(Long reservation_id) {
        this.reservation_id = reservation_id;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}