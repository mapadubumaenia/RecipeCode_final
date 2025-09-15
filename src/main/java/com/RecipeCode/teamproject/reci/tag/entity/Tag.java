package com.RecipeCode.teamproject.reci.tag.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.common.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "TAG")
@SequenceGenerator(name = "TAG_KEY_JPA",
                   sequenceName = "TAG_KEY",
                   initialValue = 1,
                   allocationSize = 1)
@EqualsAndHashCode(of = "tagId", callSuper = false)
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "TAG_KEY_JPA")
    private Long tagId;             // PK
    private String tag;
//    기본값(null) -> DB에 "N"
    @Convert(converter = BooleanToYNConverter.class)
    private boolean deleted;
}
