package hairshop.reservation;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long reservationId;
        private String status;
        private String customerId;
        private String date;
        private String stylingType;
        private String designerId;


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
        public String getDesignerId() {
            return designerId;
        }

        public void setDesignerId(String designerId) {
            this.designerId = designerId;
        }

}