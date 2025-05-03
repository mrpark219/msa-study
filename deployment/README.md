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

## 5. Api Gateway Service 배포

```shell
docker run -d \
 -p 8000:8000 \
 --network msa-study-network \
 -e "spring.cloud.config.uri=http://config-service:8888" \
 -e "spring.rabbitmq.host=rabbitmq" \
 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
 --name api-gateway-service \
 mrpark219/api-gateway-service:1.0
```

- `-p 8000:8000`: 컨테이너의 8000번 포트를 호스트의 8000번 포트로 매핑
- `-e "spring.cloud.config.uri=http://config-service:8888"`: 설정 서버(Config Server) URI를 환경변수로 설정
- `-e "spring.rabbitmq.host=rabbitmq"`: Spring 애플리케이션이 연결할 RabbitMQ 호스트 지정
- `-e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/"`: Eureka 서버의 서비스 등록 주소를 환경변수로 설정
- `mrpark219/api-gateway-service:1.0`: 사용할 이미지 이름 및 태그

## 6. MariaDB 배포(kafka-connect에 포함되어 있으므로 생략 가능)

```shell
docker run -d \
  --name msa-mariadb \
  --network msa-study-network \
  -e MYSQL_DATABASE=mydb \
  -e MYSQL_ROOT_PASSWORD=1234 \
  -p 3306:3306 \
  -v "./data/mariadb:/var/lib/mysql" \
  mariadb
```

- `-e MYSQL_DATABASE=mydb`: 생성할 기본 데이터베이스 이름을 `mydb`로 설정
- `-e MYSQL_ROOT_PASSWORD=1234`: 루트 사용자 비밀번호를 `1234`로 설정
- `-p 3306:3306`: MariaDB의 기본 포트(3306)를 호스트와 매핑
- `-v "./data/mariadb:/var/lib/mysql"`: 호스트의 `./data/mariadb` 디렉토리를 컨테이너의 데이터 디렉토리로 마운트하여 데이터 영속성 확보
- `mariadb`: 사용할 이미지 이름

## 7. Kafka 배포

```shell
docker compose -f ../kafka-connect/docker-compose.yml up -d
```

- `-f ../kafka-connect/docker-compose.yml`: 사용할 Docker Compose 파일 경로를 지정
- `up`: Compose 파일에 정의된 모든 서비스를 시작
- `-d`: 백그라운드(detached) 모드로 실행하여 터미널을 점유하지 않음

## 8. Zipkin 배포

```shell
docker run -d \
  --name zipkin \
  --network msa-study-network \
  -p 9411:9411 \
  openzipkin/zipkin
```

## 9. Prometheus 배포

```shell
docker run -d \
  --name prometheus \
  --network msa-study-network \
  -p 9090:9090 \
  -v ./prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

- `-p 9090:9090`: Prometheus 웹 UI 접근을 위한 포트를 호스트에 노출
- `-v ./prometheus.yml:/etc/prometheus/prometheus.yml`: 호스트의 설정 파일을 컨테이너 내 Prometheus 설정 경로에 마운트
- `prom/prometheus`: Prometheus 공식 Docker 이미지

## 10. Grafana 배포

```shell
docker run -d \
  --name grafana \
  --network msa-study-network \
  -p 3000:3000 \
  grafana/grafana
```

- `-p 3000:3000`: Grafana 웹 UI 접근을 위한 포트를 호스트에 노출
- `grafana/grafana`: Grafana의 공식 Docker 이미지  