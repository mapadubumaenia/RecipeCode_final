package com.RecipeCode.teamproject.reci.faq.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFaq is a Querydsl query type for Faq
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFaq extends EntityPathBase<Faq> {

    private static final long serialVersionUID = -1831612544L;

    public static final QFaq faq = new QFaq("faq");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    public final StringPath faq_answer = createString("faq_answer");

    public final NumberPath<Long> faq_num = createNumber("faq_num", Long.class);

    public final StringPath faq_question = createString("faq_question");

    public final StringPath faq_tag = createString("faq_tag");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QFaq(String variable) {
        super(Faq.class, forVariable(variable));
    }

    public QFaq(Path<? extends Faq> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFaq(PathMetadata metadata) {
        super(Faq.class, metadata);
    }

}

