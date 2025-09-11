package com.RecipeCode.teamproject.reci.recipes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Inquiry {

    @Column(length = 100)
    private String name;
    @Column(length = 20)
    private String amount;
}
