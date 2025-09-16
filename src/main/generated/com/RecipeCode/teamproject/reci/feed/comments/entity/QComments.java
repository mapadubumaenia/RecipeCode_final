package com.RecipeCode.teamproject.reci.feed.comments.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComments is a Querydsl query type for Comments
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComments extends EntityPathBase<Comments> {

    private static final long serialVersionUID = -913407688L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComments comments = new QComments("comments");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    public final ListPath<Comments, QComments> children = this.<Comments, QComments>createList("children", Comments.class, QComments.class, PathInits.DIRECT2);

    public final StringPath commentsContent = createString("commentsContent");

    public final NumberPath<Long> commentsId = createNumber("commentsId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final com.RecipeCode.teamproject.reci.auth.entity.QMember member;

    public final QComments parentId;

    public final com.RecipeCode.teamproject.reci.feed.recipes.entity.QRecipes recipes;

    public final NumberPath<Long> reportCount = createNumber("reportCount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QComments(String variable) {
        this(Comments.class, forVariable(variable), INITS);
    }

    public QComments(Path<? extends Comments> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComments(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComments(PathMetadata metadata, PathInits inits) {
        this(Comments.class, metadata, inits);
    }

    public QComments(Class<? extends Comments> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.RecipeCode.teamproject.reci.auth.entity.QMember(forProperty("member")) : null;
        this.parentId = inits.isInitialized("parentId") ? new QComments(forProperty("parentId"), inits.get("parentId")) : null;
        this.recipes = inits.isInitialized("recipes") ? new com.RecipeCode.teamproject.reci.feed.recipes.entity.QRecipes(forProperty("recipes"), inits.get("recipes")) : null;
    }

}

