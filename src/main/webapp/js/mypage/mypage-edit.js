// 공통 유틸
const $ = (s, el= document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];

// 프로필이미지
$("#avatar")?.addEventListener("change", (e) =>{
    const file = e.target.files?.[0];
    const img = $("#avatarPreview");
    if(!file) {
        img?.classList.add("hidden");
        return;
    }
    const url = URL.createObjectURL(file);
    img.src = url;
    img.classList.remove("hidden");
    $(".avatar-uploader .ph").classList.add("hidden");
});
//태
document.addEventListener("DOMContentLoaded", () => {
    const tagInput = document.querySelector("#interestTagInput");
    const tagList = document.querySelector("#tagList");
    const addTagBtn = document.querySelector("#addTagBtn");
    const hiddenContainer = document.querySelector("#interestTagHidden");

    // 태그 추가 함수
    function addTag(text) {
        const label = text.trim().replace(/^#+/, "");
        if (!label) return;

        const norm = label.toLowerCase(); // 소문자 기준

        // 중복 방지
        if ([...tagList.children].some((el) => el.dataset.tag === norm)) {
            tagInput.value = "";
            tagInput.focus();
            return;
        }

        // 화면 표시
        const el = document.createElement("span");
        el.className = "tag";
        el.dataset.tag = norm;
        el.innerHTML = `<span>#${label}</span><span class="x" title="삭제">×</span>`;
        tagList.appendChild(el);

        // hidden input 추가
        const idx = hiddenContainer.children.length;
        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = `interestTags[${idx}].tag`;
        hiddenInput.value = label;
        hiddenContainer.appendChild(hiddenInput);

        console.log("addTag 끝! value 비움 직전:", tagInput.value);
        tagInput.value = "";
        tagInput.focus();
        console.log("비운 후:", tagInput.value);
    }

    // Enter 입력
    tagInput?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            addTag(tagInput.value);
        }
    });

    // 버튼 클릭
    addTagBtn?.addEventListener("click", () => addTag(tagInput.value));

    // 삭제
    tagList?.addEventListener("click", (e) => {
        const x = e.target.closest(".x");
        if (!x) return;

        const tagSpan = x.parentElement;
        const norm = tagSpan.dataset.tag;

        // hidden input 삭제
        [...hiddenContainer.children].forEach((input) => {
            if (input.value.toLowerCase() === norm) {
                hiddenContainer.removeChild(input);
            }
        });

        tagSpan.remove();
    });
});


// 태그 삭제
document.addEventListener("click", (e) => {
    const x = e.target.closest(".x");
    if (!x) return;

    const tagSpan = x.parentElement;
    const norm = tagSpan.dataset.tag.toLowerCase();// 태그 id를 data-id에 넣어둬야 함

    document.querySelectorAll("#interestTagHidden input").forEach(input => {
        if (input.value.toLowerCase() === norm) {
            input.remove();
        }
    });

    // 화면에서 제거
    tagSpan.remove();
});

//실행 취소
function edit_cancel() {
    history.back(); // 브라우저 뒤로가기
}

//계정삭제
function edit_cancel() {
    history.back(); // 브라우저 뒤로가기
}