/* ===== 공통 유틸 ===== */
const $ = (s, el = document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];

/* ===== 대표 이미지 미리보기 ===== */
$("#thumb")?.addEventListener("change", (e) => {
    const file = e.target.files?.[0];
    const img = $("#thumbPreview");
    if (!file) {
        img?.classList.add("hidden");
        return;
    }
    const url = URL.createObjectURL(file);
    img.src = url;
    img.classList.remove("hidden");
    $(".thumb .ph").classList.add("hidden");
});

/* ===== 태그 ===== */
const tagInput = $("#tagInput");
const tagList = $("#tagList");

function addTag(text) {
    const label = text.trim().replace(/^#+/, "");
    if (!label) return;
    // 중복 방지
    if ([...tagList.children].some((el) => el.dataset.tag === label)) return;

    const el = document.createElement("span");
    el.className = "tag";
    el.dataset.tag = label;
    el.innerHTML = `<span>#${label}</span><span class="x" title="삭제">×</span>`;
    tagList.appendChild(el);
    tagInput.value = "";
}
tagInput?.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        e.preventDefault();
        addTag(tagInput.value);
    }
});
$("#addTagBtn")?.addEventListener("click", () => addTag(tagInput.value));
tagList?.addEventListener("click", (e) => {
    const x = e.target.closest(".x");
    if (!x) return;
    x.parentElement.remove();
});

/* ===== 재료 ===== */
$("#addIng")?.addEventListener("click", () => {
    const row = document.createElement("div");
    row.className = "item-row";
    row.innerHTML = `
      <input class="input" placeholder="재료명" />
      <input class="input" placeholder="분량" />
      <button type="button" class="btn icon del">🗑</button>`;
    $("#ingredients").appendChild(row);
});
$("#ingredients")?.addEventListener("click", (e) => {
    if (e.target.closest(".del")) e.target.closest(".item-row").remove();
});

function updateMoveButtons() {
    [...steps.children].forEach((step, i, arr) => {
        const up = step.querySelector(".move-up");
        const top = step.querySelector(".move-top");
        const down = step.querySelector(".move-down");
        const bot = step.querySelector(".move-bottom");
        if (up) up.disabled = i === 0;
        if (top) top.disabled = i === 0;
        if (down) down.disabled = i === arr.length - 1;
        if (bot) bot.disabled = i === arr.length - 1;
    });
}

/* ===== 단계 ===== */
const steps = $("#steps");

const makeStep = (idx = steps.children.length + 1) => {
    const wrap = document.createElement("article");
    wrap.className = "step";
    wrap.innerHTML = `
  <div class="step-head">
    <div class="step-title">Step <span class="no">${idx}</span></div>
    <div style="display:flex;gap:8px">
      <button type="button" class="btn icon move-top"    title="맨 위로">⤒</button>
      <button type="button" class="btn icon move-up"     title="위로">↑</button>
      <button type="button" class="btn icon move-down"   title="아래로">↓</button>
      <button type="button" class="btn icon move-bottom" title="맨 아래로">⤓</button>
      <button type="button" class="btn icon add-below"   title="아래에 단계 추가">＋</button>
      <button type="button" class="btn icon danger del"  title="삭제">🗑</button>
    </div>
  </div>
  <div class="step-body">
    <label class="upload">
      <input type="file" accept="image/*" />
      <span class="ph">이미지 업로드</span>
      <img class="hidden" alt="">
    </label>
    <textarea placeholder="설명(예: 팬에 올리브오일을 두르고 마늘을 볶습니다.)"></textarea>
  </div>`;
    return wrap; /* ✅ 반드시 반환 */
};

/* Step 번호 다시 매기기 */
function renumber() {
    $$(".step .no", steps).forEach((el, i) => (el.textContent = String(i + 1)));
    updateMoveButtons();
}

/* 초기 두 개 샘플 */
steps.appendChild(makeStep(1));
steps.appendChild(makeStep(2));

/* 삭제 */
steps.addEventListener("click", (e) => {
    if (e.target.closest(".del")) {
        e.target.closest(".step").remove();
        renumber();
    }
});

/* 순서/추가 버튼 */
steps.addEventListener("click", (e) => {
    const step = e.target.closest(".step");
    if (!step) return;

    if (e.target.closest(".move-up")) {
        const prev = step.previousElementSibling;
        if (prev) steps.insertBefore(step, prev);
        renumber();
    } else if (e.target.closest(".move-down")) {
        const next = step.nextElementSibling;
        if (next) steps.insertBefore(next, step);
        renumber();
    } else if (e.target.closest(".move-top")) {
        const first = steps.firstElementChild;
        if (first && first !== step) steps.insertBefore(step, first);
        renumber();
    } else if (e.target.closest(".move-bottom")) {
        steps.appendChild(step);
        renumber();
    } else if (e.target.closest(".add-below")) {
        const newStep = makeStep();
        steps.insertBefore(newStep, step.nextElementSibling);
        renumber();
        newStep.scrollIntoView({ behavior: "smooth", block: "center" });
    }
});

/* 상단 추가 */
$("#addStep")?.addEventListener("click", () => {
    steps.appendChild(makeStep());
    renumber();
    steps.lastElementChild?.scrollIntoView({ behavior: "smooth", block: "center" });
});

/* ===== 단계 이미지 미리보기 ===== */
steps.addEventListener("change", (e) => {
    const fileInput = e.target.matches('input[type="file"]')
        ? e.target
        : e.target.closest(".upload")?.querySelector('input[type="file"]');
    if (!fileInput) return;

    const label = fileInput.closest(".upload");
    const img = label.querySelector("img");
    const ph = label.querySelector(".ph");
    const file = fileInput.files && fileInput.files[0];

    if (!file) {
        img.classList.add("hidden");
        img.removeAttribute("src");
        ph.classList.remove("hidden");
        return;
    }

    const url = URL.createObjectURL(file);
    img.src = url;
    img.onload = () => URL.revokeObjectURL(url);
    img.classList.remove("hidden");
    ph.classList.add("hidden");
});

/* ===== 발행 ===== */
document.getElementById("publish")?.addEventListener("click", async () => {
    const dto = {
        recipeTitle: $("#title")?.value.trim() || "",
        recipeIntro: $("#subtitle")?.value.trim() || "",
        recipeCategory: $("#category")?.value || "",
        cookingTime: Number($("#time")?.value || 0),
        difficulty: $("#difficulty")?.value || "",
        postStatus: $("#isPublic")?.checked ? "PUBLIC" : "PRIVATE",
        tags: [...$("#tagList")?.children || []].map((t) => t.dataset.tag),
        ingredients: [...$("#ingredients")?.querySelectorAll(".item-row") || []].map((r, i) => {
            const [name, qty] = r.querySelectorAll("input");
            return {
                ingredientName: name?.value.trim() || "",
                ingredientAmount: qty?.value.trim() || "",
                sortOrder: i + 1   // ✅ 순서 부여
            };
        }),
        contents: [...document.querySelectorAll("#steps .step")].map((s, i) => {
            const txt = s.querySelector("textarea")?.value.trim() || "";
            return {
                stepExplain: txt,
                stepOrder: i + 1
                // recipeImageUrl은 서버가 채워줄 거라 제외
            };
        }),
    };

    console.log("최종 보낼 DTO", dto);
    console.log("JSON.stringify(dto)", JSON.stringify(dto));

    const formData = new FormData();
    formData.append("recipesDto", new Blob([JSON.stringify(dto)], { type: "application/json" }));

    const thumbFile = $("#thumb").files[0];
    if (thumbFile) formData.append("image", thumbFile);

    // ✅ 스텝 이미지 추가
    document.querySelectorAll("#steps .step input[type='file']").forEach((input,idx) => {
        if (input.files[0]) {
            console.log(`stepImages[${idx}] =`, input.files[0].name); // 디버깅용
            formData.append("stepImages", input.files[0]);
        }
    });

    // ✅ formData 안에 뭐가 들었는지 전체 확인
    console.log("=== formData 확인 ===");
    for (let pair of formData.entries()) {
        console.log(pair[0], pair[1]);
    }

    try {
        const res = await fetch(`${contextPath}/recipes`, {
            method: "POST",
            body: formData,
        });
        if (!res.ok) throw new Error("등록 실패");

        const data = await res.json();
        alert("등록 완료!");
        location.href = `${contextPath}/recipes/`+ data.uuid;
    } catch (err) {
        console.error(err);
        alert("에러 발생: " + err.message);
    }
});

/* ===== 최초 모드 ===== */
let recipeType = "image";
setRecipeType("image");

function setRecipeType(type) {
    recipeType = type;
    const tabImage = $("#tabImage");
    const tabVideo = $("#tabVideo");
    const imagePane = $("#imagePane");
    const videoPane = $("#videoPane");

    tabImage.classList.toggle("is-active", type === "image");
    tabImage.setAttribute("aria-selected", String(type === "image"));
    tabVideo.classList.toggle("is-active", type === "video");
    tabVideo.setAttribute("aria-selected", String(type === "video"));

    imagePane.classList.toggle("hidden", type !== "image");
    imagePane.setAttribute("aria-hidden", String(type !== "image"));
    videoPane.classList.toggle("hidden", type !== "video");
    videoPane.setAttribute("aria-hidden", String(type !== "video"));
}