//package com.justdoit.eyefridge.repository;
//
//import com.justdoit.eyefridge.domain.Grocery;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@SpringBootTest
//class GroceryReositoryTest {
//
//    @Autowired GroceryReository groceryReository;
//
//    @Test
//    @Transactional
//    //db에 데이터 남기려면 false
//    @Rollback(value = false)
//    public void testGrocery() throws Exception {
//        //given
//        Grocery grocery = new Grocery();
//        grocery.setGroceryName("김치");
//        //when
//        String saveGroceryName = groceryReository.save(grocery);
//        List<Grocery> findGrocery = groceryReository.findByName(saveGroceryName);
//
//        //then
//        Assertions.assertThat(findGrocery.get(0).getGroceryName()).isEqualTo(grocery.getGroceryName());
////        Assertions.assertThat(findGrocery).isEqualTo(grocery);
//    }
//}