// 본문 "더보기" 토글
(function () {
    const box = document.getElementById('postDesc');
    const btn = document.getElementById('btnToggleDesc');
    btn?.addEventListener('click', () => box.classList.toggle('expanded'));
})();

// 이미지/텍스트 슬라이더
(function () {
    const imgTrack = document.getElementById("imgSlides");
    const txtTrack = document.getElementById("textSlides");
    const sliderRoot = document.querySelector(".step-slider");
    if (!imgTrack || !txtTrack || !sliderRoot) return;

    const slideCount = Math.min(
        imgTrack.querySelectorAll(".slide").length,
        txtTrack.querySelectorAll(".slide").length
    );
    let index = 0;

    function trackWidth() {
        return sliderRoot.getBoundingClientRect().width;
    }

    function setTranslate(px) {
        imgTrack.style.transform = `translateX(${px}px)`;
        txtTrack.style.transform = `translateX(${px}px)`;
    }

    function snapTo(i) {
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

// 좋아요
(() => {
    const btn = document.getElementById("btnLike");
    if (!btn) return;

    const ctx  = window.ctx || "";
    const uuid = btn.dataset.uuid || document.querySelector(".container[data-recipe-uuid]")?.dataset.recipeUuid;
    const cntEl = btn.querySelector(".cnt");

    const toBool = (v) => String(v).trim().toLowerCase() === "true";
    const getCount = () => Number(cntEl?.textContent || 0);

    const setUI = (liked, count) => {
        btn.classList.toggle("active", liked);
        btn.dataset.like = String(liked);
        btn.setAttribute("aria-pressed", String(liked));
        if (typeof count === "number" && !Number.isNaN(count) && cntEl) {
            cntEl.textContent = String(count);
        }
    };

    // 초기 상태 반영
    const initialLiked = btn.classList.contains("active") || toBool(btn.dataset.like);
    setUI(initialLiked, getCount());

    btn.addEventListener("click", async () => {
        if (btn.getAttribute("aria-disabled") === "true") {
            alert("본인 레시피에는 좋아요를 누를 수 없습니다.");
            return;
        }
        if (btn.dataset.busy === "true") return;
        btn.dataset.busy = "true";

        const liked = toBool(btn.dataset.like);
        // 서버가 POST/DELETE 분기면 아래 사용, 토글 POST만 있으면 method = "POST"로 고정
        let method = liked ? "DELETE" : "POST";
        let url    = `${ctx}/api/recipes/${encodeURIComponent(uuid)}/like`;

        // CSRF (운영 시 권장)
        const t = document.querySelector('meta[name="_csrf"]');
        const h = document.querySelector('meta[name="_csrf_header"]');
        const headers = {"Accept":"application/json"};
        if (t && h) headers[h.content] = t.content;

        try {
            let res = await fetch(url, { method, credentials: "same-origin", headers });

            // 서버가 DELETE 미지원(405)인데 토글 POST만 지원하는 경우 폴백
            if (res.status === 405 && method === "DELETE") {
                res = await fetch(url, { method: "POST", credentials: "same-origin", headers });
            }

            const ctype = res.headers.get("content-type") || "";
            const body  = ctype.includes("application/json") ? await res.json() : { message: await res.text() };


            if (!res.ok) {
                if (res.status === 401) { alert("로그인 후 이용해주세요."); return; }
                if (res.status === 400 && /본인|자기|self/i.test(body?.message || "")) {
                    alert("본인 레시피에는 좋아요를 누를 수 없습니다."); return;
                }
                alert(body?.message || "좋아요 처리 중 오류가 발생했어요."); return;
            }

            // 표준 응답 가정: { isLike | liked, likesCount }
            const nowLiked = (body?.isLike ?? body?.liked ?? !liked) === true;
            const newCnt   = Number(body?.likesCount ?? (cntEl ? cntEl.textContent : 0));
            setUI(nowLiked, newCnt);

        } catch (e) {
            console.error(e);
            alert("네트워크 오류가 발생했어요.");
        } finally {
            delete btn.dataset.busy;
        }
    });
})();

// TODO: 좋아요

(() => {
    const btn = document.getElementById("btnFollow");
    if (!btn) return;

    // 게스트면 클릭 시 로그인 유도
    if (btn.getAttribute("aria-disabled") === "true") {
        btn.addEventListener("click", () => {
            alert("팔로우 기능은 로그인 후 이용할 수 있습니다.");
            location.href = `${ctx}/auth/login`; // 원하면 리다이렉트
        });
        return; // 더 이상 로직 진행하지 않음
    }

    const owner = btn.dataset.owner;
    const ctx = window.ctx || "";

    const setUI = (following) => {
        btn.dataset.following = String(following);
        btn.textContent = following ? "Unfollow" : "Follow";
        btn.classList.toggle("is-following", following);
        btn.setAttribute("aria-pressed", String(following));
    };
    setUI(btn.dataset.following === "true");

    btn.addEventListener("click", async () => {
        // 요청 중 중복클릭 방지
        if (btn.dataset.busy === "true") return;
        btn.dataset.busy = "true";

        const following = btn.dataset.following === "true";
        const method = following ? "DELETE" : "POST";
        const url = `${ctx}/api/follow/${encodeURIComponent(owner)}`;

        //나중에
        const t = document.querySelector('meta[name="_csrf"]');
        const h = document.querySelector('meta[name="_csrf_header"]');
        const headers = {"Accept": "application/json"};
        if (t && h) headers[h.content] = t.content;

        try {
            const res = await fetch(url, { method, credentials: "same-origin", headers });

            // 🔎 디버그
            console.log("[follow] status:", res.status, res.statusText);

            let body = null;
            const ctype = res.headers.get("content-type") || "";
            if (ctype.includes("application/json")) {
                try { body = await res.json(); } catch {}
            } else {
                // text/plain 같은 경우
                try { body = { message: await res.text() }; } catch {}
            }

            if (!res.ok) {
                if (res.status === 401) { alert("로그인 후 이용해주세요."); return; }
                if (res.status === 403 && body && (body.code === "SELF_FOLLOW_FORBIDDEN" || /본인/.test(body.message||""))) {
                    alert("본인 계정은 팔로우할 수 없습니다.");
                    return;
                }
                alert(body?.message || "처리 중 오류가 발생했어요.");
                return;
            }

            // 표준 응답: { following: boolean } 를 기대
            const now = (body && typeof body.following === "boolean")
                ? body.following
                : !following; // 혹시 바디가 비었으면 토글 추정

            setUI(now);

        } catch (e) {
            console.error(e);
            alert("네트워크 오류가 발생했어요.");
        } finally {
            delete btn.dataset.busy;
        }
    });
})();

// 공유 버튼 이벤트
document.addEventListener("click", (e) => {
    if (e.target.classList.contains("share-btn")) {
        const uuid = e.target.dataset.uuid;
        const url = window.location.origin + "/recipes/" + uuid;
        navigator.clipboard.writeText(url)
            .then(() => alert("링크가 복사되었습니다!"))
            .catch(() => alert("복사 실패 😢"));
    }
});

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
        const cmtHeader = document.querySelector(".comments-title");

        const COMMENTS_PAGE_SIZE = 5; // 한 번에 표시할 댓글 수
        let page = 0;
        let sort = "desc"; // 최신순

        // 날짜 포맷 함수
        function formatDate(dateString) {
            if (!dateString) return "";
            const date = new Date(dateString);
            return date.toLocaleString();
        }

        // 새로운 신고 모달
        const myModal = document.getElementById("myReportModal");
        const myBtnClose = myModal.querySelector("#myReportClose");
        const myForm = myModal.querySelector("#myReportForm");

        myBtnClose?.addEventListener("click", () => {
            myModal.hidden = true;
            currentReportBtn = null;
        });

        myForm?.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!currentReportBtn) return;

            // hidden input 재확인
            const hiddenInput = myForm.querySelector("#commentsId");
            if (!hiddenInput || !hiddenInput.value) {
                alert("댓글 ID가 설정되지 않았습니다.");
                return;
            }

            const formData = Object.fromEntries(new FormData(myForm));
            try {
                const res = await fetch(`${ctx}/comments/report/save`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(formData),
                    credentials: "include"
                });
                if (!res.ok) {
                    const text = await res.text();
                    throw new Error("신고 실패: " + text);
                }

                const countMatch = currentReportBtn.textContent.match(/\d+/);
                const count = countMatch ? parseInt(countMatch[0], 10) + 1 : 1;
                currentReportBtn.textContent = `신고 (${count})`;
                currentReportBtn.disabled = true;
                currentReportBtn.classList.add("reported");

                alert("신고가 접수되었습니다.");
                myModal.hidden = true;
                currentReportBtn = null;

            } catch (err) {
                console.error(err);
                alert("신고 중 오류가 발생했습니다: " + err.message);
            }
        });

        // 댓글 DOM 생성 (XSS 방지)
        function createCommentElem(c) {
            const div = document.createElement("div");
            div.className = "comment";
            div.dataset.commentsId = c.commentsId;

            // 댓글창
            const header = document.createElement("div");
            header.className = "comment-header";
            header.innerHTML = `<b>${c.userId}</b> • <span class="time">
            ${formatDate(c.insertTime)}${c.updateTime && c.updateTime !== c.insertTime ? ' • ' +
                '수정: ' + formatDate(c.updateTime) : ''}</span>`;

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
            <button class="btnLike">🤍 ${c.likeCount || 0}</button>
            <button class="myBtnReport" data-comments-id="${c.commentsId}">신고 (${c.reportCount || 0})</button>
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

            // 좋아요 버튼 초기 상태 반영
            const btnLike = actions.querySelector(".btnLike");
            const liked = c.liked ?? false; // DB에서 내려오는 값
            btnLike.textContent = liked ? `❤️ ${c.likeCount || 0}` : `🤍 ${c.likeCount || 0}`;
            btnLike.classList.toggle("active", liked);
            btnLike.addEventListener("click", () => likeComment(c.commentsId, btnLike));

            // 신고 버튼 → 새 모달 열기
            const reportBtn = actions.querySelector(".myBtnReport");
            if (reportBtn) {
                if (c.alreadyReported) {
                    reportBtn.disabled = true;
                    reportBtn.classList.add("reported");
                }
                reportBtn.addEventListener("click", () => {
                    currentReportBtn = reportBtn;
                    myModal.hidden = false;

                    const hiddenInput = myForm.querySelector("#commentsId");
                    if (hiddenInput) hiddenInput.value = c.commentsId;
                    else console.error("commentsId input 없음");
                });
            }

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
                    },
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
                    body: JSON.stringify({commentsContent: content}),
                    credentials: "include"
                });

                if (!res.ok) throw new Error("댓글 작성 실패");

                // 작성 후 초기화
                cmtInput.value = "";

                await loadCommentsCount();
                await loadComments(true);
            } catch (e) {
                console.error(e);
                alert("댓글 작성 중 오류가 발생했습니다.");
            }
        });

        // 댓글 수정
        async function editComment(commentsId, contentElem) {
            const oldContent = contentElem.textContent;
            const cmtCard = contentElem.closest(".comment");
            cmtCard?.classList.add("editing");

            const textarea = document.createElement("textarea");
            textarea.value = oldContent; // 기존 입력내용 기본값
            textarea.classList.add("edit-textarea");

            // 버튼 영역
            const controls = document.createElement("div");
            controls.classList.add("edit-controls");

            const btnSave = document.createElement("button");
            btnSave.textContent = "저장";
            btnSave.classList.add("btn--ghost");

            const btnCancel = document.createElement("button");
            btnCancel.textContent = "취소";
            btnCancel.classList.add("btn--ghost");

            controls.appendChild(btnSave);
            controls.appendChild(btnCancel);

            // DOM 교체. 기존 댓글 숨김
            contentElem.style.display = "none";
            contentElem.insertAdjacentElement("afterend", textarea);
            textarea.insertAdjacentElement("afterend", controls);

            // 바로 입력가능하게
            textarea.focus();

            // 취소
            btnCancel.addEventListener("click", () => {
                textarea.remove();
                controls.remove();
                contentElem.style.display = "";
                cmtCard?.classList.remove("editing");
            });

            // 저장
            btnSave.addEventListener("click", async () => {
                const newContent = textarea.value.trim();
                if (!newContent || newContent === oldContent) {
                    btnCancel.click();
                    return;
                }

                try {
                    const res = await fetch(`${ctx}/comments/${commentsId}`, {
                        method: "PATCH",
                        headers: {"Content-Type": "application/json"},
                        body: JSON.stringify({commentsContent: newContent})
                    });
                    const data = await res.json();

                    contentElem.textContent = data.commentsContent;
                    textarea.remove();
                    controls.remove();
                    contentElem.style.display = "";
                    cmtCard?.classList.remove("editing");

                    // 시간 갱신
                    const headerElem = contentElem.parentElement.querySelector(".comment-header");
                    if (headerElem) {
                        let timeText = formatDate(data.insertTime);
                        if (data.updateTime && data.updateTime !== data.insertTime) {
                            timeText += ` • 수정: ${formatDate(data.updateTime)}`;
                        }
                        headerElem.querySelector(".time").textContent = timeText;
                    }
                } catch (err) {
                    console.error(err);
                    alert("댓글 수정 중 오류가 발생했습니다.");
                }
            });
        }

        // 댓글 삭제
        async function deleteComment(commentsId, CommentElem) {
            if (!confirm("정말 삭제하시겠습니까?")) return;

            try {
                const res = await fetch(`${ctx}/comments/${commentsId}`, {
                    method: "DELETE"
                });
                if (!res.ok) throw new Error("삭제 실패");
                CommentElem.remove();
                await loadCommentsCount();
                await loadComments(true);
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

        // 대댓 작성
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
                await loadCommentsCount();

            });

            container.appendChild(textarea);
            container.appendChild(btn);
        }

        // 댓글 수 세기
        async function loadCommentsCount() {
            try {
                const res = await fetch(`${ctx}/comments/count/${recipeUuid}`);
                if (!res.ok) throw new Error("댓글 수 조회 실패");
                const data = await res.json();
                cmtHeader.textContent = `댓글 (${data.commentsCount}개)`;
            } catch (err) {
                console.error(err);
                cmtHeader.textContent = "댓글 (0개)";
            }
        }

        // 좋아요
        async function likeComment(commentId, btn) {
            try {
                const res = await fetch(`${ctx}/comments/likes/${commentId}`, {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    credentials: "include"
                });
                if (res.status === 401) {
                    alert("로그인이 필요합니다.");
                    return;
                }
                if (!res.ok) {
                    const msg = await res.text();
                    throw new Error(msg || "좋아요 처리 실패");
                }

                const data = await res.json();

                const nowLiked = data.liked ?? false;
                btn.textContent = nowLiked ? `❤️ ${data.likesCount}` : `🤍 ${data.likesCount}`;
                btn.classList.toggle("active", nowLiked);
            } catch (err) {
                console.error(err);
                alert("좋아요 처리중 오류 발생")
            }
        }

        // 더보기 버튼 클릭
        btnCmtMore.addEventListener("click", () => {
            loadComments();
        });

        // 초기 댓글 로드
        loadCommentsCount();
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
