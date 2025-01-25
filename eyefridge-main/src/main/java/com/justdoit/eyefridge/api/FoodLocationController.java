package com.justdoit.eyefridge.api;

import com.justdoit.eyefridge.domain.Grocery;
import com.justdoit.eyefridge.service.GroceryService;
import com.justdoit.eyefridge.service.SpeechToTextService;
import com.justdoit.eyefridge.service.TextToSpeechService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@AllArgsConstructor
@RequestMapping("/food")
public class FoodLocationController {

    //생성자
    private final GroceryService groceryService;
    private final SpeechToTextService speechToTextService;
    private final TextToSpeechService textToSpeechService;

    //식료품 위치 음성
    //음성파일-> API 호출-> Sring추출-> db조회-> 구역 속성 추출-> API 호출-> 음성파일
    @PostMapping("/location")
    public ResponseEntity<String> foodLocation(@RequestPart("speechFile") MultipartFile speechFile) {
        //음성-> 스트링
        speechToTextService.changeToText(speechFile);
        String sttGrocery = speechToTextService.getSttResultValue();

        //스트링-> 검색
        try {
            Grocery grocery = groceryService.findGroceryName(sttGrocery);
            int location = grocery.getLocation();
            String groceryLocation = String.valueOf(location) + ",구역 입니다.";

            //스트링-> 음성 파일 저장
            textToSpeechService.setTTSaudio(groceryLocation);
            return ResponseEntity.ok("녹음 파일 만들기 성공했습니다.");
        } catch (EntityNotFoundException e) {
            //스트링-> 음성 파일 저장
            String groceryLocation = "다시 시도 해주세요";
            textToSpeechService.setTTSaudio(groceryLocation);
            return ResponseEntity.ok("음식 위치 검색 실패했습니다.");
        }
    }

    @GetMapping("/location")
    public ResponseEntity<Resource> locatioVoice() {

        String voicePath = "src/main/java/com/justdoit/eyefridge/voice/checkcheck.mp3";
        Resource resource = new FileSystemResource(voicePath);

        // Content-Disposition 헤더를 설정하여 다운로드할 때의 파일 이름을 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
