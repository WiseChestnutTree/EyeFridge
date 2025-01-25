package com.justdoit.eyefridge.domain;

import com.justdoit.eyefridge.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Grocery {
    @Id
    @GeneratedValue
    @Column(name = "grocery_id")
    private Long id;

    private String groceryName;

    private int quantity;

    private LocalDate expirationDate;

    private int location;

    @ColumnDefault("0")
    private int state;

    /*
        유통기한 자동 설정 남은 유통기한 계산할 때 사용
        영수증이 찍힌 당일에 샀으니까
        LocalDate.now()이용
    */
}
