# 아수라장 (mayhem)

Paper 기반 마인크래프트 PvP 미니게임 플러그인입니다.  
랜덤 바이옴 전장에서 팀전 또는 개인전으로 싸웁니다.

---

## 요구 사항

| 항목 | 버전 |
|------|------|
| Minecraft | 26.1.2 |
| Paper | 26.1.2+ |
| Java | 25+ |

## 게임 흐름

1. `/mayhem start` → GUI에서 **팀전 / 개인전** 선택, **기지 모드** 켜고 끄기
2. 랜덤 바이옴 전장 추첨 후 카운트다운
3. 좁은 전장에서 30분간 전투 (시간은 항상 아침)
4. 시간 종료 또는 `/mayhem stop` 시 게임 끝

---

## 명령어

| 명령어 | 권한 | 설명 |
|--------|------|------|
| `/mayhem start` | `asurajang.admin` | 게임 모드 선택 GUI 열기 |
| `/mayhem stop` | `asurajang.admin` | 게임 강제 종료 |
| `/mayhem reload` | `asurajang.admin` | 설정 리로드 |
| `/mayhem list` | `asurajang.admin` | 전체 증강 목록 보기 |
| `/mayhem list prism` | `asurajang.admin` | 프리즘 증강 목록 보기 |
| `/mayhem status` | 없음 | 내 증강 확인 |

별칭: `/아수라장`, `/증바람`

---

## 게임 모드

### 팀전
- 무작위로 **레드팀 / 블루팀**으로 나뉘어 서로 반대편에서 시작

### 개인전
- 모두가 적, 전장 곳곳에 흩어져 시작

### 기지 모드 (옵션)
- 양 팀 진영에 거점이 생기고, 거점을 지키는 가디언을 쓰러뜨리면 거점이 파괴됨
- 가디언은 여러 번의 목숨을 가지고 있어 한 번 쓰러뜨려도 시간이 지나면 더 강해져서 부활함
- 같은 편끼리는 서로 피해를 주지 않으며, 죽으면 우리 팀 거점에서 부활

### 거점 공격 (옵션, 기지 모드 전용):
- 가디언이 범위 안에 들어온 상대팀을 유도형 투사체로 직접 공격하며, 라이프가 회복될수록 공격 속도가 빨라짐

---

## 보상

### 골드

| 상황 | 골드 |
|------|------|
| 일반 킬 | +50 |
| 퍼스트 블러드 | +75 (+25 보너스) |

- 어시스트한 플레이어도 처치 보상을 함께 나눠 받음

### 연속킬 (10초 이내 추가 킬)

| 연속 | 레이블 | 추가 골드 |
|------|--------|-----------|
| 2연 | 더블 킬 | +5 |
| 3연 | 트리플 킬 | +10 |
| 4연 | 쿼드라 킬 | +15 |
| 5연 | 펜타 킬 | +20 |
| 6연+ | 전설적인 킬 | +25 |

### 경험치
- 적을 처치하면 경험치도 함께 얻으며, 쌓이면 레벨업

---

## 증강
- 전투 중 무작위로 얻을 수 있는 여러 종의 특수 능력
- `/mayhem list`로 전체 목록, `/mayhem status`로 내가 가진 증강을 확인
- 프리즘 증강은 더 희귀한 별도 풀에서 등장하며, `/mayhem list prism`으로 확인 가능

---

## 사망
- 아이템·경험치 드롭 없음, 인벤토리 유지
- 사망 위치에서 10초 관전 후 자동 리스폰
- 보유 골드의 10%를 잃음

---

<details>
<summary>전체 증강 목록 (26종)</summary>

<table>
<thead>
<tr><th></th><th>이름</th><th>설명</th></tr>
</thead>
<tbody>
<tr><td align="center"><img src="docs/icons/feather.png" width="64"></td><td>가벼운 착지</td><td>낙하 피해를 받지 않습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/nether_star.png" width="64"></td><td>강강약약</td><td>가장 킬 수가 높은 적에게 1.5배의 피해를 줍니다.</td></tr>
<tr><td align="center"><img src="docs/icons/wither_rose.png" width="64"></td><td>강약약강</td><td>상대에게 (50 - 상대의 체력 비율)% 만큼의 추가 피해를 줍니다</td></tr>
<tr><td align="center"><img src="docs/icons/pufferfish.png" width="64"></td><td>개복치</td><td>최대체력이 절반 감소합니다. 적을 때릴 때마다 10% 확률로 능력을 이전 시킵니다.</td></tr>
<tr><td align="center"><img src="docs/icons/nautilus_shell.png" width="64"></td><td>경정권</td><td>공격 시 80%의 피해를 준 뒤 0.5초 후 40%의 피해와 2배의 넉백을 추가로 줍니다.</td></tr>
<tr><td align="center"><img src="docs/icons/string.png" width="64"></td><td>그랩</td><td>원거리 공격이 적에게 명중하면 그 적을 자신의 위치로 끌어옵니다.</td></tr>
<tr><td align="center"><img src="docs/icons/snowball.png" width="64"></td><td>냉혈한</td><td>적을 우클릭하면 해당 대상에게 구속과 나약함을 3초간 부여합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/fermented_spider_eye.png" width="64"></td><td>데드풀</td><td>최대 체력이 5칸으로 고정되며 1초마다 즉시 치유 I를 받습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/gold_nugget.png" width="64"></td><td>도박사</td><td>매 틱마다 1~239 사이의 무작위 숫자를 액션바에 표시합니다. 77이 나오면 체력을 전부 회복합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/ender_eye.png" width="64"></td><td>매혹</td><td>공격한 상대에게 3초간 이동 속도를 50% 감소시키고 자신을 계속 바라보게 만듭니다.</td></tr>
<tr><td align="center"><img src="docs/icons/magenta_dye.png" width="64"></td><td>모방</td><td>적을 죽일 시 적의 증강 중 하나를 복사합니다. 3회 복사 후 이 증강이 사라집니다.</td></tr>
<tr><td align="center"><img src="docs/icons/armor_stand.png" width="64"></td><td>변태</td><td>적을 때릴 때마다 3% 확률로 갑옷 부위를 벗깁니다.</td></tr>
<tr><td align="center"><img src="docs/icons/shears.png" width="64"></td><td>부기우기</td><td>양손 맞바꾸기 키로 20블록 내의 가장 가까운 적과 위치를 교환합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/bedrock_iso.png" width="64"></td><td>불괴</td><td>아이템의 내구도가 소모될 때 75% 확률로 소모되지 않습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/totem_of_undying.png" width="64"></td><td>불멸</td><td>사망 시 35%의 확률로 부활합니다. 부활 시 전체 체력의 절반인 상태로 부활하며 죽은 위치의 10칸 이내에 랜덤한 좌표에서 부활합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/wind_charge.png" width="64"></td><td>붕뜨네</td><td>검 우클릭 시 바라보는 방향으로 돌풍구를 날립니다.</td></tr>
<tr><td align="center"><img src="docs/icons/creeper_spawn_egg.png" width="64"></td><td>신체 폭탄</td><td>버리기 키를 누르면 체력 3칸을 소모하고 TNT를 던집니다.</td></tr>
<tr><td align="center"><img src="docs/icons/spawn_egg.png" width="64"></td><td>옥견</td><td>두 마리의 길들인 늑대가 당신을 도와 싸웁니다. 늑대는 사망 시 60초 후에 부활합니다. 당신이 사망하면 늑대도 같이 사라지며 같이 부활합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/iron_axe.png" width="64"></td><td>육중한 힘</td><td>공격력이 20% 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/tnt_iso.png" width="64"></td><td>자폭</td><td>35초마다 머리 위에 TNT가 부착됩니다. 부착 시 5초 간 신속을 받은 뒤 폭발하며 반경 8칸 내 적에게 최대 체력의 20%로 고정 피해를 줍니다. 자신은 그 피해의 50%만 받습니다. 폭탄 보유 중 사망하면 즉시 폭발합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/bow.png" width="64"></td><td>저격 대결</td><td>12칸 이상 거리의 적을 공격 시 피해가 50% 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/redstone.png" width="64"></td><td>죄책감의 쾌락</td><td>적 플레이어를 처치 시 최대 체력의 20%를 회복합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/blaze_powder.png" width="64"></td><td>주님의 사랑</td><td>검 우클릭 시 바라보는 방향으로 화염구를 3회 연달아 날립니다.</td></tr>
<tr><td align="center"><img src="docs/icons/sugar.png" width="64"></td><td>클린업</td><td>체력 30% 이하인 적이 10칸 내에 있으면 이동 속도가 50% 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/cooked_porkchop.png" width="64"></td><td>포화지방산</td><td>배고픔이 소모되지 않습니다. 음식을 먹을 때마다 즉시 치유 효과를 받습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/obsidian_iso.png" width="64"></td><td>흑섬</td><td>공격마다 3% 확률로 2.5배의 피해를 줍니다. 발동 후 3번의 공격 동안 확률이 증가합니다.</td></tr>
</tbody>
</table>

</details>

<details>
<summary>프리즘 증강 목록 (5종)</summary>

<table>
<thead>
<tr><th></th><th>이름</th><th>설명</th></tr>
</thead>
<tbody>
<tr><td align="center"><img src="docs/icons/smithing_table_iso.png" width="64"></td><td>능력치 더하기 더하기 더하기</td><td>능력치 모루 4개를 획득합니다!</td></tr>
<tr><td align="center"><img src="docs/icons/rabbit_foot.png" width="64"></td><td>드롭킥</td><td>근접 공격 시 체력 15% 이하인 적을 즉사시킵니다. 즉사한 적에게 강한 넉백과 폭발이 발생하며, 자신의 체력을 최대 체력의 50% 회복합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/glass_iso.png" width="64"></td><td>유리 대포</td><td>최대 체력이 30% 감소합니다. 공격 시 15%의 추가 고정 피해를 입힙니다.</td></tr>
<tr><td align="center"><img src="docs/icons/golden_sword.png" width="64"></td><td>처형인의 검</td><td>내구도가 1이지만 높은 날카로움 수치를 가진 금 검을 즉시 지급합니다. 검이 깨지면 자동으로 다시 지급됩니다.</td></tr>
<tr><td align="center"><img src="docs/icons/jukebox_iso.png" width="64"></td><td>탭 댄서</td><td>기본 공격 시 무한히 중첩되는 0.02의 이동 속도를 얻습니다. 마지막 중첩을 쌓은 후 5초 이내로 중첩을 쌓지 않으면 중첩이 빠르게 감소합니다.</td></tr>
</tbody>
</table>

</details>
