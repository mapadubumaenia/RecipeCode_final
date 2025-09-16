package com.RecipeCode.teamproject.reci.admin.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAdmin is a Querydsl query type for Admin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdmin extends EntityPathBase<Admin> {

    private static final long serialVersionUID = -1064028576L;

    public static final QAdmin admin = new QAdmin("admin");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

    public final StringPath adminBlog = createString("adminBlog");

    public final StringPath adminEmail = createString("adminEmail");

    public final StringPath adminId = createString("adminId");

    public final StringPath adminInsta = createString("adminInsta");

    public final StringPath adminInterestTag = createString("adminInterestTag");

    public final StringPath adminIntroduce = createString("adminIntroduce");

    public final StringPath adminLocation = createString("adminLocation");

    public final StringPath adminWebsite = createString("adminWebsite");

    public final StringPath adminYoutube = createString("adminYoutube");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> insertTime = _super.insertTime;

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final ArrayPath<byte[], Byte> profileImage = createArray("profileImage", byte[].class);

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final StringPath profileStatus = createString("profileStatus");

    public final StringPath role = createString("role");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QAdmin(String variable) {
        super(Admin.class, forVariable(variable));
    }

    public QAdmin(Path<? extends Admin> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdmin(PathMetadata metadata) {
        super(Admin.class, metadata);
    }

}

