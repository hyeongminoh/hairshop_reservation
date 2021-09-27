package hairshop.reservation;

public class ReservationCompleted extends AbstractEvent {

    private Long id;
    private Long reservation_id;
    private String designer_id;

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

    public String getDesignerId() {
        return designer_id;
    }

    public void setDesignerId(String designer_id) {
        this.designer_id = designer_id;
    }
}