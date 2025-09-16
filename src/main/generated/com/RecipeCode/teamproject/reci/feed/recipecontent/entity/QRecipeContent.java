package com.RecipeCode.teamproject.reci.feed.recipecontent.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipeContent is a Querydsl query type for RecipeContent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeContent extends EntityPathBase<RecipeContent> {

    private static final long serialVersionUID = -541561674L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipeContent recipeContent = new QRecipeContent("recipeContent");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    public final ArrayPath<byte[], Byte> recipeImage = createArray("recipeImage", byte[].class);

    public final StringPath recipeImageUrl = createString("recipeImageUrl");

    public final com.RecipeCode.teamproject.reci.feed.recipes.entity.QRecipes recipes;

    public final StringPath stepExplain = createString("stepExplain");

    public final NumberPath<Long> stepId = createNumber("stepId", Long.class);

    public final NumberPath<Long> stepOrder = createNumber("stepOrder", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QRecipeContent(String variable) {
        this(RecipeContent.class, forVariable(variable), INITS);
    }

    public QRecipeContent(Path<? extends RecipeContent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipeContent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipeContent(PathMetadata metadata, PathInits inits) {
        this(RecipeContent.class, metadata, inits);
    }

    public QRecipeContent(Class<? extends RecipeContent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipes = inits.isInitialized("recipes") ? new com.RecipeCode.teamproject.reci.feed.recipes.entity.QRecipes(forProperty("recipes"), inits.get("recipes")) : null;
    }

}

