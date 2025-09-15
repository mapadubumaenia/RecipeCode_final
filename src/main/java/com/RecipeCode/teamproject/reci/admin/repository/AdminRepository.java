package com.RecipeCode.teamproject.reci.admin.repository;

import com.RecipeCode.teamproject.reci.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin,String> {

}
