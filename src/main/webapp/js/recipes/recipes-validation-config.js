// /js/recipes/recipes-validation-config.js
(function ($) {
  // ====== 커스텀 메서드 ======
  $.validator.addMethod(
      "youtubeUrl",
      function (value, el) {
        if (!value) return true; // required는 따로 처리
        const re =
            /(youtu\.be\/[A-Za-z0-9_-]{6,}|youtube\.com\/(watch\?v=|shorts\/)[A-Za-z0-9_-]{6,})/i;
        return re.test(value);
      },
      "유효한 유튜브 주소를 입력하세요."
  );

  // ====== 유틸: 동적 필드 규칙 바인딩 ======
  function bindIngredientRules($scope) {
    ($scope || $(document))
        .find("[name$='.ingredientName']")
        .each(function () {
          $(this).rules("add", {
            required: true,
            messages: { required: "재료명을 입력하세요." },
          });
        });
  }

  function bindStepRules($scope) {
    ($scope || $(document))
        .find("[name$='.stepExplain']")
        .each(function () {
          $(this).rules("add", {
            required: {
              depends: function () {
                return $("#recipeType").val() === "IMAGE";
              },
            },
            maxlength: 1000,
            messages: {
              required: "조리 단계 설명을 입력하세요.",
              maxlength: "최대 {0}자까지 입력",
            },
          });
        });
  }

  // ====== 초기화 ======
  $(function () {
    const $form = $("#recipeForm");
    if ($form.length === 0) return;

    const validator = $form.validate({
      ignore: [], // hidden도 검증(대표이미지/타입 전환용)
      errorClass: "fld-error",
      validClass: "fld-valid",
      errorElement: "em",
      highlight: function (el) {
        $(el).addClass("has-error");
      },
      unhighlight: function (el) {
        $(el).removeClass("has-error");
      },
      errorPlacement: function (error, el) {
        const $row = $(el).closest(".row");
        if ($row.length) error.appendTo($row);
        else error.insertAfter(el);
      },

      // ----- 고정 필드 규칙 -----
      rules: {
        recipeTitle: { required: true, minlength: 2, maxlength: 80 },
        recipeCategory: { required: true },
        cookingTime: { required: true, digits: true, min: 1, max: 1440 },
        difficulty: { required: true },
      },
      messages: {
        recipeTitle: {
          required: "제목을 입력하세요.",
          minlength: "최소 {0}자 이상",
          maxlength: "최대 {0}자까지",
        },
        recipeCategory: { required: "카테고리를 선택하세요." },
        cookingTime: {
          required: "조리 시간을 입력하세요.",
          digits: "숫자만 입력",
          min: "1분 이상",
          max: "하루(1440분) 이내",
        },
        difficulty: { required: "난이도를 선택하세요." },
      },

      // ----- 제출 직전 추가 검사(최소 재료/단계) -----
      submitHandler: function (form) {
        const type = $("#recipeType").val();

        const ingCount = $("[name$='.ingredientName']")
            .filter(function () {
              return $(this).val().trim().length > 0;
            })
            .length;
        if (ingCount === 0) {
          alert("재료를 최소 1개 입력하세요.");
          return false;
        }

        if (type === "IMAGE") {
          const stepCount = $("[name$='.stepExplain']")
              .filter(function () {
                return $(this).val().trim().length > 0;
              })
              .length;
          if (stepCount === 0) {
            alert("조리 단계를 최소 1개 입력하세요.");
            return false;
          }
        }

        form.submit();
      },
    });

    // ----- 타입별 필드 규칙 -----
    // VIDEO: URL 필수 + 유튜브 패턴
    $("#videoUrl").rules("add", {
      required: {
        depends: function () {
          return $("#recipeType").val() === "VIDEO";
        },
      },
      youtubeUrl: true,
      messages: { required: "동영상 URL을 입력하세요." },
    });

    // IMAGE: 대표이미지 필수(수정모드에서 기존 썸네일 있으면 면제)
    $("#thumb").rules("add", {
      required: {
        depends: function () {
          const isImage = $("#recipeType").val() === "IMAGE";
          const hasPreview = !!$("#thumbPreview").attr("src");
          return isImage && !hasPreview;
        },
      },
      // jQuery Validate의 accept보단 확장자 검사 권장
      extension: "png|jpg|jpeg|gif|webp",
      messages: {
        required: "대표 이미지를 업로드하세요.",
        extension: "이미지 파일(png, jpg, jpeg, gif, webp)만 업로드",
      },
    });

    // ----- 동적 필드 초기/재바인딩 -----
    bindIngredientRules($form);
    bindStepRules($form);

    // 외부 스크립트(행 추가 로직)에서 재바인딩 호출할 수 있도록 공개
    window.RecipesValidation = {
      rebindIngredients: function ($scope) {
        bindIngredientRules($scope);
      },
      rebindSteps: function ($scope) {
        bindStepRules($scope);
      },
      revalidateTypeFields: function () {
        validator.element("#videoUrl");
        validator.element("#thumb");
        $("[name$='.stepExplain']").each(function () {
          validator.element(this);
        });
      },
    };
  });
})(jQuery);