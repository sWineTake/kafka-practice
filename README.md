# SpringBoot 멀티모듈

## 프로젝트 구조
```
super-module(root)
├── adapter : 각종 외부 프로그램 연동 (DB, REDIS, KAFKA, ...)
│   ├── kafka : 카프카 어댑터 설정만 구현 - 토픽은 각자 사용하는곳에서 설정 
│   ├── mysql : mysql DB 연동 및 엔티티 선언 
├── api : 컨트롤러 / 비즈니스 로직 구현
│   ├── producer-api : 프로듀서 API 
├── usecase : 어뎁터 연결 창로, API 모듈 연결
│   ├── producer-usecase : 메시지 프로듀서 코드
└── worker : 비동기 작업 처리 서버(배치, 이벤트서버)
    └── auto-inspection-worker : 카프카 컨슈머 처리
```
