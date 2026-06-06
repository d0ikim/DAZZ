---
name: feedback-mysql-port
description: Docker MySQL 로컬 접근 포트는 3306 — 3307로 기록되어 있던 내용을 사용자가 직접 정정
metadata:
  type: feedback
---

Docker Compose의 MySQL 컨테이너 로컬 접근 포트는 **3306**이다. 3307이 아님.

**Why:** 사용자가 3306으로 변경 후 정상 동작을 확인했으며, 3307로 기록된 배경(로컬 MySQL 충돌 방지)은 실제 환경과 맞지 않는다.

**How to apply:** 향후 인프라 포트 관련 내용 작성 시 MySQL은 3306으로 기재. [[concepts/hexagonal-architecture]]의 로컬 개발 환경 포트 테이블 참조.
