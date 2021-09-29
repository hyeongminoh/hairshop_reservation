package hairshop.reservation;

import hairshop.reservation.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired HairshopRepository hairshopRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentFinished_CheckReservation(@Payload PaymentFinished paymentFinished){

        //예약이 결제 완료 후 미용사 매핑하러

        if(!paymentFinished.validate()) return;

        System.out.println("\n\n##### listener CheckReservation : " + paymentFinished.toJson() + "\n\n");

        // Sample Logic //
        Hairshop hairshop = new Hairshop();
        hairshop.setReservationId(paymentFinished.getReservationId());
        /*if(hairshop.getId()%2 == 1)
        {
            hairshop.setDesignerId("ChaHong");
        }
        else
        {
            hairshop.setDesignerId("ParkJun");
        }*/
        hairshop.setDesignerId("ChaHong");
        hairshop.setRsvStatus("RSV_REQUESTED");
        
        hairshopRepository.save(hairshop);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_CancelReservation(@Payload ReservationCanceled reservationCanceled){

        if(!reservationCanceled.validate()) return;

        System.out.println("\n\n##### listener CancelReservation : " + reservationCanceled.toJson() + "\n\n");



        // Sample Logic //
        Hairshop tmp = hairshopRepository.findByReservationId(reservationCanceled.getId());
        tmp.setDesignerId("CANCELED");
        tmp.setRsvStatus("RSV_CANCELED");
        hairshopRepository.save(tmp);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}