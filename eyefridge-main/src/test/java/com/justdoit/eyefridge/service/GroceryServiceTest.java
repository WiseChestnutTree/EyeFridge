package com.justdoit.eyefridge.service;

import com.justdoit.eyefridge.domain.Grocery;
import com.justdoit.eyefridge.repository.GroceryReository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class GroceryServiceTest {

    @Autowired
    GroceryService groceryService;
    @Autowired
    GroceryReository groceryReository;
    @Autowired
    EntityManager em;

    @Test
    @Rollback(value = false)
    public void 식료품등록() throws Exception {
        //given
        Grocery grocery = new Grocery();
        grocery.setGroceryName("김치");

        //when
        Long savedGroceryId = groceryService.addGrocery(grocery);

        //then
        Grocery savedGrocery = groceryReository.findOne(savedGroceryId);
        assertNotNull(savedGrocery); // Check if the saved grocery is not null
        assertEquals("김치", savedGrocery.getGroceryName());
    }

    @Test
    @Rollback(value = false)
    public void 중복식료품처리() throws Exception {
        //given
        Grocery grocery1 = new Grocery();
        grocery1.setGroceryName("김치");
        grocery1.setQuantity(1);

        Grocery grocery2 = new Grocery();
        grocery2.setGroceryName("김치");
        grocery2.setQuantity(2);

        //when
        groceryService.addGrocery(grocery1);
        groceryService.addGrocery(grocery2);

        //then
        List<Grocery> updatedGroceries = groceryReository.findByName("김치");

        //then
        assertEquals(1, updatedGroceries.size()); // Only one grocery with name "김치" should exist
        assertEquals(3, updatedGroceries.get(0).getQuantity());
    }

    @Test
    @Transactional
    @Rollback(value = false)
    public void 식료품수정() throws Exception {
        //given
        Grocery grocery1 = new Grocery();

        grocery1.setId(1L);
        grocery1.setGroceryName("김치");
        grocery1.setQuantity(1);
        grocery1.setExpirationDate("2000-12-12");
        grocery1.setLocation(4);
        groceryReository.save(grocery1);

        //when
        groceryService.updateGrocery(grocery1.getId(),"불고기", 2, "1000-2-2", 1);

        //then
        Grocery updatedGrocery1 = groceryReository.findOne(grocery1.getId());
        assertEquals("불고기", updatedGrocery1.getGroceryName());
        assertEquals(2, updatedGrocery1.getQuantity());
        assertEquals("1000-2-2", updatedGrocery1.getExpirationDate());
        assertEquals(1, updatedGrocery1.getLocation());
    }

    @Test
    @Transactional
    @Rollback(value = false)
    public void 식료품삭제() throws Exception {
        //given
        Grocery grocery = new Grocery();
        grocery.setGroceryName("김치");
        Long savedGroceryId = groceryService.addGrocery(grocery);

        //when
        groceryService.deleteById(savedGroceryId);

        //then
        em.clear();
        Grocery deletedGrocery = groceryService.findOne(savedGroceryId);
        assertNull(deletedGrocery);
    }
}
