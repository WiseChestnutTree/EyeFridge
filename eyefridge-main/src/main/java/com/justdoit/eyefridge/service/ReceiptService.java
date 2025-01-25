package com.justdoit.eyefridge.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReceiptService {

    // 이미지에서 텍스트를 추출하기 위한 CLOVA OCR API 호출 URL
    String invokeURL = "https://vl44rti1bx.apigw.ntruss.com/custom/v1/25925/08c27baa525cca20b5a227378a42bf959331e82202b318385ab6923bb8c49c2e/document/receipt";
    // CLOVA OCR API의 비밀 키
    String secretKey = "ckltbllCcm1TcGFCdmpzdkVoR0lpU09hQ3p2cE5GalM=";
    public List<String> groceryNames;
    public List<Integer> groceryQuantities;
    private int indexItem, indexName, indexSname, indexEname, indexCount, indexSCount, indexECount, totalPrice;



    public List<String> getGroceryNames() {
        return groceryNames;
    }

    public List<Integer> getGroceryQuantities() {
        return groceryQuantities;
    }

    public ReceiptService() {
        groceryNames = new ArrayList<>();
        groceryQuantities = new ArrayList<>();
    }

//    public ReceiptService(이미지) {
//        groceryNames = new ArrayList<>();
//        groceryQuantities = new ArrayList<>();
//    }
    public void receiptScan(MultipartFile receiptFile) {
        groceryQuantities = new ArrayList<>();
        groceryNames = new ArrayList<>();
        // CLOVA OCR API 호출 및 응답 데이터 처리
        try {
            // URL 객체 생성
            URL url = new URL(invokeURL);

            // HttpURLConnection 객체 생성
            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            // 캐싱 사용 여부 설정
            con.setUseCaches(false);

            // 입력 및 출력 여부 설정
            con.setDoInput(true);
            con.setDoOutput(true);

            // 읽기 시간 제한 설정
            con.setReadTimeout(30000);

            // 요청 메서드 설정
            con.setRequestMethod("POST");

            // multipart/form-data 콘텐츠 타입 설정
            String boundary = "----" + UUID.randomUUID().toString().replaceAll("-", "");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            // CLOVA OCR API의 비밀 키 설정
            con.setRequestProperty("X-OCR-SECRET", secretKey);
//---------------------------------클로바 서버에 요청부-----------------------------
            // JSON 객체 생성
            JSONObject json = new JSONObject();

            // JSON 객체에 API 요청 정보 설정
            json.put("version", "V2");
            json.put("requestId", UUID.randomUUID().toString());
            json.put("timestamp", System.currentTimeMillis());

            // 이미지 정보를 포함하는 JSON 객체 생성
            JSONObject image = new JSONObject();
            image.put("format", "jpg");
            image.put("name", "demo");

            // 이미지 정보를 포함하는 배열 생성
            JSONArray images = new JSONArray();
            images.put(image);

            // JSON 객체에 이미지 정보를 포함하는 배열 설정
            json.put("images", images);

            // JSON 객체 문자열로 변환
            String postParams = json.toString();
//---------------------------------클로바 ocr 반환 형식-----------------------------
            // 연결
            con.connect();

            // 출력 스트림 생성
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());

            //multipartFile을 file로 변환
            File file = convertMultipart2File(receiptFile);

            // multipart/form-data 콘텐츠 작성
//            File file = new File(imageFile);
            writeMultiPart(wr, postParams, file, boundary);

            // 출력 스트림 닫기
            wr.close();

            // 응답 코드 가져오기
            int responseCode = con.getResponseCode();
            BufferedReader br;
            // 응답 코드가 200이면 성공한 것으로 간주
            if (responseCode == 200) {
                // 입력 스트림 생성
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                // 입력 스트림 생성
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            // 응답 데이터를 버퍼에 저장
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
//---------------------------------클로바 서버에 요청에 대한 결과값 저장-----------------------------
            jsonProcess(response);

            // 입력 스트림 닫기
            br.close();

        } catch (Exception e) {
            // 예외 메시지 출력
            System.out.println(e);
        }
    }

    private void jsonProcess(StringBuffer response){
        //  2-1) items 위치 찾은 후 index 저장
        indexItem = response.indexOf("items");
        totalPrice = response.indexOf("totalPrice");
        indexName = response.indexOf("name",indexItem); // 초기 상품명

        //  totalPrice 보다 인덱스가 작으면 실행
        while(totalPrice > indexName){
            groceryNames.add(indexName(response));
            groceryQuantities.add(indexQuantity(response));
            indexName = response.indexOf("name",indexSCount);   // 다음 상품명 위치 찾기 위해 아리로

        }
    }

    private File convertMultipart2File(MultipartFile multipartFile) throws IOException{
        File file = new File("temp");

        Files.copy(multipartFile.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return file;
    }

    private int indexQuantity(StringBuffer response) {
        //	3-1) count 찾기 + text 찾기
        indexCount = response.indexOf("count", indexEname);
        indexCount = response.indexOf("text", indexCount+6);
        //	3-2) 2-3 동일
        indexSCount = response.indexOf("\"",indexCount+5);	//scount+1 = 수량 시작 위치
        indexECount = response.indexOf("\"",indexSCount+1);//ecount-1 = 수량 끝 위치

        //	3-3) 수량 저장
        int groceryCount = Integer.parseInt(response.substring(indexSCount+1,indexECount));
        return groceryCount;
    }

    private String indexName(StringBuffer response){
        //	2-2) name 찾기 + text 찾기
        indexName = response.indexOf("text",indexName);
        //	2-3) 상품 이름 찾기
        indexSname = response.indexOf("\"",indexName+5);	//sname+1 = 상품 이름 시작 위치
        indexEname = response.indexOf("\"",indexSname+1);//ename-1 = 상품 이름 끝 위치

        //	2-4) 상품 이름 추출
        String groceryName = response.substring(indexSname + 1,indexEname);
        return groceryName;
    }

    private void writeMultiPart(OutputStream out,
                                String jsonMessage,
                                File file,
                                String boundary) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
        sb.append(jsonMessage);
        sb.append("\r\n");

        out.write(sb.toString().getBytes("UTF-8"));
        out.flush();

        if (file != null && file.isFile()) {
            out.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
            StringBuilder fileString = new StringBuilder();
            fileString.append("Content-Disposition:form-data; name=\"file\"; filename=");
            fileString.append("\"" + file.getName() + "\"\r\n");
            fileString.append("Content-Type: application/octet-stream\r\n\r\n");

            out.write(fileString.toString().getBytes("UTF-8"));
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.write("\r\n".getBytes());
            }

            out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
        }
        out.flush();
    }
}

