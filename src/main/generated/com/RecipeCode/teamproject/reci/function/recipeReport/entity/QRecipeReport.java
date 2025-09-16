package com.RecipeCode.teamproject.reci.function.recipeReport.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecipeReport is a Querydsl query type for RecipeReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipeReport extends EntityPathBase<RecipeReport> {

    private static final long serialVersionUID = -2012344550L;

    public static final QRecipeReport recipeReport = new QRecipeReport("recipeReport");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    public final NumberPath<Long> reportId = createNumber("reportId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QRecipeReport(String variable) {
        super(RecipeReport.class, forVariable(variable));
    }

    public QRecipeReport(Path<? extends RecipeReport> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecipeReport(PathMetadata metadata) {
        super(RecipeReport.class, metadata);
    }

}

