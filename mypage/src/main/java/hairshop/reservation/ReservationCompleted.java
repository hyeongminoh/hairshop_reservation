package hairshop.reservation;

public class ReservationCompleted extends AbstractEvent {

    private Long id;
    private Long reservation_id;
    private String date;
    private String styling_type;
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
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getStylingType() {
        return styling_type;
    }

    public void setStylingType(String styling_type) {
        this.styling_type = styling_type;
    }
    public String getDesignerId() {
        return designer_id;
    }

    public void setDesignerId(String designer_id) {
        this.designer_id = designer_id;
    }
}