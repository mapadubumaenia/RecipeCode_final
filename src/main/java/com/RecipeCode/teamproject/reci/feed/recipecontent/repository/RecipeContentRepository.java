package com.RecipeCode.teamproject.reci.feed.recipecontent.repository;

import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeContentRepository extends JpaRepository<RecipeContent, Long> {
}
