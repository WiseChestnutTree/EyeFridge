package com.justdoit.eyefridge.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class SpeechToTextService {
    String clientId = "1ioya0ex75";                                     // STT id
    String clientSecret = "dBJ2Zji1sN1mkUZW0JGLQUOinl2dM7RYJcxtnBik";   // STT 비밀 키
    // STT 결과값 담을 string
    String sttResultValue;

    //결과 String 가져오기
    public String getSttResultValue() {
        return sttResultValue;
    }

    public SpeechToTextService() {   // STT결과값 초기화.
        sttResultValue = "";
    }

    // Speech-To-Text 본체
    public void changeToText(MultipartFile speechFile) {
        try {
            //-----------------------여기를 수정하면 됩니다(음성 파일)--------------------------------
            File voiceFile = convertMultipart2File(speechFile);

//            File voiceFile = new File(soundFile);// 설정.
            //-----------------------------------------------------------------------------------

            // stt?lang=언어 코드 ( Kor, Jpn, Eng, Chn )
            String apiURL = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt?lang=Kor";
            URL url = new URL(apiURL);
            //-----------------------기본 틀 만들기----------------------------------------
            //캐시 사용x, 출력, 입력, type, api키 설정
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
            //-----------------------출력,입력 스트림 객체 생성------------------------------
            OutputStream outputStream = conn.getOutputStream();
            FileInputStream inputStream = new FileInputStream(voiceFile);
            //-----------------------버퍼 설정 및 데이터 읽고 쓰기---------------------------
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            //-----------------------post 걸기-------------------------------------------
            BufferedReader br = null;
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {  // 오류 발생
                System.out.println("error!!!!!!! responseCode= " + responseCode);
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            //-----------------------결과갑 가져오기---------------------------------------
            String inputLine;
            if (br != null) {
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                //-----------------------결과 가공 부분---------------------------------------
//                System.out.println(response.toString());
                jsonProcess(response);
            } else {
                sttResultValue = "Speech-To-Text 실행중 오류가 발생하였습니다.";
            }
            System.out.println("changeToText 결과 : " + sttResultValue);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void jsonProcess(StringBuffer response) {
        int indexCheck, start, end;
        indexCheck = response.indexOf("text");
        start = response.indexOf("\"", indexCheck + 5);
        end = response.indexOf("\"", start + 1);
        sttResultValue = response.substring(start + 1, end);
//        System.out.println("jsonProcess 결과 : "+sttResultValue);
    }

    private File convertMultipart2File(MultipartFile multipartFile) throws IOException{
        File file = new File("temp");

        Files.copy(multipartFile.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return file;
    }
}

