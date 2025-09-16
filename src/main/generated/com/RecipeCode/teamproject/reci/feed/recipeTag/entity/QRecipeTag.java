package com.RecipeCode.teamproject.reci.feed.recipeTag.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeTag is a Querydsl query type for RecipeTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeTag extends EntityPathBase<RecipeTag> {

    private static final long serialVersionUID = -1302440458L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeTag recipeTag = new QRecipeTag("recipeTag");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    public final com.RecipeCode.teamproject.reci.feed.recipes.entity.QRecipes recipes;

    public final NumberPath<Long> recipeTagId = createNumber("recipeTagId", Long.class);

    public final com.RecipeCode.teamproject.reci.tag.entity.QTag tag;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QRecipeTag(String variable) {
        this(RecipeTag.class, forVariable(variable), INITS);
    }

    public QRecipeTag(Path<? extends RecipeTag> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeTag(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeTag(PathMetadata metadata, PathInits inits) {
        this(RecipeTag.class, metadata, inits);
    }

    public QRecipeTag(Class<? extends RecipeTag> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipes = inits.isInitialized("recipes") ? new com.RecipeCode.teamproject.reci.feed.recipes.entity.QRecipes(forProperty("recipes"), inits.get("recipes")) : null;
        this.tag = inits.isInitialized("tag") ? new com.RecipeCode.teamproject.reci.tag.entity.QTag(forProperty("tag")) : null;
    }

}

