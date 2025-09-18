

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
            const resp = await fetch(`${ctx}/api/recipes/${recipeUuid}/like`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" }
                    // 👉 [운영 시 다시 활성화]
                    // const csrfMeta = document.querySelector('meta[name="_csrf"]');
                    // const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
                    // const headers = { "Content-Type": "application/json" };
                    // if (csrfMeta && csrfHeaderMeta) {
                    //     headers[csrfHeaderMeta.content] = csrfMeta.content;
                    // }
                    // const resp = await fetch(`${ctx}/api/recipes/${recipeUuid}/like`, {
                    //     method: "POST",
                    //     headers
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

document.addEventListener("DOMContentLoaded", () => {
        // TODO: 댓글 디버깅 코드 추가 -- 여기서 시작
        const elRecipe = document.querySelector('.container[data-recipe-uuid]');
        const recipeUuid = elRecipe?.dataset.recipeUuid || '';
        console.log('recipeUuid', recipeUuid, 'el:', elRecipe);
        if (!recipeUuid) {
            console.error('recipeUuid가 비었음. html 확인:', elRecipe?.outerHTML);
            return;
        }
        // TODO: --------- 여기까지

        // const recipeUuid = document.querySelector(".container").dataset.recipeUuid;
        const cmtList = document.getElementById("cmtList");
        const btnCmtSubmit = document.getElementById("btnCmtSubmit");
        const cmtInput = document.getElementById("cmt");
        const btnCmtMore = document.getElementById("btnCmtMore");

        const COMMENTS_PAGE_SIZE = 5; // 한 번에 표시할 댓글 수
        let page = 0;
        let sort = "desc"; // 최신순


        // 날짜 포맷 함수
        function formatDate(dateString) {
            if (!dateString) return "";
            const date = new Date(dateString);
            return date.toLocaleString();
        }

        // 댓글 DOM 생성 (XSS 방지)
        function createCommentElem(c) {
            const div = document.createElement("div");
            div.className = "comment";
            div.dataset.commentsId = c.commentsId;

            // 댓글창
            const header = document.createElement("div");
            header.className = "comment-header";
            header.innerHTML = `<b>${c.userId}</b> • <span class="time">${formatDate(c.insertTime)}</span>`;

            // 댓글 내용
            const content = document.createElement("div");
            content.className = "comment-content";
            content.textContent = c.commentsContent;

            const actions = document.createElement("div");
            actions.className = "comment-actions";
            actions.innerHTML = `
            <button class="btnReply">답글</button>
            <button class="btnEdit">수정</button>
            <button class="btnDelete">삭제</button>
            <button class="btnLike">좋아요 (${c.likeCount || 0})</button>
            <button class="btnReport">신고 (${c.reportCount || 0})</button>
        `;

            // 댓글 div에 추가
            div.appendChild(header);
            div.appendChild(content);
            div.appendChild(actions);

            // 대댓
            const repliesDiv = document.createElement("div");
            repliesDiv.className = "replies";
            div.appendChild(repliesDiv);

            actions.querySelector(".btnReply").addEventListener("click", () => openReplyInput(c.commentsId, repliesDiv));
            actions.querySelector(".btnEdit").addEventListener("click", () => editComment(c.commentsId, content));
            actions.querySelector(".btnDelete").addEventListener("click", () => deleteComment(c.commentsId, div));
            actions.querySelector(".btnLike").addEventListener("click", () => likeComment(c.commentsId, actions.querySelector(".btnLike")));
            actions.querySelector(".btnReport").addEventListener("click", () => reportComment(c.commentsId, actions.querySelector(".btnReport")));

            // 대댓 항상 띄우기
            loadReplies(c.commentsId, repliesDiv);

            return div;
        }

        // 댓글 불러오기
        async function loadComments(reset = false) {
            try {
                if (reset) {
                    cmtList.innerHTML = "";
                    page = 0;
                }

                const res = await fetch(
                    // TODO : 디버깅 ----------------- 여기부터
                    `${ctx}/comments/${encodeURIComponent(recipeUuid)}?page=${page}&size=${COMMENTS_PAGE_SIZE}&sort=${sort}`);
                // TODO : ---------------------- 여기까지
                // `${ctx}/comments/${recipeUuid}?page=${page}&size=${COMMENTS_PAGE_SIZE}&sort=${sort}`);
                if (!res.ok) throw new Error("댓글 조회 실패");
                const data = await res.json();
                // TODO : 디버깅 ----------------- 여기부터
                if (!Array.isArray(data)) {
                    console.error('댓글 응답 형식이 배열이 아님:', data)
                    return;
                }

                // TODO : ---------------------- 여기까지
                data.forEach(c => {
                    const commentElem = createCommentElem(c);
                    cmtList.appendChild(commentElem);
                });

                // 더보기 버튼 표시 여부
                btnCmtMore.style.display = data.length === COMMENTS_PAGE_SIZE ? "block" : "none";
                page++; // 다음 페이지 준비
            } catch (e) {
                console.error(e);
            }
        }

        // 댓글 작성
        btnCmtSubmit.addEventListener("click", async () => {
            // 비어있으면 경고
            const content = cmtInput.value.trim();
            if (!content) return alert("댓글 내용을 입력해주세요.");

            try {
                const res = await fetch(`${ctx}/comments/${recipeUuid}`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    }
                    // [운영 시 다시 활성화]
                    // const csrfMeta = document.querySelector('meta[name="_csrf"]');
                    // const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
                    // const headers = { "Content-Type": "application/json" };
                    // if (csrfMeta && csrfHeaderMeta) {
                    //     headers[csrfHeaderMeta.content] = csrfMeta.content;
                    // }
                    // const res = await fetch(`${ctx}/comments/${recipeUuid}`, {
                    //     method: "POST",
                    //     headers,
                    //     body: JSON.stringify({ commentsContent: content })
                    // });
                    ,
                    body: JSON.stringify({commentsContent: content})
                });

            if (!res.ok) throw new Error("댓글 작성 실패");

                // 작성 후 초기화
                cmtInput.value = "";
                loadComments(true); // 새로 불러오기
            } catch (e) {
                console.error(e);
                alert("댓글 작성 중 오류가 발생했습니다.");
            }
        });

        // 삭제
        async function deleteComment(commentsId, CommentElem) {
            if (!confirm("정말 삭제?")) return;

            try {
                const res = await fetch(`${ctx}/comments/${commentsId}`, {
                    method: "DELETE"
                });
                if (!res.ok) throw new Error("삭제 실패");
                CommentElem.remove();
            } catch (err) {
                console.error(err);
                alert("댓글 삭제 중 오류발생");
            }
        }

        // 대댓 불러오기
        async function loadReplies(parentId, container) {
            const res = await fetch(`${ctx}/comments/replies/${parentId}`);
            if (!res.ok) return;
            const replies = await res.json();
            container.innerHTML = "";
            replies.forEach(r => container.appendChild(createCommentElem(r)));
        }

        // 대댓작성
        function openReplyInput(parentId, container) {
            const textarea = document.createElement("textarea");
            const btn = document.createElement("button");
            btn.textContent = "등록";

            btn.addEventListener("click", async () => {
                const content = textarea.value.trim();
                if (!content) return;
                const res = await fetch(`${ctx}/comments/replies/${parentId}`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({commentsContent: content})
                });
                if (res.ok) {
                    loadReplies(parentId, container);
                    textarea.remove();
                    btn.remove();
                }
            });

            container.appendChild(textarea);
            container.appendChild(btn);
        }

        // 대댓 삭제
        async function deleteReply(parentId, CommentElem) {
            if (!confirm("삭제?")) return;

            try {
                const res = await fetch(`${ctx}/comments/replies/${parentId}`, {
                    method: "DELETE"
                });
                if (!res.ok) throw new Error("삭제 실패");
                CommentElem.remove();
            } catch (err) {
                console.error(err);
                alert("댓글 삭제 중 오류발생");
            }
        }

        // 더보기 버튼 클릭
        btnCmtMore.addEventListener("click", () => {
            loadComments();
        });

        // 초기 댓글 로드
        loadComments();
    }
);

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
