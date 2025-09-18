// 본문 "더보기" 토글
(function(){
    const box = document.getElementById('postDesc');
    const btn = document.getElementById('btnToggleDesc');
    btn?.addEventListener('click', () => box.classList.toggle('expanded'));
})();

// 이미지/텍스트 슬라이더
(function(){
    const imgTrack = document.getElementById("imgSlides");
    const txtTrack = document.getElementById("textSlides");
    const sliderRoot = document.querySelector(".step-slider");
    if (!imgTrack || !txtTrack || !sliderRoot) return;

    const slideCount = Math.min(
        imgTrack.querySelectorAll(".slide").length,
        txtTrack.querySelectorAll(".slide").length
    );
    let index = 0;
    function trackWidth(){ return sliderRoot.getBoundingClientRect().width; }
    function setTranslate(px){
        imgTrack.style.transform = `translateX(${px}px)`;
        txtTrack.style.transform = `translateX(${px}px)`;
    }
    function snapTo(i){
        if (slideCount === 0) return;
        index = (i + slideCount) % slideCount;
        const x = -index * trackWidth();
        imgTrack.classList.remove("no-trans");
        txtTrack.classList.remove("no-trans");
        setTranslate(x);
    }
    window.addEventListener("resize", () => snapTo(index));
    document.querySelector(".prev")?.addEventListener("click", () => snapTo(index - 1));
    document.querySelector(".next")?.addEventListener("click", () => snapTo(index + 1));
    window.addEventListener("keydown", (e) => {
        if (e.key === "ArrowLeft") snapTo(index - 1);
        if (e.key === "ArrowRight") snapTo(index + 1);
    });
    snapTo(0);
})();

// 좋아요/댓글 AJAX는 추후 여기서 fetch 붙이면 됨 (data-recipe-uuid 이용)

(function(){
    const btnLike = document.getElementById("btnLike");
    const likeCnt = document.getElementById("likeCnt");
    const recipeBox = document.querySelector(".container[data-recipe-uuid]");
    if (!btnLike || !likeCnt || !recipeBox) return;

    const recipeUuid = recipeBox.dataset.recipeUuid;

    btnLike.addEventListener("click", async () => {
        try {
            const resp = await fetch(`${ctx}/recipes/${recipeUuid}/like`, {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });
            if (!resp.ok) throw new Error("서버 오류");
            const data = await resp.json();

            // 서버에서 내려준 dto 값 반영
            likeCnt.textContent = data.likesCount;
            if (data.liked) {
                btnLike.classList.add("active"); // CSS로 하트 색 변환
            } else {
                btnLike.classList.remove("active");
            }
        } catch (err) {
            console.error(err);
            alert("좋아요 처리 중 오류 발생 😢");
        }
    });
})();

// 신고 모달 열기/닫기, 서버 전송
document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("reportModal");
    const btnReport = document.getElementById("btnReport");
    const btnClose = document.getElementById("btnClose");
    const form = document.getElementById("reportForm");

    // 🚩 신고 버튼 → 모달 열기
    btnReport?.addEventListener("click", () => modal.hidden = false);
    // 취소 버튼 → 모달 닫기
    btnClose?.addEventListener("click", () => modal.hidden = true);

    // 폼 제출 → 서버에 전송
    form?.addEventListener("submit", async (e) => {
        e.preventDefault();
        const formData = new FormData(form);

        try {
            const res = await fetch(`${ctx}/report/add`, {
                method: "POST",
                body: new URLSearchParams(formData)
            });
            if (!res.ok) throw new Error("신고 실패");

            const data = await res.json(); // 컨트롤러 JSON 응답 파싱

            if (data.status === "ok") {
                alert(data.message); // "신고가 접수되었습니다."
                modal.hidden = true;
            } else {
                alert(data.message); // "로그인이 필요합니다." 등
                modal.hidden = true; // 필요시 닫지 않고 로그인 페이지로 유도 가능
            }
        } catch (err) {
            console.error(err);
            alert("신고 중 오류가 발생했습니다.");
        }
    });
});
