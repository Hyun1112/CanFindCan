# CanFindCan 딥러닝 기반 캔음료 인식 애플리케이션

## 💡 프로젝트 소개
- 시각장애인들에게 새로운 소통 방식을 제공하기 위해 애플리케이션을 구현하고자 함
- 점자 표기로는 종류의 구분이 불가능한 캔 음료의 구분에 목적을 둠
- 카메라로 캔을 비추면 캔의 종류를 분석하고, 캔의 밑바닥을 촬영하면 유통기한을 음성으로 안내
<br/>

## 💡 개발 배경
<img src="https://user-images.githubusercontent.com/88569472/203055052-8023b95d-f4ab-4a0f-809e-28f3d639ee0d.png" width="850" height="480">
<img src="https://user-images.githubusercontent.com/88569472/203055161-21ca38c8-a0a2-4d50-aff3-a9bc0130c6bc.png" width="850" height="420">
시각장애인들은 캔 음료의 종류를 구분하지 못하고 유통기한을 인식하지 못하여 음료수를 고르는데 불편을 겪고 있다.
<br/>
국내에서 팔고 있는 캔 음료는 구체적인 상품명이 아닌 “음료“, 혹은 “탄산”으로만 표기되어 있는 경우가 대부분이고 유통기한은 점자 표기조차 없어 글을 읽을 수 없다면 파악할 수 없다.
<br/>
이러한 불편함을 해소하고자 시각장애인들의 캔 음료 인식을 도와주는 애플리케이션 “CanFindCan”을 개발하게 되었다.
<br/>

## 💡 주요 기능
### 1. 캔 음료 인식
<img src="https://user-images.githubusercontent.com/88569472/203058225-991607c8-d69c-4920-85fa-ae562edbccc8.png" width="850" height="450">
- 애플리케이션 실행 시 자동으로 웹 뷰 실행
- 학습된 데이터가 적용된 HTML 기반의 웹 서버
- 실행된 카메라에 캔을 비추면 종류 분석 진행
- 분석 완료 시 음성 안내

### 2. 유통기한 인식
<img src="https://user-images.githubusercontent.com/88569472/203058374-ff09671b-a44b-4d90-9d7d-41504c6f0a16.png" width="700" height="450">
<img src="https://user-images.githubusercontent.com/88569472/203058501-286a6035-0d46-4aaf-a861-8bed711f121a.png" width="700" height="450">
- 중앙부에 위치한 카메라 버튼 클릭 시 내장 카메라 실행
- 카메라로 캔 바닥 촬영
- 유통기한 글자 추출 후 음성 안내
- 상단 BACK 버튼 클릭 시 캔 음료 인식 기능 재실행
<br/>

## 💡 구성도
<img src="https://user-images.githubusercontent.com/88569472/203058916-f8b371b0-55fe-4704-af7a-5659f0b1fb63.png" width="800" height="300">
<img src="https://user-images.githubusercontent.com/88569472/203058972-6ebb2030-fed5-48f7-92da-1deb67ce2176.png" width="400" height="900">
<br/>

## 💡 사용 기술
- Web Crawling
- labelImg 툴을 이용하여 이미지의 클래스와 경계박스 지정
- YOLOv5를 사용하여 모델 학습
- 학습 된 pt 파일을 tfjs 모델로 변환
- Amazon AWS IoT Core, AWS Rule, AWS IAM, AWS S3, AWS Cognito
- Amazon Rekognition
<br/>

## 💡 기대효과 및 발전방향
- 캔 음료 구분에 대한 시각장애인들의 불편함 해소
- 일반 음료, 과자, 의약품 등과 같이 다양한 분야에서 활용
- 캔 상세정보 검출, 캔 위치 찾기 등 추가적인 기능으로 애플리케이션 확장
<br/>

## 💡 프로젝트 기간
2022.03 ~ 2022.06
</br>

## 💡 팀원
|이름|역할|
|----|-------|
|김현경|UI/UX, 텍스트 인식 기능 구현|
|이하영|텍스트 인식 기능 구현, 앱 구현|
|임수빈|웹 서버 개발, 모델 학습|
|정민경|데이터 수집, 모델 학습, 앱 구현|


