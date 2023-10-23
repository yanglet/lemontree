# lemontree

### 바로가기
- [실행 방법](#실행-방법)
- [기술 스택](#기술-스택)
- [문제 및 해결책 / 고려사항](#문제-및-해결책--고려사항)
- [API 명세](#api-명세)
- [DB Schema](#db-schema)

### 실행 방법
```
# git clone https://github.com/yanglet/lemontree.git
# cd lemontree
# docker-compose up -d (redis 띄우기)
# ./gradlew clean bootjar
# java -jar ./build/libs/lemontree-0.0.1-SNAPSHOT.jar
```

### 기술 스택
```
Kotlin (Jdk 17), Spring Data JPA, Spring Boot 3.1.x
h2 (Mysql 8.0), Redis
Kotest 5.5.5 
```

### 문제 및 해결책 / 고려사항
```dockerfile
1. Cache stampede (TTL 만료시 요청이 몰려 부하가 발생하는 이슈)
    -> 1) 특정 유저에 따른 송금 목록 조회이므로 비즈니스상 많은 부하가 몰릴 가능성이 적다고 판단하여 고려하지 않았다
       2) 만일 고려해야한다면 TTL 전에 다시 캐싱해주는 알고리즘, 키 전략을 좀 더 상세하게 나누는 것,
        요청 중 맨 나중 것만 디비를 찌르게 하는 것 등이 있을 것 같다

2. 송금 비즈니스 로직에 Lockname 을 to (송금 받는 회원 번호)로 잡았는데, (여러 사람이 한 사람에게 송금하는 것만 동시성 이슈가 발생할 것 같다 생각함)
    만약 어떤 이슈로 인해 한 사람이 여러 사람에게 동시에 송금하는 요청이 들어오면 동시성 이슈가 생길 수 있다.
    -> 운영 이슈로 받아들이고 수작업으로 처리해줘야할 것 같다.

3. 상식적으로 생각해봤을 때 한 회원에 대한 머니 충전이 동시에 들어올 수 없다고 생각할 수 있지만,
    돈과 관련된 이슈 (민감하고, 틀어지면 안되는) 이기 때문에 동시성을 고려하여 구현하였다.

4. 트래픽이 높지 않고 캐시 히트율이 높은 비즈니스가 많이 없어서 인프라가(ex: Redis) 구성되어 있지 않았다면
    분산 캐시가 아닌 로컬 캐시를 고려하고, redisson 분산락 대신 DB의 비관적락을 적용하면서 인프라 비용을 절감시킬 것 같다.
    -> 물론 DB의 락을 건다면 성능적인 측면에서 조금 떨어질 순 있겠다.
    -> 로컬 캐시 또한 서버가 여러대라면 운이 좋아야(?) 히트가 되므로 성능적으로 떨어질 수 있겠다.
    
5. 내장 레디스를 적용해 편리하게 실행하도록 하고 싶었으나, CPU(apple M2) 이슈로 지원하지않아
    docker 로 띄우기로 했다. (해결방법이 있는 것 같기는 하나 시간이 많이 걸릴 것 같았다)
    
6. 돈과 관련된 민감한 비즈니스이므로 테스트 코드를 작성하여 신뢰성을 높이고자 했다.
```
![image](https://github.com/yanglet/lemontree/assets/96788792/17f41544-4ea4-4e14-8b00-9f42fb3ea819)

### API 명세
```shell
머니 충전 API
[POST] /v1/wallets/{memberNo}/deposit

Request
----------------------------------------------------------
{
  "amount": 1000 (충전할 금액, Long 타입)
}
----------------------------------------------------------


Response 

1. 200 OK
{} 응답 없음

2. 400 BAD_REQUEST
----------------------------------------------------------
{
  "message": "찾을 수 없는 지갑입니다." or "찾을 수 없는 회원입니다."
}
----------------------------------------------------------

3. 500 INTERNAL_SERVER_ERROR
----------------------------------------------------------
{
  "message": "에러 메세지"
}
----------------------------------------------------------

```

```shell
송금 API
[POST] /v1/remittances/save

Request
----------------------------------------------------------
{
  "from": 1038, (송금 하는 회원 번호, Long 타입)
  "to": 1504, (송금 받는 회원 번호, Long 타입)
  "amount": 100000 (송금할 금액, Long 타입)
}
----------------------------------------------------------


Response

1. 200 OK
{} 응답 없음

2. 400 BAD_REQUEST
----------------------------------------------------------
{
  "message": "찾을 수 없는 지갑입니다." or "찾을 수 없는 회원입니다."
}
----------------------------------------------------------

3. 500 INTERNAL_SERVER_ERROR
----------------------------------------------------------
{
  "message": "에러 메세지"
}
----------------------------------------------------------
```

```shell
특정 유저의 송금 목록 조회 API
[GET] /v1/remittances/{memberNo}

Request
Body 없음


Response

1. 200 OK
{
  "remittanceNo": 180, (송금 번호, Long 타입)
  "to": 1504, (송금 받는 회원 번호, Long 타입)
  "from": 1038, (송금 하는 회원 번호, Long 타입)
  "toBalance": 350000, (송금 후 송금받은 회원의 보유 금액, Long 타입)
  "fromBalance": 150000, (송금 후 송금한 회원의 보유 금액, Long 타입)
  "amount": 100000, (송금 금액, Long 타입)
  "status": 1000, (송금 상태, String 타입)
  "reason": 1000, (송금 상태에 따른 사유, String 타입)
  "insertDate": LocalDateTime, (송금일시)
  "updateDate": LocalDateTime (송금 데이터 수정일시)
}

2. 400 BAD_REQUEST
----------------------------------------------------------
{
  "message": "찾을 수 없는 회원입니다."
}
----------------------------------------------------------

3. 500 INTERNAL_SERVER_ERROR
----------------------------------------------------------
{
  "message": "에러 메세지"
}
----------------------------------------------------------
```

### DB Schema
```sql
CREATE TABLE `lemontree`.`MEMBER`
(
    `member_no`       INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '회원 번호',
    `status`          VARCHAR(10) NOT NULL COMMENT '회원 상태 (탈퇴, 활동)',
    `insert_date`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '등록일자',
    `insert_operator` VARCHAR(50) NOT NULL COMMENT '등록자',
    `update_date`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '수정일자',
    `update_operator` VARCHAR(50) NOT NULL COMMENT '수정자',
    PRIMARY KEY (`member_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_general_ci' COMMENT='회원';

CREATE TABLE `lemontree`.`WALLET`
(
    `wallet_no`       INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '지갑 번호',
    `member_no`       INT UNSIGNED NOT NULL COMMENT '회원 번호',
    `balance`         INT UNSIGNED NOT NULL COMMENT '보유 금액',
    `maximum_balance` INT UNSIGNED NOT NULL COMMENT '보유 금액 최대 한도',
    `insert_date`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '등록일자',
    `insert_operator` VARCHAR(50) NOT NULL COMMENT '등록자',
    `update_date`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '수정일자',
    `update_operator` VARCHAR(50) NOT NULL COMMENT '수정자',
    PRIMARY KEY (`wallet_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_general_ci' COMMENT='지갑';

CREATE TABLE `lemontree`.`REMITTANCE`
(
    `remittance_no`   INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '송금 번호',
    `to_member_no`    INT UNSIGNED NOT NULL COMMENT '송금 받는 회원 번호',
    `from_member_no`  INT UNSIGNED NOT NULL COMMENT '송금 하는 회원 번호',
    `to_balance`      INT UNSIGNED NOT NULL COMMENT '송금 받는 회원의 보유 금액 (거래 후)',
    `from_balance`    INT UNSIGNED NOT NULL COMMENT '송금 하는 회원의 보유 금액 (거래 후)',
    `amount`          INT UNSIGNED NOT NULL COMMENT '송금 금액',
    `status`          VARCHAR(10) NOT NULL COMMENT '송금 상태 (실패, 성공)',
    `reason`          VARCHAR(255) COMMENT '송금 상태에 따른 사유',
    `insert_date`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '등록일자',
    `insert_operator` VARCHAR(50) NOT NULL COMMENT '등록자',
    `update_date`     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '수정일자',
    `update_operator` VARCHAR(50) NOT NULL COMMENT '수정자',
    PRIMARY KEY (`remittance_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 DEFAULT COLLATE='utf8mb4_general_ci' COMMENT='송금';
```
