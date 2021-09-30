<img src = "https://user-images.githubusercontent.com/29780972/135097518-f3f7e4b3-7edc-44ca-a321-cc460aa155b3.png" width="200" height="100"/>

# hairshop_reservation
spring boot를 활용한 미용실 예약 플랫폼

# 미용실 예약
클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트 확인

# Table of contents

- [백신예약](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [ConfigMap 설정](#ConfigMap-설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
    - [Self healing](#Liveness-Probe)


# 서비스 시나리오
기능적 요구사항
1. 고객이 날짜, 미용실, 헤어스타일종류를 선택하고 미용실 예약 요청을 한다.
2. 예약 요청이 결제 되면 미용실에 전달 된다.
3. 미용실에서 예약을 확인 후 미용사를 매핑하면 예약이 완료된다.
4. 예약이 완료되면 고객의 예약 상태를 완료로 업데이트 한다.
5. 고객이 예약을 취소 요청을 한다.
6. 미용실에서 취소 요청된 예약을 확인 후 해당 예약의 미용사를 취소시키면 예약이 취소된다.
7. 예약이 취소되면 고객의 예약상태를 취소로 업데이트 한다.
8. 고객은 날짜, 미용실, 헤어스타일종류, 미용사, 예약상태 확인할 수 있다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 예약 요청은 미용사를 매핑 할 수 없다.  (Sync 호출)
2. 장애격리
    1. 미용실의 시스템이 수행되지 않더라도 예약 요청 승인, 취소 요청을 을 받을 수 있다. Async (event-driven), Eventual Consistency
    1. 예약 요청 승인이 과중되면 고객을 잠시동안 받지 않고 예약 요청을 잠시후에 하도록 유도한다  Circuit breaker, fallback
3. 성능
    1. 미용실 예약에 대한 정보 및 예약 상태 등을 한 화면에서 확인 할 수 있다. CQRS

 - View Model 추가
 ![image](https://user-images.githubusercontent.com/29780972/135188276-92b75ef6-4270-4417-836c-8b3af5d480a2.png)


![image](https://user-images.githubusercontent.com/29780972/135099574-dceaaa9c-6b56-449a-8488-d9d023236f30.png)

    - 고객이 날짜, 미용실, 헤어스타일종류를 선택하여 예약한다. (ok)
    - 선금을 결제한다. (ok)
    - 예약 결제가 완료되면 예약 요청한 내역이 미용실에게 전달된다. (ok)
    - 미용실은 예약 요청 내역을 확인 후 미용사를 매핑하여 예약을 완료한다.(ok)
    - 고객은 중간중간 예약 현황을 조회한다. (View-green sticker 의 추가로 ok)
    
    - 고객이 예약을 취소할 수 있다. (ok)
    - 예약이 취소되면 예약 상태가 변경되고 매핑된 미용사가 사라진다.(ok)  

### 비기능 요구사항에 대한 검증

- 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 예약 결제 요청 시:  결제가 완료되지 않은 예약은 절대 받지 않는다는 정책에 따라, ACID 트랜잭션 적용. 예약 결제 요청시 승인처리에 대해서는 Request-Response 방식 처리
        - 결제 완료 시 미용실 예약 완료 및 예약 상태 변경 처리:  결제서비스에서 마이크로서비스로 예약완료내역이 전달되는 과정에 있어서 hairshop 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
        - 나머지 모든 inter-microservice 트랜잭션: 예약상태, 매핑 된 미용사 등 모든 이벤트에 대해 MyPage처리 등, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.
	- 미용시 시스템이 수행되지 않더라도 예약 및 결제는 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
        - 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다  Circuit breaker, fallback


## 구현:
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)
```
   cd reservation
   mvn spring-boot:run
   
   cd payment
   mvn spring-boot:run
   
   cd hairshop
   mvn spring-boot:run
   
   cd mypage
   mvn spring-boot:run
   
   cd gateway
   mvn spring-boot:run
   
```

## CQRS

미용실 예약/취소/매핑 등 총 Status 및 배정된 미용사에 대하여 고객이 조회 할 수 있도록 CQRS 로 구현하였다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다
- Table 모델링


![image](https://user-images.githubusercontent.com/29780972/135100776-d94f3c61-17b8-49af-86db-2a5e0e478016.png)

- mypage MSA PolicyHandler를 통해 구현
   ("ReservationPlaced" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
   
![image](https://user-images.githubusercontent.com/29780972/135101051-00a7a146-b847-49c6-8f6b-7e308cc75e2c.png)

 ("ReservationCompleted" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
 
 ![image](https://user-images.githubusercontent.com/29780972/135101282-61175efb-3ae9-43e0-844b-38e8445d67fe.png)

 ("CancelCompleted" 이벤트 발생 시, Pub/Sub 기반으로 별도 테이블에 저장)
 
 ![image](https://user-images.githubusercontent.com/29780972/135103809-1f5d8dc1-a971-4e55-994e-3ae393dca010.png)



- 실제로 view 페이지를 조회해 보면 모든 room에 대한 정보, 예약 상태, 결제 상태 등의 정보를 종합적으로 알 수 있다.
- 
![image](https://user-images.githubusercontent.com/29780972/135103894-8081f9e4-8cb7-45d4-9faf-b3fac5918cf2.png)


## API 게이트웨이

![image](https://user-images.githubusercontent.com/29780972/135102501-a3646647-7767-4724-bffe-8e5330f909f2.png)

## Correlation

hairshop_reservation 프로젝트에서는 PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현하고 있습니다. 

아래의 구현 예제를 보면

예약(Reservation)을 하면 동시에 연관된 미용실(hairshop), 결제(payment) 등의 서비스의 상태가 적당하게 변경이 되고,
예약건의 취소를 수행하면 다시 연관된  미용실(hairshop), 결제(payment) 등의 서비스의 상태값 등의 데이터가 적당한 상태로 변경되는 것을
확인할 수 있습니다.

- 미용실 예약 요청
http POST http://localhost:8088/reservations customerId=OHM date=20211001  stylingType=perm

- 예약 후 -> 미용실 상태
  http GET http://localhost:8088/hairshops
 
![image](https://user-images.githubusercontent.com/29780972/135105736-704e832c-1c3b-42ff-8e99-0c0be9c1f3c3.png)


- 예약 후 -> 예약 상태
  http GET http://localhost:8088/reservations
 
![image](https://user-images.githubusercontent.com/29780972/135108098-96719162-1f32-4acb-8423-e026fac400ab.png)

- 예약 취소
 http PATCH http://localhost:8088/reservations/2 status=CANCEL_REQUESTED

- 취소 후 - 미용실 상태
  http GET http://localhost:8088/hairshops
 
![image](https://user-images.githubusercontent.com/29780972/135106170-11564325-494b-46ff-95a5-07024ef7d10b.png)

- 취소 후 - 예약 상태
  http GET http://localhost:8088/reservations
  
![image](https://user-images.githubusercontent.com/29780972/135108199-c76e4e23-988f-45b9-a045-27cc592301bc.png)



## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 Reservation 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 현실에서 발생가는한 이벤트에 의하여 마이크로 서비스들이 상호 작용하기 좋은 모델링으로 구현을 하였다.


```
@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerId;
    private String date;
    private String stylingType;
    private String status;
    private String shopstatus;
    private String paystatus;

    @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 미용실 예약 요청 ============== ");
        ReservationPlaced reservationPlaced = new ReservationPlaced();
        BeanUtils.copyProperties(this, reservationPlaced);
        reservationPlaced.publishAfterCommit();



        hairshop.reservation.external.Payment payment = new hairshop.reservation.external.Payment();
        // mappings goes here
        /* 결제(payment) 동기 호출 진행 */
        /* 결제 진행 가능 여부 확인 후 미용실매핑 */

        if(this.getStatus().equals("RSV_REQUESTED")){

            payment.setReservationId(this.getId());
            payment.setStatus("PAYMENT_REQUESTED");
        }
        
        ReservationApplication.applicationContext.getBean(hairshop.reservation.external.PaymentService.class)
            .requestpayment(payment);

    }

    @PrePersist
    public void onPrePersist(){
        System.out.println(" ============== 미용실 예약 요청 전 ============== ");
        status = "RSV_REQUESTED";
        shopstatus = "NULL";
        paystatus = "NULL";

    }

    @PostUpdate
    public void onPostUpdate(){

        System.out.println(" ============== 예약 취소 요청 ============== ");

        if(this.getStatus().equals("CANCEL_REQUESTED") ){
            ReservationCanceled reservationCanceled = new ReservationCanceled();
            BeanUtils.copyProperties(this, reservationCanceled);
            reservationCanceled.publishAfterCommit();
        }

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

    public String getShopStatus() {
        return shopstatus;
    }

    public void setShopStatus(String shopstatus) {
        this.shopstatus = shopstatus;
    }

    
    public String getPayStatus() {
        return paystatus;
    }

    public void setPayStatus(String paystatus) {
        this.paystatus = paystatus;
    }

}
```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

```
package hairshop.reservation;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reservations", path="reservations")
public interface ReservationRepository extends PagingAndSortingRepository<Reservation, Long>{

}
```

- 적용 후 REST API 의 테스트

```
#reservation 서비스의 백신 예약 요청
http POST http://localhost:8088/reservations customerId=OHM date=20211001  stylingType=perm

#reservation 서비스의 백신 취소 요청
http PATCH http://localhost:8088/reservations/2 status=CANCEL_REQUESTED

#reservation 서비스의 백신 예약 상태 및 백신 종류 확인
http GET http://localhost:8088/reservations

#hairshops 서비스의 및 유통기한등 백신 정보 확인
http GET http://localhost:8088/hairshops 
```

## 동기식 호출(Sync) 과 Fallback 처리

분석단계에서의 조건 중 하나로 예약(reservation)->승인(approval) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient로 이용하여 호출하도록 한다.

- 승인 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
#ReservationService.java

@FeignClient(name="payment",  url="${prop.aprv.url}")
public interface PaymentService {
    @RequestMapping(method= RequestMethod.GET, path="/payments")
    public void requestpayment(@RequestBody Payment payment);

}
```

- 예약 요청을 받은 직후(@PostPersist) 결제를 동기(Sync)로 요청하도록 처리
```
   @PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 미용실 예약 요청 ============== ");
        ReservationPlaced reservationPlaced = new ReservationPlaced();
        BeanUtils.copyProperties(this, reservationPlaced);
        reservationPlaced.publishAfterCommit();



        hairshop.reservation.external.Payment payment = new hairshop.reservation.external.Payment();
        // mappings goes here
        /* 결제(payment) 동기 호출 진행 */
        /* 결제 진행 가능 여부 확인 후 미용실매핑 */

        if(this.getStatus().equals("RSV_REQUESTED")){

            payment.setReservationId(this.getId());
            payment.setStatus("PAYMENT_REQUESTED");
        }
        
        ReservationApplication.applicationContext.getBean(hairshop.reservation.external.PaymentService.class)
            .requestpayment(payment);

    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인

```
# 결제 (payment) 서비스를 잠시 내려놓음 (ctrl+c)
```

```
# 예약 요청  - Fail
http POST http://localhost:8088/reservations customerId=OHM date=20211001  stylingType=perm
```
![image](https://user-images.githubusercontent.com/29780972/135109892-a00c0cc8-feb4-4f91-bb27-f9d4127b0aa2.png)

```
# 결제서비스 재기동
```

```
# 예약 요청  - Success

http POST http://localhost:8088/reservations customerid=OHM hospitalid=123 date=20210910
```
![image](https://user-images.githubusercontent.com/29780972/135110131-2d226f8d-57fb-4f31-8e0b-b87b6d48d6fc.png)

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커 처리는 운영단계에서 설명한다.)


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

결제가 이루어진 후에 예약 시스템의 상태가 업데이트 되고, 미용실 시스템의 상태 업데이트가 비동기식으로 호출된다.
- 이를 위하여 결제가 완료되면 결제 완료 되었다는 이벤트를 카프카로 송출한다. (Publish

```
#payment.java

@PostPersist
    public void onPostPersist(){

        System.out.println(" ============== 예약 결제 요청 ============== ");

        PaymentFinished paymentFinished = new PaymentFinished();
        BeanUtils.copyProperties(this, paymentFinished);
        paymentFinished.publishAfterCommit();

    }

```

- 미용실 시스템에서는 승인 완료된 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다

```
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
    
```

그 외 예약 승인/거부는 미용실과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 유지보수로 인해 잠시 내려간 상태 라도 예약을 받는데 문제가 없다.

```
# 미용실 서비스 (hairshop) 를 잠시 내려놓음 (ctrl+c)
```

```
# 예약 요청  - Success
http POST http://localhost:8088/reservations customerId=OHM date=20211001  stylingType=perm
```
![image](https://user-images.githubusercontent.com/29780972/135110965-9de23beb-46f3-47c9-99ed-308fd558bfb1.png)

```
# 예약 상태 확인  - hairshop 서비스와 상관없이 예약 상태는 정상 확인
http GET http://localhost:8088/hairshops
```
![image](https://user-images.githubusercontent.com/29780972/135111126-2c81037c-8da0-4a23-9542-b4ac458ce39e.png)

"status": "RSV_REQUESTED" 에서 끝난것을 확인


## 폴리글랏 퍼시스턴스

viewPage 는 H2가 아닌 RDB 계열의 데이터베이스인 Maria DB 를 사용하기로 하였다. 
기존의 Entity Pattern 과 Repository Pattern 적용과 데이터베이스 관련 설정 (pom.xml, application.yml) 을 변경하였으며, mypage pom.xml에 maria DB 의존성을 추가 하였다.
위 작업을 통해 maria DB를 부착하였으며 아래와 같이 작업 진행됨을 확인할 수 있다.

```
* 의존성 추가
#pom.xml

		<dependency> 
			<groupId>org.mariadb.jdbc</groupId> 
			<artifactId>mariadb-java-client</artifactId> 
		</dependency>


*application.yml 수정
spring:
  profiles: default
  jpa:
    show_sql: true
      #format_sql: true
    generate-ddl: true
    hibernate:
        ddl-auto: create-drop
  datasource:
    url: jdbc:mariadb://localhost:3306/HairshopReservation
    driver-class-name: org.mariadb.jdbc.Driver
    username: ####   (계정정보 숨김처리)
    password: ####   (계정정보 숨김처리)
```

실제 MariaDB 접속하여 확인 시, 데이터 확인 가능 (ex. Reservation에서 객실 예약 요청한 경우)
![image](https://user-images.githubusercontent.com/29780972/135111635-01f266d0-347e-44f5-8c3c-19043ded3a3f.png)


# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하 buildspec.yml 에 포함되었다.

AWS CodeBuild 적용 현황
![image](https://user-images.githubusercontent.com/29780972/135394925-583a19f4-166b-4537-b9f2-a30f6bfee119.png)

webhook을 통한 CI 확인
![image](https://user-images.githubusercontent.com/29780972/135394973-c24ce3c7-6239-47ba-86ee-311503b2ecda.png)

AWS ECR 적용 현황
![image](https://user-images.githubusercontent.com/29780972/135395046-c547ecd0-9c07-4cd6-8520-59eae642ee88.png)


EKS에 배포된 내용
![image](https://user-images.githubusercontent.com/29780972/135395112-73cc077d-5db8-4450-ba95-49e62292445c.png)

AWS 주소로 GET 확인

http GET http://a5f96d3b2b238462fa74fd87f34e4939-61865795.ap-southeast-1.elb.amazonaws.com:8080/hairshops
![image](https://user-images.githubusercontent.com/29780972/135396671-3003cea8-bee9-4ea7-ae54-4f2881269623.png)

http GET http://a5f96d3b2b238462fa74fd87f34e4939-61865795.ap-southeast-1.elb.amazonaws.com:8080/reservations
![image](https://user-images.githubusercontent.com/29780972/135396719-5fa4b451-1a9c-4b2f-8725-a2bd8a98357b.png)

http GET http://a5f96d3b2b238462fa74fd87f34e4939-61865795.ap-southeast-1.elb.amazonaws.com:8080/payments
![image](https://user-images.githubusercontent.com/29780972/135396791-6c39a20e-7d5b-4035-acac-fc55ebc1fff8.png)


## ConfigMap 설정


 동기 호출 URL을 ConfigMap에 등록하여 사용
 
  kubectl apply -f configmap

```
apiVersion: v1
kind: ConfigMap
metadata:
    name: hairshop-configmap
    namespace: hairshop-reservation
data:
    apiurl: "http://user13-gateway:8080"
```

buildspec 수정

```
spec:
                containers:
                  - name: $_PROJECT_NAME
                    image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
                    ports:
                      - containerPort: 8080
                    env:
                    - name: apiurl
                      valueFrom:
                        configMapKeyRef:
                          name: hairshop-configmap
                          key: apiurl 
```

application.yml 수정
```
prop:
  aprv:
    url: ${apiurl}
```

동기 호출 URL 실행
![image](https://user-images.githubusercontent.com/29780972/135396430-af578678-e9da-4204-8e51-3cd0797a2473.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: istio의 Destination Rule을 적용 Traffic 관리함.

시나리오는 예약(reservation)-->결제(payment) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 10명
- 10초 동안 실시

```
siege -c10 -t10s -v http://user04-gateway:8080/payments 

```
![image](https://user-images.githubusercontent.com/29780972/135397669-9cc450f5-c40a-44e7-9b2b-c4fab8b9547c.png)

CB가 없기 때문에 100% 성공

```
kubectl apply -f destinationRule -n hairshop-reservation

apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
    name: dr-payment
    namespace: hairshop-reservation
spec:
    host: user13-payment
    trafficPolicy:
        connectionPool:
            http:
                http1MaxPendingRequests: 1
                maxRequestsPerConnection: 1
```
![image](https://user-images.githubusercontent.com/29780972/135400069-168864fc-8639-4613-a38d-6454143f17dd.png)


istio-injection 활성화 및 room pod container 확인
![image](https://user-images.githubusercontent.com/29780972/135400115-8c78c98c-5cd8-49f0-9456-2fe3fea88316.png)



CB적용 되어 일부 실패 확인



### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.	
```
siege -c100 -t10S -v --content-type "application/json" 'http://a591a265c2dd941c7b527c3737d49de6-753212419.ap-southeast-1.elb.amazonaws.com:8080/reservations'

```

```
# buildspec.yaml 의 readiness probe 의 설정:

                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
```

reservation 서비스 신규 버전으로 배포

![image](https://user-images.githubusercontent.com/29780972/135409768-a18795fc-32b2-4499-85bf-8d11504c2bb2.png)


배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.

## Liveness Probe

테스트를 위해 buildspec.yml을 아래와 같이 수정 후 배포

```
livenessProbe:
                      # httpGet:
                      #   path: /actuator/health
                      #   port: 8080
                      exec:
                        command:
                        - cat
                        - /tmp/healthy
```


 pod 상태 확인
 ![image](https://user-images.githubusercontent.com/29780972/135411059-c1043806-6261-4987-aac0-c8d259121517.png)


 kubectl describe ~ 로 pod에 들어가서 아래 메시지 확인
 ```
 Warning  Unhealthy  26s (x2 over 31s)     kubelet            Liveness probe failed: cat: /tmp/healthy: No such file or directory
 ```
![image](https://user-images.githubusercontent.com/29780972/135410833-c50b0fe8-719d-4998-9e21-5cb3001639d8.png)


/tmp/healthy 파일 생성
```
kubectl exec -it pod/user04-customer-5b7c4b6d7-p95n7 -n hotels -- touch /tmp/healthy
```

성공 확인
![image](https://user-images.githubusercontent.com/29780972/135411113-78740dfe-91bb-4e3c-af6e-302943b8731d.png)
