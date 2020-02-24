외국인 관광객들을 위한 정보번역, 제공 어플리케이션

기능은 총 세가지가 있는데
1. AR기능을 사용한 주변(반경 100M) 장소 표시
  Wikitude SDK를 사용해 구현한 AR기능을 사용하여 사용자 위치 반경 100M이내의 장소들을 AR마커로 표시한다 마커를 클릭하게 되면 그 장소에대한 자세한 정보들이 번역되어 표시된다. 또한, 길찾기 버튼을 클릭하면 다음 카카오 맵으로 넘어가며 길을 찾을수있도록 하였다.
  
2. 구글맵을 사용한 전체 장소 표시
  사용자가 선호하는 메뉴를 선택할수있다(한식,중식,양식,분식 등등) 선택하고 검색을 누르게되면 검색되어져 지도에 마커로 표시되게 되고 마커를 클릭시 다음 카카오맵으로 넘어가 길을 찾을수있다.
  
3. 음성인식 번역 기능
  STT기능을 이용하여 음성을 인식해 텍스트로 변환 후 그 텍스트를 파파고 API를 이용해 변역한다.
  
- Server
  서버는 AWS EC2 가상컴퓨터를 이용해 구축하였으며 nodejs를 이용해 웹서버를 구축하였고 서울시 공공API를 받아 가공(번역) 후 1번기능에게 데이터를 넘겨주게 된다.
  
- 환경설정
  환경설정 탭에서 사용자가 원하는 언어를 선택할수있다. 현재는 한국어, 영어, 중국어, 일본어, 베트남어, 스페인어를 지원중이다.
