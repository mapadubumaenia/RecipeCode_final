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

// 좋아요/댓글 AJAX는 추후 여기서 fetch 붙이면 됨 (data-recipe-uuid 이용)

(function () {
    const btnLike = document.getElementById("btnLike");
    const likeCnt = document.getElementById("likeCnt");
    const recipeBox = document.querySelector(".container[data-recipe-uuid]");
    if (!btnLike || !likeCnt || !recipeBox) return;

    const initiallyLiked = btnLike.dataset.liked === "true";
    btnLike.classList.toggle("active", initiallyLiked);

    const recipeUuid = recipeBox.dataset.recipeUuid;

    btnLike.addEventListener("click", async () => {
        try {
            const resp = await fetch(`${ctx}/api/recipes/${recipeUuid}/like`, {
                method: "POST",
                credentials: "include",
                headers: {"Content-Type": "application/json"}
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
            if (!resp.ok) {
                const msg = await resp.text();
                if (resp.status === 401) {
                    if (confirm(msg + "\n로그인 페이지로 이동할까요?")) {
                        window.location.href = `${ctx}/auth/login`;
                    }
                } else if (resp.status === 400) {
                    alert(msg); // "본인 레시피에는 좋아요를 누를 수 없습니다!"
                } else {
                    alert("알 수 없는 오류 발생. 관리자에게 문의하세요!")
                }
                return;
            }
            const data = await resp.json();
            // 서버에서 내려준 dto 값 반영
            likeCnt.textContent = data.likesCount;
            const now = (data.isLike ?? data.liked ?? false) === true;
            btnLike.classList.toggle("active", now);
            btnLike.dataset.liked = String(now);
            // if (data.liked) {
            //     btnLike.classList.add("active"); // CSS로 하트 색 변환
            // } else {
            //     btnLike.classList.remove("active");
            // }
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

            // 알림 링크 이동용 id 추가
            div.id = `comment-${c.commentsId}`;

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
