<<<<<<<< HEAD:src/main/java/com/RecipeCode/teamproject/reci/auth/repository/MemberRepository.java
package com.RecipeCode.teamproject.reci.auth.repository;

========
package com.RecipeCode.teamproject.reci.feed.comments.repository;
>>>>>>>> main:src/main/java/com/RecipeCode/teamproject/reci/feed/comments/repository/MemberRepository.java

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
}
