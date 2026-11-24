테스트용 배포 구성 (APK 미빌드)
GitHub creep1020/grammar-150 저장소에 업로드하고 Settings > Pages > main / root 활성화. Actions > Build Grammar APK 실행.
웹 주소가 다르면 MainActivity.java URL 수정. Firebase 인증 허용 도메인 및 Firestore 권한 확인 필요.
Android SpeechRecognizer 인식 콜백 및 마이크 권한 처리 추가. 녹음 파일 재생용 오디오 반환 및 FCM 푸시는 미구현. 실제 APK 빌드 및 휴대폰 검증 필요.

V95: WebView 오디오 캡처 권한 처리 추가 (Android 마이크 권한이 이미 허용된 경우). 네이티브 음성인식 녹음 파일 반환은 미구현. 음성인식 서비스의 PCM 콜백은 보장되지 않아 임의 WAV 변환을 적용하지 않음. FCM 미연동.
