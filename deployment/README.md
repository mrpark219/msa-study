# MSA 환경 Docker 배포

## 1. 네트워크 구성

Docker 컨테이너 간 통신을 위한 사용자 정의 브리지 네트워크를 생성한다.

```shell
docker network create --gateway 172.20.0.1 --subnet 172.20.0.0/16 msa-study-network
```

- `--gateway`: 네트워크의 게이트웨이 IP 지정
- `--subnet`: 네트워크 주소 범위 지정
- `msa-study-network`: 생성할 네트워크 이름

## 2. RabbitMQ 배포

RabbitMQ 컨테이너를 배포하고, 기본 사용자와 비밀번호를 설정한다.

```shell
docker run -d \
  --name rabbitmq \
  --network msa-study-network \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:4.0-management
```

- `-p 5672:5672`: AMQP 프로토콜 포트 매핑
- `-p 15672:15672`: 관리 UI 접근 포트 매핑
- `-e RABBITMQ_DEFAULT_USER/PASS`: 초기 접속 계정 설정
- `rabbitmq:4.0-management`: RabbitMQ 관리 UI가 포함된 이미지 사용

## 3. Configuration Service 배포

```shell
docker run -d \
  -p 8888:8888 \
  --network msa-study-network \
  -e "spring.rabbitmq.host=rabbitmq" \
  --name config-service \
  mrpark219/config-service:1.0
```

- `-p 8888:8888`: 컨테이너의 8888 포트를 호스트의 8888 포트에 매핑
- `-e "spring.rabbitmq.host=rabbitmq"`: Spring 애플리케이션이 연결할 RabbitMQ 호스트 지정
- `mrpark219/config-service:1.0`: 사용할 이미지 이름 및 태그

## 4. Discovery Service 배포

```shell
docker run -d \
  -p 8761:8761 \
  --network msa-study-network \
  --name discovery-service \
  mrpark219/discovery-service:1.0
```

- `-p 8761:8761`: 컨테이너의 8761 포트를 호스트의 8761 포트에 매핑
- `mrpark219/discovery-service:1.0`: 사용할 이미지 이름 및 태그