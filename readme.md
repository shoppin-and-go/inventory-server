![ci](https://github.com/shoppin-and-go/inventory-server/actions/workflows/integration.yml/badge.svg?branch=main)
![cd](https://github.com/shoppin-and-go/inventory-server/actions/workflows/deployment.yml/badge.svg?branch=main)

# inventory-server
카트에 있는 QR코드를 스캔하여 앱과 연동하고, 카트의 상품을 실시간으로 앱을 통해 확인할 수 있는 서비스입니다.

이 레포지토리에서는 아래의 기능들을 제공합니다.
- 카트와 앱을 연동하는 API
- 카트에 CNN으로 인식한 상품을 추가/제거하는 API
- 카트에 상품이 추가/제거 되었을 때 앱에 소켓을 통해 이벤트 발행
- 카트에 있는 상품을 조회하는 API

## How to run
Amazon ECR의 이미지를 사용하여 서버를 실행할 수 있습니다

**Docker**
```bash
docker run -d \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8 \
  -e SPRING_DATASOURCE_USERNAME=<DB_USERNAME> \
  -e SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD> \
  -e SERVER_PORT=8080 \
  -p 8080:8080 \
  public.ecr.aws/e6u1y0g6/shoppin-and-go/inventory-server:latest
```

**Docker compose**
```yaml
services:
  inventory-server:
    image: public.ecr.aws/e6u1y0g6/shoppin-and-go/inventory-server:latest
    container_name: inventory-server
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SERVER_PORT: 8080
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
      SPRING_DATASOURCE_USERNAME: inventory_server_application
      SPRING_DATASOURCE_PASSWORD: securepassword
    depends_on:
      db:
        condition: service_healthy
    ports:
      - "8080:8080"

  db:
    image: mysql:8.0.39
    container_name: db
    environment:
      MYSQL_DATABASE: inventory_db
      MYSQL_USER: inventory_server_application
      MYSQL_PASSWORD: securepassword
      MYSQL_ROOT_PASSWORD: rootpassword
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin" ,"ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
```

### Environment Variables
| Name                       | Description    | Default Value | Required |
|----------------------------|----------------|:-------------:|:--------:|
| SPRING_DATASOURCE_URL      | MySQL URL      |       -       |    O     |  
| SPRING_DATASOURCE_USERNAME | MySQL username |       -       |    O     | 
| SPRING_DATASOURCE_PASSWORD | MySQL password |       -       |    O     |
| SPRING_PROFILES_ACTIVE     | profile        |      dev      |    X     |
| SERVER_PORT                | Server port    |     8080      |    X     |
