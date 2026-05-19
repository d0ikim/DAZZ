# MySQL vs MongoDB

**Summary**: 뮤지션-앨범-협업 관계의 참조 무결성이 필수이므로 MySQL 선택. MongoDB는 관계 그래프 정합성을 보장할 수 없다.
**Tags**: #database #architecture-decision #mysql
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 결론

**MySQL 채택. 번복 불가.**

핵심 이유: DAZZ의 컨셉은 **정확한 정보 연결**이다.
잘못된 연결 = 신뢰도 파괴 = 컨셉 붕괴.

---

## 항목별 비교

| 항목 | MongoDB | **MySQL 8.0** | DAZZ 판단 |
| --- | --- | --- | --- |
| 참조 무결성 | 애플리케이션 책임 | **FK + ACID 보장** | **MySQL** — 컨셉의 본질 |
| 관계 표현 | 임베딩 또는 수동 JOIN | **JOIN + FK** | **MySQL** |
| 스키마 유연성 | 높음 | 낮음 | MongoDB 우위 — 불필요 |
| 트랜잭션 | 제한적 (4.0+) | **완전한 ACID** | **MySQL** |
| 복합 인덱스 | 가능 | **강력 (Composite, Fulltext)** | **MySQL** |
| 운영 비용 | 중간 | **AWS RDS/Aurora 안정적** | **MySQL** |
| 그래프 탐색 | 불리 | 불리 (Redis 캐시로 보완) | 동등 (캐싱으로 해결) |

---

## MongoDB를 탈락시킨 핵심 이유

### 1. 관계 정합성 보장 불가

```
뮤지션 A ─협업─ 뮤지션 B ─협업─ 뮤지션 C

MongoDB: B가 삭제됐을 때 A-B, B-C 협업 데이터 고아 발생 가능
MySQL:   FK + ON DELETE 제약으로 원천 차단
```

DAZZ의 관계도는 고아 데이터가 단 하나라도 있으면 그래프가 깨진다.

### 2. 복잡한 JOIN 쿼리

Sideman 이력 조회:
```sql
SELECT m.stage_name, ap.participation_type, a.title, a.release_date
FROM MUSICIAN m
JOIN ALBUM_PARTICIPATION ap ON m.id = ap.musician_id
JOIN ALBUM a ON ap.album_id = a.id
WHERE m.id = ?
  AND ap.participation_type = 'SIDEMAN'
ORDER BY a.release_date DESC
```

이런 다중 JOIN이 MongoDB에서는 `$lookup` 파이프라인으로 복잡해지고 성능도 떨어짐.

---

## Neo4j는 왜 검토했고 왜 탈락했나

그래프 DB인 Neo4j는 관계도 탐색에 최적화되어 있어 매력적.

**탈락 이유**:
- 1인 프로젝트 운영 부담 (별도 DB 서버 + 학습 곡선)
- 트랜잭션 본체(협업 등록)는 RDBMS가 더 안전
- 그래프 탐색 성능은 **Redis 캐싱**으로 MySQL도 충분히 보완 가능

---

## MySQL 8.0 활용 포인트

- **Window Function**: 뮤지션별 협업 순위, 기간별 통계
- **Fulltext Index**: 뮤지션 이름 한글 검색 (P1)
- **JSON 컬럼**: `DOCENT_NOTE.style_tags`, `VENUE.image_urls`
- **Composite Index**: `(musician_id, participation_type)` 으로 Sideman 이력 고속 조회

---

## 관련 페이지

- [[comparisons/kafka-vs-rabbitmq]]
- [[entities/musician]]
- [[entities/collaboration]]
