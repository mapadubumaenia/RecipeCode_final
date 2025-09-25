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
        document.querySelectorAll("#interestTagHidden input").forEach(input => {
            if (input.value.toLowerCase() === norm) {
                input.remove();
            }
        });

        tagSpan.remove();
    });
});


//실행 취소
function edit_cancel() {
    history.back(); // 브라우저 뒤로가기
}

//계정삭제
function delete_account() {
    if (!confirm("정말로 계정을 삭제하시겠습니까?")) {
        return;
    }

    fetch("/mypage/delete", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (response.ok) {
                alert("계정이 탈퇴 처리되었습니다.");
                window.location.href = "/";
            } else {
                return response.text().then(text => { throw new Error(text) });
            }
        })
        .catch(error => {
            alert("계정 삭제 중 오류 발생: " + error.message);
        });
}

// 비밀번호 보기 토글
$$(".toggle").forEach(btn=>{
    btn.addEventListener('click', ()=>{
        const input = document.querySelector(btn.dataset.target);
        if(!input) return;
        input.type = input.type === 'password' ? 'text' : 'password';
    });
});

// 비밀번호 강도 (아주 단순화)
const pw = $('#pw');
const pw2 = $('#pw2');
const formError = $('#formError');
const submitBtn = $('#savebtn');
const pwBar = $('#pwBar');

function scorePassword(v){
    let s = 0;
    if(v.length >= 8) s += 30;
    if(/[A-Z]/.test(v)) s += 20;
    if(/[a-z]/.test(v)) s += 20;
    if(/[0-9]/.test(v)) s += 20;
    if(/[^A-Za-z0-9]/.test(v)) s += 10;
    return Math.min(s, 100);
}

function renderPwBar(){
    const sc = scorePassword(pw.value);
    pwBar.style.width = sc + '%';
    pwBar.style.background = sc >= 70 ? '#16a34a' : (sc >= 40 ? '#d97706' : '#dc2626');
}

function renderPwMatch(){
    const hint = $('#pwMatchHint');
    if(!pw2.value){ hint.textContent = ''; return; }
    hint.textContent = pw.value === pw2.value ? '비밀번호가 일치합니다.' : '비밀번호가 일치하지 않습니다.';
    hint.style.color = pw.value === pw2.value ? '#16a34a' : '#dc2626';
}


function validateAll(){
    const hasPw = pw.value.length > 0;
    const pwOk = !hasPw || pw.value.length >= 8;
    const matchOk =!hasPw || (pw.value === pw2.value);
    const allOk = pwOk && matchOk
    submitBtn.disabled = !allOk;
    formError.textContent = allOk ? '' : '비밀번호가 8자리 미만이거나 일치하지 않습니다.';
}

// 초기 상태
validateAll(); renderPwBar();

['input','change'].forEach(ev=>{
    pw.addEventListener(ev, ()=>{ renderPwBar(); validateAll(); });
    pw2.addEventListener(ev, ()=>{ renderPwMatch(); validateAll(); });
});

