package com.justdoit.eyefridge.service;

import com.justdoit.eyefridge.domain.Grocery;
import com.justdoit.eyefridge.repository.GroceryReository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroceryService {

    private final GroceryReository groceryReository;

    /*
    영수증 등록 혹은 form데이터 등록
    */
    @Transactional
    public Long addGrocery(Grocery grocery) {
        //기존 DB에 존재 여부 확인
        Grocery existingGrocery = validateDuplicateGrocery(grocery);

        if (existingGrocery != null) {
            //같은 식료품 존재
            int updateQuantity = existingGrocery.getQuantity() + grocery.getQuantity();
            existingGrocery.setQuantity(updateQuantity);
            groceryReository.save(existingGrocery);
            return existingGrocery.getId();
        } else {
            //신규 식료품 등록
            groceryReository.save(grocery);
            return grocery.getId();
        }
    }

    //중복된 식료품 존재 여부 확인
    private Grocery validateDuplicateGrocery(Grocery grocery) {
        List<Grocery> findGroceries = groceryReository.findByName(grocery.getGroceryName());
        if (!findGroceries.isEmpty()) {
            //같은 식료품 존재
            return findGroceries.get(0);
        }
        //신규 식료품
        return null;
    }

    //식료품 전체 목록 조회
    public List<Grocery> findGroceries() {
        return groceryReository.findAll();
    }

    //식료품 id로 검색
    public Grocery findOne(Long groceryId) {
        return groceryReository.findOne(groceryId);
    }

    //식료품 이름으로 검색
    public Grocery findGroceryName(String groceryName) {
        List<Grocery> grocery = groceryReository.findByName(groceryName);
        if (grocery.isEmpty()) {
            throw new EntityNotFoundException(groceryName + "이 없어요");
        }
        return grocery.get(0);
    }

    //식료품 유통기한 2일 이하 조회
    public List<Grocery> findExpiryGroceries() {
        return groceryReository.findExpirationDate2();
    }

    //식료품 위치로 검색
//    public int findLocation(int location) {
//        return groceryReository.findByLocation(location);
//    }

    //식료품 속성 수정
    @Transactional
    public void updateGrocery(Long id, String groceryName, int quantity, LocalDate expirationDate, int location) {
        Grocery findGrocery = groceryReository.findOne(id);

        findGrocery.setGroceryName(groceryName);
        findGrocery.setQuantity(quantity);
        findGrocery.setExpirationDate(expirationDate);
        findGrocery.setLocation(location);
    }

    //식료품 속성 수정
    @Transactional
    public void updateState(Long id) {
        Grocery findGrocery = groceryReository.findOne(id);

        findGrocery.setState(1);
    }

    //식료품 삭제
    @Transactional
    public boolean deleteById(Long id) {
        try {
            groceryReository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
