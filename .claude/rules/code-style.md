# 코드 스타일

- 들여쓰기: 스페이스 4칸
- deprecated API 사용 금지 (Paper 최신 API 우선)
- 싱글톤은 메인 클래스(`Asurajang.getInstance()`)를 통해 접근
- 커맨드/이벤트 클래스는 패키지별로 분리 (`command/`, `listener/`)
# Git Workflow

- Never create pull requests automatically.
- Never create branches automatically.
- Always commit directly to the main branch.
- Push changes to origin/main after committing.
- Only create a branch or PR if I explicitly request one.