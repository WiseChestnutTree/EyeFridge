package com.justdoit.eyefridge.repository;

import com.justdoit.eyefridge.domain.Grocery;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

@Repository
public class GroceryReository {

    @PersistenceContext
    private EntityManager em;

    /*
    영수증 등록
        따로 왜냐면 영수증에서 이름과 수량만 따로 갖고옴
        임의로 유통기한과 위치를 지정해야함
    */

    //식료품 등록
    public void save(Grocery grocery) {
        if (grocery.getId() == null) {
            em.persist(grocery);
        } else {
            em.merge(grocery);
        }
    }

    //repositoryTest용
//    public String save(Grocery grocery) {
//        em.persist(grocery);
//        return grocery.getGroceryName();
//    }

    //식료품 id로 검색
    public Grocery findOne(Long id) {
        return em.find(Grocery.class, id);
    }

//    식료품 이름을 가지고 조회
    public List<Grocery> findByName(String groceryName) {
        return em.createQuery("select g from Grocery g where g.groceryName = :groceryName", Grocery.class)
                .setParameter("groceryName", groceryName)
                .getResultList();
    }

    //    식료품 위치를 가지고 조회
//    public int findByLocation(int location) {
//        return em.createQuery("select g from Grocery g where g.location = :location", Grocery.class)
//                .setParameter("location", location)
//                .getResultList();
//    }

    //식료품 전체 조회
    public List<Grocery> findAll(){
        return em.createQuery("select g from Grocery g", Grocery.class)
                .getResultList();
    }

    //유통기한 2일 남은 식료품 조회
    public List<Grocery> findExpirationDate2(){
        return em.createQuery("select g from Grocery g WHERE g.expirationDate IS NOT NULL AND g.expirationDate <= :expirationDate AND g.state = 0",
                        Grocery.class)
                .setParameter("expirationDate", LocalDate.now().plusDays(2))
                .getResultList();
    }

    //식료품 삭제
    public void deleteById(Long id) {
        em.createQuery("delete from Grocery g where g.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
