package com.RecipeCode.teamproject.reci.auth.notisetting.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.notisetting.entity.NotiSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotiSettingRepository extends JpaRepository<NotiSetting, Long> {
    List<NotiSetting> findByMember(Member member);
}
