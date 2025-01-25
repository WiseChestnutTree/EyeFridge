package com.justdoit.eyefridge.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class TextToSpeechService {

    //--------------------api id, key
    public static String ttsId = "84evqhen0a";
    public static String ttsSecret = "LhWrIwWgUAs28wPudnngsqDHiz459yaz5HH4rEPq";
    //--------------------음성으로 바꿀 text, 저장경로, 파일경로 가져오기
    String getText = "";
    public static String upload_Mp3 = "src/main/java/com/justdoit/eyefridge/voice/checkcheck.mp3"; //임시 경로, 다시 설정해야함.

//    private String getPath() {
//        return upload_Mp3;
//    } // 일단 삭제 보류

    //--------------------생성자
    TextToSpeechService() {
    }

//    public void setTTSaudio(String speechText) throws IOException {
public void setTTSaudio(String speechText) {
        // String을 받아서 Audio파일 생성하는 매서드
        try {
            // text 저장 및 파일 경로.
//            String toSay = getText;
            String audioFilePath = upload_Mp3;

            // 음성으로 바꿀 값 인코딩
            String text = URLEncoder.encode(speechText, "UTF-8");
            String apiURL = "https://naveropenapi.apigw.ntruss.com/tts-premium/v1/tts";
            URL url = new URL(apiURL);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", ttsId);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", ttsSecret);
            //------------요청 파라미터 설정
            String postParams = "speaker=nara&volume=0&speed=0&pitch=0&format=mp3&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            // api 응답코드
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                InputStream is = con.getInputStream();
                int read = 0;
                byte[] bytes = new byte[1024];
                // 정해진 경로에 저장
                File f = new File(audioFilePath);
                // f.createNewFile();
                OutputStream outputStream = new FileOutputStream(f);
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                is.close();
            } else {  // 오류 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println(response.toString());
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    //--------------------return을 위해 가공
//    public ResponseEntity<String> downloadMp3File() {
//        try {
//            // 이미 저장된 MP3 파일을 읽어 byte 배열 변환
//            File mp3File = new File(upload_Mp3);
//            byte[] mp3Bytes = Files.readAllBytes(mp3File.toPath());
//
//            // byte 배열을 16진수 문자열로 변환하여 출력
//            String hexString = bytesToHexString(mp3Bytes);
//
//            // 클라이언트에게 16진수 문자열을 응답으로 전송
//            return ResponseEntity.ok().body(hexString);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Failed to read or provide the MP3 file");
//        }
//    }
//
//    // 위 메서드에서 사용 (byte -> 문자열 변환)
//    public String bytesToHexString(byte[] bytes) {
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : bytes) {
//            // 0xFF를 사용하여 부호 없는 8비트로 변환 후 16진수 문자열로 변환
//            hexString.append(String.format("%02X", b & 0xFF));
//        }
//        return hexString.toString();
//    }
}
