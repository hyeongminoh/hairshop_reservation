package hairshop.reservation;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyPageRepository extends CrudRepository<MyPage, Long> {

    List<MyPage> findByReservationId(Long reservationId);
    //List<MyPage> findByReservationId(Long reservationId);

}