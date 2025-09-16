package com.RecipeCode.teamproject.reci.feed.recipes.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecipes is a Querydsl query type for Recipes
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipes extends EntityPathBase<Recipes> {

    private static final long serialVersionUID = -98795498L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecipes recipes = new QRecipes("recipes");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    public final NumberPath<Long> commentCount = createNumber("commentCount", Long.class);

    public final NumberPath<Long> cookingTime = createNumber("cookingTime", Long.class);

    public final StringPath difficulty = createString("difficulty");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final com.RecipeCode.teamproject.reci.auth.entity.QMember member;

    public final StringPath postStatus = createString("postStatus");

    public final StringPath recipeCategory = createString("recipeCategory");

    public final StringPath recipeIntro = createString("recipeIntro");

    public final ListPath<com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag, com.RecipeCode.teamproject.reci.feed.recipeTag.entity.QRecipeTag> recipeTag = this.<com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag, com.RecipeCode.teamproject.reci.feed.recipeTag.entity.QRecipeTag>createList("recipeTag", com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag.class, com.RecipeCode.teamproject.reci.feed.recipeTag.entity.QRecipeTag.class, PathInits.DIRECT2);

    public final StringPath recipeTitle = createString("recipeTitle");

    public final StringPath recipeType = createString("recipeType");

    public final NumberPath<Long> reportCount = createNumber("reportCount", Long.class);

    public final ArrayPath<byte[], Byte> thumbnail = createArray("thumbnail", byte[].class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public final StringPath uuid = createString("uuid");

    public final StringPath videoText = createString("videoText");

    public final StringPath videoUrl = createString("videoUrl");

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public QRecipes(String variable) {
        this(Recipes.class, forVariable(variable), INITS);
    }

    public QRecipes(Path<? extends Recipes> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecipes(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecipes(PathMetadata metadata, PathInits inits) {
        this(Recipes.class, metadata, inits);
    }

    public QRecipes(Class<? extends Recipes> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.RecipeCode.teamproject.reci.auth.entity.QMember(forProperty("member")) : null;
    }

}

