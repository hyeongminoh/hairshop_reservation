package hairshop.reservation;

import hairshop.reservation.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {


    @Autowired
    private MyPageRepository myPageRepository;

    //고객이 미용예약을 요청함
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationPlaced_then_CREATE_1 (@Payload ReservationPlaced reservationPlaced) {
        try {

            if (!reservationPlaced.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setReservationId(reservationPlaced.getId());
            myPage.setStatus(reservationPlaced.getStatus());
            myPage.setCustomerId(reservationPlaced.getCustomerId());
            myPage.setDate(reservationPlaced.getDate());
            myPage.setStylingType(reservationPlaced.getStylingType());
            myPage.setDesignerId("Not Matched Yet!");
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //예약이 완료됨
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCompleted_then_UPDATE_1(@Payload ReservationCompleted reservationCompleted) {
        try {
            if (!reservationCompleted.validate()) return;
                // view 객체 조회

                    List<MyPage> myPageList = myPageRepository.findByReservationId(reservationCompleted.getReservationId());
                    for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setDesignerId(reservationCompleted.getDesignerId());
                    myPage.setStatus("Reservation Completed");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //예약이 취소됨
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCancelCompleted_then_UPDATE_2(@Payload CancelCompleted cancelCompleted) {
        try {
            if (!cancelCompleted.validate()) return;
                // view 객체 조회

                    List<MyPage> myPageList = myPageRepository.findByReservationId(cancelCompleted.getId());
                    for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus("Reservation Canceled");
                    myPage.setDesignerId(cancelCompleted.getDesignerId());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /*
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCanceled_then_UPDATE_2(@Payload ReservationCanceled reservationCanceled) {
        try {
            if (!reservationCanceled.validate()) return;
                // view 객체 조회

                    List<MyPage> myPageList = myPageRepository.findByReservationId(reservationCanceled.getId());
                    for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus("Reservation Canceled");
                    myPage.setDate(reservationCanceled.getDate());
                    myPage.setDesignerId(reservationCanceled.getStylingType());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

}

