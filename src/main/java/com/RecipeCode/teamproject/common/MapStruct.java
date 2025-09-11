package com.RecipeCode.teamproject.common;


import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.recipeTag.entity.RecipeTag;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // null 제외 기능(update 시 사용)
)
public interface MapStruct {
    //  TODO: Faq
    FaqDto toDto(Faq faq);

    Faq toEntity(FaqDto faqDto);

    // TODO: 수정 시 사용: dirty checking 기능(save() 없이 수정 가능)
    void updateFromDto(FaqDto faqDto, @MappingTarget Faq faq);

    //  TODO: RecipeTag
    RecipeTagDto toDto(RecipeTag recipeTag);

    RecipeTag toEntity(RecipeTagDto recipeTagDto);

}
