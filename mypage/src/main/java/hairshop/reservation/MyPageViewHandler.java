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


    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCompleted_then_UPDATE_1(@Payload ReservationCompleted reservationCompleted) {
        try {
            if (!reservationCompleted.validate()) return;
                // view 객체 조회

                    List<MyPage> myPageList = myPageRepository.findByReservationId(reservationCompleted.getReservationId());
                    for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setDesignerId(reservationCompleted.getDesignerId());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCanceled_then_UPDATE_2(@Payload ReservationCanceled reservationCanceled) {
        try {
            if (!reservationCanceled.validate()) return;
                // view 객체 조회

                    List<MyPage> myPageList = myPageRepository.findByReservationId(reservationCanceled.getId());
                    for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus(reservationCanceled.getStatus());
                    myPage.setDate(reservationCanceled.getDate());
                    myPage.setDesignerId(reservationCanceled.getStylingType());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

