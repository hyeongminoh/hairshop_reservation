package hairshop.reservation;

public class ReservationPlaced extends AbstractEvent {

    private Long id;
    private String customerId;
    private String date;
    private String stylingType;
    private String status;

    public ReservationPlaced(){
        super();
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
}