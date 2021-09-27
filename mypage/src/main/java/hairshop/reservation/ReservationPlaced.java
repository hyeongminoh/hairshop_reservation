package hairshop.reservation;

public class ReservationPlaced extends AbstractEvent {

    private Long id;
    private String customer_id;
    private String date;
    private String styling_type;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCustomerId() {
        return customer_id;
    }

    public void setCustomerId(String customer_id) {
        this.customer_id = customer_id;
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
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}