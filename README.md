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
<tr><th width="17%"></th><th width="33%">이름</th><th width="50%">설명</th></tr>
</thead>
<tbody>
<tr><td align="center"><img src="docs/icons/feather.png" width="64"></td><td>가벼운 착지</td><td><b>낙하 피해</b>를 받지 않습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/nether_star.png" width="64"></td><td>강강약약</td><td>가장 킬 수가 높은 적에게<br><span style="color:#CC9900">1.5배</span>의 피해를 줍니다.</td></tr>
<tr><td align="center"><img src="docs/icons/wither_rose.png" width="64"></td><td>강약약강</td><td>상대의 체력이 낮을수록<br><span style="color:#D32F2F">추가 피해</span>가 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/pufferfish.png" width="64"></td><td>개복치</td><td>최대 체력이 <span style="color:#008B8B">절반</span> 감소합니다.<br>적을 때릴 때마다 <span style="color:#008B8B">10% 확률</span>로<br>능력을 이전시킵니다.</td></tr>
<tr><td align="center"><img src="docs/icons/nautilus_shell.png" width="64"></td><td>경정권</td><td>공격 시 <span style="color:#008B8B">80%</span>의 피해를 준 뒤<br>0.5초 후 <span style="color:#008B8B">40%</span>의 피해와<br><span style="color:#008B8B">2배</span>의 넉백을 추가로 줍니다.</td></tr>
<tr><td align="center"><img src="docs/icons/string.png" width="64"></td><td>그랩</td><td>원거리 공격이 적에게 명중하면<br>그 적을 <span style="color:#888888">자신의 위치</span>로 끌어옵니다.</td></tr>
<tr><td align="center"><img src="docs/icons/snowball.png" width="64"></td><td>냉혈한</td><td>적을 우클릭하면 대상에게<br><span style="color:#008B8B">구속</span>과 <span style="color:#008B8B">나약함</span>을 3초간 부여합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/fermented_spider_eye.png" width="64"></td><td>데드풀</td><td>최대 체력이 <span style="color:#D32F2F">5칸</span>으로 고정되며<br>1초마다 <span style="color:#D32F2F">즉시 치유 I</span>를 받습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/gold_nugget.png" width="64"></td><td>도박사</td><td>매 틱마다 <span style="color:#CC9900">1~239</span> 사이의<br>무작위 숫자를 액션바에 표시합니다.<br><span style="color:#CC9900">77</span>이 나오면 체력을 전부 회복합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/ender_eye.png" width="64"></td><td>매혹</td><td>공격한 상대의 이동 속도를<br><span style="color:#D81B60">3초</span>간 <span style="color:#D81B60">50%</span> 감소시키고<br>자신을 계속 바라보게 만듭니다.</td></tr>
<tr><td align="center"><img src="docs/icons/magenta_dye.png" width="64"></td><td>모방</td><td>적을 죽일 시 적의 증강 중<br>하나를 <span style="color:#D81B60">복사</span>합니다.<br><span style="color:#D81B60">3회</span> 복사 후 이 증강이 사라집니다.</td></tr>
<tr><td align="center"><img src="docs/icons/armor_stand.png" width="64"></td><td>변태</td><td>적을 때릴 때마다 <span style="color:#D4AC0D">3% 확률</span>로<br>갑옷 부위를 벗깁니다.</td></tr>
<tr><td align="center"><img src="docs/icons/shears.png" width="64"></td><td>부기우기</td><td>양손 맞바꾸기 키로 <span style="color:#D81B60">20블록</span> 내의<br>가장 가까운 적과 <span style="color:#D81B60">위치를 교환</span>합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/bedrock_iso.png" width="64"></td><td>불괴</td><td>아이템 내구도 소모 시<br><span style="color:#555555">75% 확률</span>로 소모되지 않습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/totem_of_undying.png" width="64"></td><td>불멸</td><td>사망 시 <span style="color:#CC9900">35% 확률</span>로 부활합니다.<br>부활 시 체력의 <span style="color:#CC9900">절반</span> 상태로 부활하며<br>죽은 위치 <span style="color:#CC9900">10칸</span> 이내<br>랜덤한 좌표에서 부활합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/wind_charge.png" width="64"></td><td>붕뜨네</td><td>검 우클릭 시 바라보는 방향으로<br><span style="color:#008B8B">돌풍구</span>를 날립니다.</td></tr>
<tr><td align="center"><img src="docs/icons/creeper_spawn_egg.png" width="64"></td><td>신체 폭탄</td><td>버리기 키를 누르면<br>체력 <span style="color:#2E7D32">3칸</span>을 소모하고<br><span style="color:#2E7D32">TNT</span>를 던집니다.</td></tr>
<tr><td align="center"><img src="docs/icons/spawn_egg.png" width="64"></td><td>옥견</td><td><b>두 마리</b>의 길들인 늑대가<br>당신을 도와 싸웁니다.<br>늑대는 사망 시 <b>60초</b> 후 부활합니다.<br>당신이 사망하면 늑대도<br>같이 사라지며 함께 부활합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/iron_axe.png" width="64"></td><td>육중한 힘</td><td>공격력이 <span style="color:#888888">20%</span> 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/tnt_iso.png" width="64"></td><td>자폭</td><td><span style="color:#D32F2F">35초</span>마다 머리 위에 TNT가 부착됩니다.<br>부착 시 <span style="color:#D32F2F">5초</span>간 신속을 받은 뒤 폭발하며<br>반경 <span style="color:#D32F2F">8칸</span> 내 적에게<br>최대 체력의 <span style="color:#D32F2F">20%</span> 고정 피해를 줍니다.<br>자신은 그 피해의 <span style="color:#D32F2F">50%</span>만 받습니다.<br>폭탄 보유 중 사망하면 즉시 폭발합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/bow.png" width="64"></td><td>저격 대결</td><td><span style="color:#D4AC0D">12칸</span> 이상 거리의 적을 공격 시<br>피해가 <span style="color:#D4AC0D">50%</span> 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/redstone.png" width="64"></td><td>죄책감의 쾌락</td><td>적 처치 시 최대 체력의<br><span style="color:#D32F2F">20%</span>를 회복합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/blaze_powder.png" width="64"></td><td>주님의 사랑</td><td>검 우클릭 시 바라보는 방향으로<br><span style="color:#CC9900">화염구</span>를 <span style="color:#CC9900">3회</span> 연달아 날립니다.</td></tr>
<tr><td align="center"><img src="docs/icons/sugar.png" width="64"></td><td>클린업</td><td>체력 <b>30%</b> 이하인 적이<br><b>10칸</b> 내에 있으면<br>이동 속도가 <b>50%</b> 증가합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/cooked_porkchop.png" width="64"></td><td>포화지방산</td><td><span style="color:#D4AC0D">배고픔</span>이 소모되지 않습니다.<br>음식 섭취 시<br><span style="color:#D4AC0D">즉시 치유</span> 효과를 받습니다.</td></tr>
<tr><td align="center"><img src="docs/icons/obsidian_iso.png" width="64"></td><td>흑섬</td><td>공격마다 <span style="color:#555555">3% 확률</span>로<br><span style="color:#555555">2.5배</span>의 피해를 줍니다.<br>발동 후 <span style="color:#555555">3번</span>의 공격 동안<br>확률이 증가합니다.</td></tr>
</tbody>
</table>

</details>

<details>
<summary>프리즘 증강 목록 (5종)</summary>

<table>
<thead>
<tr><th width="17%"></th><th width="33%">이름</th><th width="50%">설명</th></tr>
</thead>
<tbody>
<tr><td align="center"><img src="docs/icons/smithing_table_iso.png" width="64"></td><td>능력치 더하기 더하기 더하기</td><td>능력치 모루 <span style="color:#8E24AA">4개</span>를 획득합니다!</td></tr>
<tr><td align="center"><img src="docs/icons/rabbit_foot.png" width="64"></td><td>드롭킥</td><td>근접 공격 시 체력 <span style="color:#D32F2F">15%</span> 이하인<br>적을 <span style="color:#D32F2F">즉사</span>시킵니다.<br>즉사한 적에게 <span style="color:#D32F2F">강한 넉백</span>과<br><span style="color:#D32F2F">폭발</span>이 발생하며<br>자신의 체력을 최대 체력의<br><span style="color:#D32F2F">50%</span> 회복합니다.</td></tr>
<tr><td align="center"><img src="docs/icons/glass_iso.png" width="64"></td><td>유리 대포</td><td>최대 체력이 <b>30%</b> 감소합니다.<br>공격 시 <b>15%</b>의<br>추가 고정 피해를 입힙니다.</td></tr>
<tr><td align="center"><img src="docs/icons/golden_sword.png" width="64"></td><td>처형인의 검</td><td><span style="color:#D4AC0D">내구도 1</span>, 높은 날카로움의<br>금 검을 즉시 지급합니다.<br>검이 깨지면 <span style="color:#D4AC0D">299초</span> 후<br>자동으로 다시 지급됩니다.</td></tr>
<tr><td align="center"><img src="docs/icons/jukebox_iso.png" width="64"></td><td>탭 댄서</td><td>기본 공격 시 <span style="color:#008B8B">0.02</span>의 이동 속도를<br>무한히 중첩하여 얻습니다.<br>마지막 중첩 후 <span style="color:#008B8B">5초</span> 이내<br>중첩을 쌓지 않으면<br>중첩이 빠르게 감소합니다.</td></tr>
</tbody>
</table>

</details>
