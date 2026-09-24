# RemiCare — Backend

원거리 가족돌봄자를 위한 AI 기반 노인 응급 케어 시스템의 백엔드 서버입니다.

떨어져 사는 가족이 어르신의 상태를 실시간으로 확인하고, 응급 상황과 복약 일정을 놓치지 않도록 돕습니다.

- **기간**: 2026.03 ~ 2026.06 (심화캡스톤 연계 상상기업)
- **팀 구성**: 6인 (백엔드 2 · 프론트엔드 2 · AI 1 · 인프라 1)

<br>

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.3.6, Spring Data JPA |
| Auth | Spring Security, JWT (jjwt 0.11.5) |
| Realtime | WebSocket + STOMP, RabbitMQ (AMQP), Reactor Netty |
| Notification | Firebase Admin (FCM), nurigo SDK (SMS) |
| DB | MySQL |
| Docs | Springdoc OpenAPI |

<br>

## 개발 환경

| 구분 | 사용 도구 |
| --- | --- |
| Build | Gradle |
| Container | Docker, docker-compose |
| Orchestration | Kubernetes |
| CI/CD | GitHub Actions |
| Code Quality | Spotless (googleJavaFormat), Git pre-commit hook |

<br>

## 주요 기능

- **인증/인가** — JWT 기반 로그인 및 토큰 관리
- **웨어러블 기기 연결 관리** — 기기 연결 상태 등록·조회·갱신
- **복약 일정 관리** — 복약 일정 등록·조회·수정·삭제
- **돌봄 일정 관리** — 일정 등록 및 알림 연동
- **실시간 채팅** — WebSocket + RabbitMQ 메시지 브로커 기반, 다중 서버 환경에서의 메시지 전달 보장
- **다채널 알림** — FCM 푸시 알림 및 SMS 발송

<br>

## 아키텍처

```
src/main/java/com/kgu/life_watch
├── auth           # 인증/인가
├── user           # 사용자 관리
├── schedule       # 일정·복약 관리
├── chat           # 실시간 채팅 (WebSocket + RabbitMQ)
├── notification   # FCM 푸시 · SMS 발송
└── global         # 공통 설정, 예외 처리, 에러코드
```

실시간 채팅은 단일 서버 WebSocket이 아니라 **RabbitMQ를 외부 메시지 브로커로 두는 구조**입니다. 서버가 여러 대로 확장되어도 서로 다른 인스턴스에 연결된 사용자 간 메시지 전달이 보장됩니다.

<br>

## 실행 방법

```bash
# 의존 서비스 기동 (MySQL, RabbitMQ)
docker compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

Kubernetes 배포 매니페스트는 `k8s/` 디렉토리에 있습니다.

```bash
kubectl apply -f k8s/
```

## 📠 Convention

### 🤝 Branch Naming Convention

| 머릿말  | 설명                               |
| ------- | ---------------------------------- |
| main    | 서비스 브랜치                      |
| develop | 배포 전 작업 기준                  |
| feat    | 기능 단위 구현                     |
| hotfix  | 서비스 중 긴급 수정 건에 대한 처리 |
| fix        | 버그 수정 및 에러 해결 작업                      |
| refactor   | 코드 리팩토링 (기능 변경 없이 코드 구조 개선)    |

<details>
<summary>Branch Naming Convention Detail</summary>
<div markdown="1">

```
master(main) ── develop ── feature
└── hotfix                └── fix 
                          └── refactor 
```

- [ ] [깃 플로우](https://techblog.woowahan.com/2553/)를 베이스로 하여 프로젝트 사이즈에 맞게 재정의했습니다.
- [ ] 브랜치 이름은 `kebab-case`를 따릅니다.
- [ ] 이슈 번호는 가장 마지막에 적습니다. (ex. #_)

#### master(main)

- [ ] 실제 서비스가 이루어지는 브랜치입니다.
- [ ] 이 브랜치를 기준으로 develop 브랜치가 분기됩니다.
- [ ] 배포 중, 긴급하게 수정할 건이 생길시 hotfix 브랜치를 만들어 수정합니다.

#### develop

- [ ] 개발, 테스트, 릴리즈 등 배포 전 작업의 기준이 되는 브랜치입니다.
- [ ] 해당 브랜치를 default로 설정합니다.
- [ ] 이 브랜치에서 feature 브랜치가 분기됩니다.

#### feature

- [ ] 개별 개발자가 맡은 작업을 개발하는 브랜치입니다.
- [ ] `feat/(feat-name)` 과 같이 머릿말을 feat, 꼬릿말을 개발하는 기능으로 명명합니다.
- [ ] feat-name의 경우 kebab-case를 따릅니다.
- [ ] ex) feat/social-login-#5

#### fix

- [ ] 버그나 에러를 수정하는 브랜치입니다.
- [ ] `fix/(수정내용)` 형식으로 명명합니다.
- [ ] ex) `fix/login-error-#8`

---

#### refactor

- [ ] 코드 구조를 개선하거나 리팩토링하는 브랜치입니다. (기능 변화 없음)
- [ ] `refactor/(개선내용)` 형식으로 명명합니다.
- [ ] ex) `refactor/remove-duplication-#12`

#### hotfix

- [ ] 서비스 중 긴급히 수정해야 할 사항이 발생할 때 사용합니다.
- [ ] main에서 분기됩니다.

</div>
</details>

### 🤝 Commit Convention

| 머릿말           | 설명                                                                      |
| ---------------- | ------------------------------------------------------------------------- |
| Feat             | 새로운 기능 추가                                                          |
| Fix              | 버그 수정                                                                 |
| Refactor         | 코드 리팩토링                                                  |
| Style         | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우                                                  |
| Comment          | 필요한 주석 추가 및 변경                                                  |
| Docs             | 문서 수정                                                                 |
| Test             | 테스트 코드, 리팩토링 테스트 코드 추가                        |
| Chore            | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
| Rename           | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우                        |
| Remove           | 파일을 삭제하는 작업만 수행한 경우                                        |
| !BREAKING CHANGE | 커다란 API 변경의 경우                                                    |
| !HOTFIX          | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우                     |

<details>
<summary>Commit Convention Detail</summary>
<div markdown="1">

### 1. 제목과 본문을 빈행으로 분리

- 커밋 유형 이후 제목과 본문은 한글로 작성하여 내용이 잘 전달될 수 있도록 할 것
- 본문에는 변경한 내용과 이유 설명 (어떻게보다는 무엇 & 왜를 설명)

### 2. 제목 첫 글자는 대문자로, 끝에는 `.` 금지

### 3. 제목은 영문 기준 50자 이내로 할 것

### 4. 마지막에 이슈번호 추가하기

### 5. 자신의 코드가 직관적으로 바로 파악할 수 있다고 생각하지 말자

### 6. 여러가지 항목이 있다면 글머리 기호를 통해 가독성 높이기

```
- 변경 내용 1
- 변경 내용 2
- 변경 내용 3
```

### 8. 예시
커밋유형: 기능 설명 (#이슈번호)
ex) Feat: 로그인 기능 구현 (#5)

</div>
</details>

<!--
TODO: 아래 항목을 채우면 문서 완성도가 올라갑니다.
- 서비스 소개 이미지 또는 데모 GIF
- 시스템 아키텍처 다이어그램 (웨어러블 → 서버 → 앱 흐름)
- ERD
- API 명세 링크 (Swagger 배포 주소)
- 팀원별 담당 영역 표
-->
