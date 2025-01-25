package com.justdoit.eyefridge.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Getter
public class SortService {

    private LocalDate today = LocalDate.now();

    private List<String> meatList = Arrays.asList("고기", "소", "돼지", "닭", "양", "베이컨", "소시지",
                                                "햄", " 우", "립아이", "스테이크", "심", "차돌박이",
                                                "설도", "육", "갈비", "살", "날개", "다리", "삼겹", "BBQ");

    private List<String> fishList = Arrays.asList("어", "치", "미", "리", "태", "기", "우럭", "조개",
                                                "굴", "락", "전복", "새우", "낙지", "대구");

    private List<String> vegetableList = Arrays.asList("버섯", "고추", "추", "무", "당근", "파", "피망", "감자",
                                                "깻잎", "고구마", "호박", "가지", "오이", "시금치");


    public LocalDate expiration(int sortedGrocery) {
        if (sortedGrocery == 1) {
            LocalDate expirationDate = today.plusDays(3);
            return expirationDate;
        } else if (sortedGrocery == 2) {
            LocalDate expirationDate = today.plusDays(3);
            return expirationDate;
        } else if (sortedGrocery == 3) {
            LocalDate expirationDate = today.plusDays(5);
            return expirationDate;
        } else {
            return today.plusDays(3);
        }
    }

    public int sortGrocery(String grocery) {
        if (classifyMeat(grocery, meatList)) {
            return 1;
        } else if (classifyFish(grocery, fishList)) {
            return 2;
        } else if (classifyVege(grocery, vegetableList)) {
            return 3;
        } else {
            return 4;
        }
    }

    public boolean classifyMeat(String grocery, List<String> meatList){
        for (String meat :
                meatList) {
            if (grocery.contains(meat)) {
                return true;
            }
        }
        return false;
    }
    public boolean classifyFish(String grocery, List<String> fishList){
        for (String fish :
                fishList) {
            if (grocery.contains(fish)) {
                return true;
            }
        }
        return false;
    }
    public boolean classifyVege(String grocery, List<String> vegetableList){
        for (String vege :
                vegetableList) {
            if (grocery.contains(vege)) {
                return true;
            }
        }
        return false;
    }
}
