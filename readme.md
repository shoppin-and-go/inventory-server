![example workflow](https://github.com/shoppin-and-go/inventory-server/actions/workflows/ci.yml/badge.svg?branch=main)

# inventory-server
카트에 있는 QR코드를 스캔하여 앱과 연동하고, 카트의 상품을 실시간으로 앱을 통해 확인할 수 있는 서비스입니다.

이 레포지토리에서는 아래의 기능들을 제공합니다.
- 카트와 앱을 연동하는 API
- 카트에 CNN으로 인식한 상품을 추가/제거하는 API
- 카트에 상품이 추가/제거 되었을 때 앱에 소켓을 통해 이벤트 발행
- 카트에 있는 상품을 조회하는 API

## Schedule
- [x] 4주차(09/23 ~ 09/29): 아키텍쳐 설계 및 DB 설계
- [x] 5주차(09/30 ~ 10/06): 카트 ID와 상품 ID를 전달받아 앱과 카트를 연동하는 API 구현
- [x] 6주차(10/07 ~ 10/13): 카트에 상품을 추가/제거할 수 있는 API 구현
- [x] 7주차(10/14 ~ 10/20): 앱과의 소켓 연결 구현
- [ ] 8주차(10/21 ~ 10/27): 카트의 인벤토리를 조회할 수 있는 API 개발
- [x] 9주차(10/28 ~ 11/03): 인벤토리에 상품이 추가되었을 때 앱에 소켓으로 이벤트를 보내는 로직 개발
- [x] 10주차(11/04 ~ 11/10): AI 모델 통합 및 테스트
- [ ] 11주차(11/11 ~): 통합 테스트 및 배포


