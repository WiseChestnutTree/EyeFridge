package com.justdoit.eyefridge.api;

import com.justdoit.eyefridge.domain.Grocery;
import com.justdoit.eyefridge.service.GroceryService;
import com.justdoit.eyefridge.service.ReceiptService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@AllArgsConstructor
@RequestMapping("/alarm")
public class AlarmController {

    //생성자
    private final GroceryService groceryService;

    //유통기한 임박 알람
    //DB조회-> 유통기한 1,2일 남은 식료품 조회-> 리스트 업
    @GetMapping("/expiryGroceries")
    public expirationGrocery expirationAlarm() {
        List<Grocery> findGroceries = groceryService.findExpiryGroceries();
        List<GroceriesDto> collect = findGroceries.stream()
                .map(g -> new GroceriesDto(
                                g.getId(),
                                g.getGroceryName(),
                                g.getQuantity(),
                                g.getExpirationDate(),
                                g.getLocation(),
                                g.getState()
                        )
                )
                .collect(Collectors.toList());
        return new expirationGrocery(collect);
    }

    @PutMapping ("/edit/{id}")
    public ResponseEntity<String> editStateGrocery(@PathVariable Long id) {
        groceryService.updateState(id);
        return ResponseEntity.ok("state가 변경됐습니다.");
    }

    @Data
    @AllArgsConstructor
    static class expirationGrocery<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class GroceriesDto {
        private Long groceryId;

        private String groceryName;

        private int quantity;

        private LocalDate expirationDate;

        private int location;

        private int state;
    }
}
