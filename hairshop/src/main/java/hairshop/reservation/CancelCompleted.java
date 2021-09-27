package hairshop.reservation;

public class CancelCompleted extends AbstractEvent {

    private Long id;
    private String reservationId;
    private String date;
    private String stylingType;
    private String designerId;

    public CancelCompleted(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
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
    public String getDesignerId() {
        return designerId;
    }

    public void setDesignerId(String designerId) {
        this.designerId = designerId;
    }
}