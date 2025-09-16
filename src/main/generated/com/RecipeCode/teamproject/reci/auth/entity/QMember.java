package com.RecipeCode.teamproject.reci.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 351108636L;

    public static final QMember member = new QMember("member1");

    public final com.RecipeCode.teamproject.common.QBaseTimeEntity _super = new com.RecipeCode.teamproject.common.QBaseTimeEntity(this);

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

    public final StringPath userBlog = createString("userBlog");

    public final StringPath userEmail = createString("userEmail");

    public final StringPath userId = createString("userId");

    public final StringPath userInsta = createString("userInsta");

    public final StringPath userInterestTag = createString("userInterestTag");

    public final StringPath userIntroduce = createString("userIntroduce");

    public final StringPath userLocation = createString("userLocation");

    public final StringPath userWebsite = createString("userWebsite");

    public final StringPath userYoutube = createString("userYoutube");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

