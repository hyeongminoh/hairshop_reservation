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
    @Autowired ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCompleted_UpdateStatus(@Payload ReservationCompleted reservationCompleted){

        if(!reservationCompleted.validate()) return;

        //예약이 미용실에서 미용사까지 매핑 된 후
        System.out.println("\n\n##### listener UpdateStatus : " + reservationCompleted.toJson() + "\n\n");


        // Sample Logic //
        //예약 완료 된 예약 찾기
        Reservation reservation = reservationRepository.findById(reservationCompleted.getReservationId()).get();
        reservation.setStatus("Reservation Completed");
        reservation.setShopStatus("Designer Matched");
        reservation.setPayStatus("Pay Finished");
        reservationRepository.save(reservation);

    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelCompleted_UpdateStatus(@Payload CancelCompleted cancelCompleted){

        //미용실에서 취소 완료 되어 미용사 null
        if(!cancelCompleted.validate()) return;

        System.out.println("\n\n##### listener UpdateStatus : " + cancelCompleted.toJson() + "\n\n");



        // Sample Logic //
        Reservation reservation = reservationRepository.findById(cancelCompleted.getId()).get();
        reservation.setStatus("Reservation Canceled");
        reservation.setShopStatus("Designer Deleted");
        reservation.setPayStatus("Pay Canceled");
        reservationRepository.save(reservation);

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}