# SpringBoot 멀티모듈

## 프로젝트 구조
```
super-module(root)
├── adapter : 각종 외부 프로그램 연동 (DB, REDIS, KAFKA, ...)
│   ├── kafka : 카프카 어댑터 설정만 구현 - 토픽은 각자 사용하는곳에서 설정 
├── api : 컨트롤러 / 비즈니스 로직 구현
│   ├── api-1-server : API 1번 서버
│   ├── api-2-server : API 2번 서버
├── usecase : 어뎁터 연결 창로, API 모듈 연결
│   ├── sample-usecase : Adapter 통합 연결 창로 (프로듀서 처리)
└── worker : 비동기 작업 처리 서버(배치, 이벤트서버)
    └── auto-inspection-worker : 이벤트서버 ex) 카프카 컨슈머 처리
```
