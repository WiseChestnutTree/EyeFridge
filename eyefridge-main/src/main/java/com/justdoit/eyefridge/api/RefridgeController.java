package com.justdoit.eyefridge.api;

import com.justdoit.eyefridge.domain.Grocery;
import com.justdoit.eyefridge.service.GroceryService;
import com.justdoit.eyefridge.service.ReceiptService;
import com.justdoit.eyefridge.service.SortService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@AllArgsConstructor
@RequestMapping("/refridge")
public class RefridgeController {

    //생성자
    private final GroceryService groceryService;
    private final ReceiptService receiptService;
    private final SortService sortService;

    //영수증 요청-> OCR 진행-> DB에 저장, 관리

    //file 형식 맞추고 이미지 넘어오는거 확인 후
    //List로 넘어오는 식료품 이름 분석 후 구역과 유통기한 임의 지정
    //db 로직
    @PostMapping("/receipt")
    public ResponseEntity<Resource> manageReceipt(@RequestPart("receiptFile") MultipartFile receiptFile) {
        receiptService.receiptScan(receiptFile);
        for (int i = 0; i < receiptService.getGroceryNames().size(); i++) {
            Grocery scanGrocery = new Grocery();
            String scanGroceryName = receiptService.getGroceryNames().get(i);
            int scanGroceryQuantity = receiptService.getGroceryQuantities().get(i);
            scanGrocery.setGroceryName(scanGroceryName);
            scanGrocery.setQuantity(scanGroceryQuantity);

            //위치
            int location = sortService.sortGrocery(scanGroceryName);
            scanGrocery.setLocation(location);
            //유통기한
            scanGrocery.setExpirationDate(sortService.expiration(location));
            //db에 저장
            groceryService.addGrocery(scanGrocery);
        }
        //실패 시 실패 음성 파일 전송
        if (receiptService.getGroceryNames().isEmpty()) {
            String failVoice = "src/main/java/com/justdoit/eyefridge/voice/fail.mp3";
            Resource resource = new FileSystemResource(failVoice);

            // Content-Disposition 헤더를 설정하여 다운로드할 때의 파일 이름을 지정
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            String successVoice = "src/main/java/com/justdoit/eyefridge/voice/success.mp3";
            Resource resource = new FileSystemResource(successVoice);

            // Content-Disposition 헤더를 설정하여 다운로드할 때의 파일 이름을 지정
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
    }

    /*
        db에서 냉장고 리스트 조회
        조회할 것이 없을 때 처리해야함
    */
    @GetMapping("/searchAll")
    public allGrocery groceries() {
        //임의의 데이터 추가
//        for (int i = 0; i < 3; i++) {
//            Grocery grocery = new Grocery();
//            grocery.setGroceryName("김치" + Integer.toString(i));
//            grocery.setQuantity(i);
//            grocery.setExpirationDate("2000-12-12");
//            grocery.setLocation(i);
//            groceryService.addGrocery(grocery);
//        }
        //
        List<Grocery> findGroceries = groceryService.findGroceries();
        List<GroceriesDto> collect = findGroceries.stream()
                .map(g -> new GroceriesDto(
                                g.getId(),
                                g.getGroceryName(),
                                g.getQuantity(),
                                g.getExpirationDate(),
                                g.getLocation()
                        )
                )
                .collect(Collectors.toList());
        return new allGrocery(collect);
    }

    /*
        db에 개별 데이터 추가
    */
    @PostMapping("/add")
    public ResponseEntity<String> addGrocery(@RequestBody @Validated CreateGroceryRequest request) {

        Grocery grocery = new Grocery();
        grocery.setGroceryName(request.getGroceryName());
        grocery.setQuantity(request.getQuantity());
        grocery.setExpirationDate(request.getExpirationDate());
        grocery.setLocation(request.getLocation());

        Long id = groceryService.addGrocery(grocery);
        //id 값 반환
//        return new CreateGroceryResponse(id);
        return ResponseEntity.ok("식료품 추가를 성공했습니다.");
    }

    /*
        식료품 하나 조회-> 수정
    */
//    @GetMapping("/edit/{id}")
//    public oneGrocery grocery(@PathVariable("id") Long id){
//        Grocery findGrocery = groceryService.findOne(id);
//        GroceriesDto groceriesDto = new GroceriesDto(
//                findGrocery.getGroceryName(),
//                findGrocery.getQuantity(),
//                findGrocery.getExpirationDate(),
//                findGrocery.getLocation()
//        );
//        return new oneGrocery(groceriesDto);
//    }

    /*
        식료품 수정 pathVariable로 id를 넣을지 name을 넣을지 결정
    */
    @PutMapping("/edit/{id}")
    public ResponseEntity<String> updateGrocery(@PathVariable("id") Long id, @RequestBody @Validated UpdateGroceryRequest request) {
        groceryService.updateGrocery(id, request.groceryName, request.quantity, request.expirationDate, request.location);
        Grocery findGrocery = groceryService.findOne(id);
//        return new UpdateGroceryResponse(findGrocery.getId(), findGrocery.getGroceryName());
        return ResponseEntity.ok("식료품 수정을 성공했습니다.");
    }

    //db에 개별 데이터 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteGrocery(@PathVariable Long id) {
        boolean isDeleted = groceryService.deleteById(id);

        if (isDeleted) {
            return ResponseEntity.ok("식료품 삭제를 성공했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 식료품이 없습니다.");
        }
    }

    @Data
    static class UpdateGroceryRequest {
        private String groceryName;

        private int quantity;

        private LocalDate expirationDate;

        private int location;
    }

    @Data
    @AllArgsConstructor
    static class UpdateGroceryResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class oneGrocery<T> {
        private T data;
    }

    @Data
    static class CreateGroceryRequest {
        private String groceryName;

        private int quantity;

        private LocalDate expirationDate;

        private int location;
    }

    @Data
    static class CreateGroceryResponse {
        private Long id;

        public CreateGroceryResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    @AllArgsConstructor
    static class allGrocery<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class GroceriesDto {
        private Long Id;

        private String groceryName;

        private int quantity;

        private LocalDate expirationDate;

        private int location;
    }
}
