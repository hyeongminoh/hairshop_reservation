package hairshop.reservation;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="hairshops", path="hairshops")
public interface HairshopRepository extends PagingAndSortingRepository<Hairshop, Long>{
    Hairshop findByReservationId(Long reservationId);

}
