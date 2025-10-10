// utils.js

// 안전 파서: 응답이 JSON이든 텍스트든 에러 없이 파싱
async function parseBodySafe(res) {
    const ct = (res.headers.get("content-type") || "").toLowerCase();
    // 1) JSON으로 보인다면 먼저 json() 시도 → 실패하면 text() 폴백
    if (ct.includes("application/json")) {
        try {
            return { kind: "json", data: await res.json() };
        } catch {
            try {
                const t = await res.text();
                return { kind: "text-badjson", data: t }; // 헤더는 json인데 실제는 텍스트
            } catch {
                return { kind: "none", data: null };
            }
        }
    }
    // 2) JSON이 아니라면 text() 먼저 → 혹시 JSON 문자열이면 파싱 시도
    try {
        const t = await res.text();
        try {
            const j = JSON.parse(t);
            return { kind: "json-mislabeled", data: j }; // 헤더는 text인데 실제는 JSON
        } catch {
            return { kind: "text", data: t };
        }
    } catch {
        return { kind: "none", data: null };
    }
}



// TODO: (운영 시 활성화) CSRF 토큰을 JS에서 쓰고 싶을 때
window.CSRF = window.CSRF || (() => {
    const t = document.querySelector('meta[name="_csrf"]');
    const h = document.querySelector('meta[name="_csrf_header"]');
    if (!t || !h) return { header: null, token: null };
    return { header: h.content, token: t.content };
})();

// 중복 클릭 방지
function setBusy(el, v) {
    if (!el) return;
    if (v) {
        el.dataset.busy = "true";
        // el.style.pointerEvents = "none"; // 원하면 주석 해제로 시각적/물리적 더블클릭 완전 차단
    } else {
        delete el.dataset.busy;
        // el.style.pointerEvents = "";
    }
}


// 시간 변환
function timeAgo(dateStr) {
    if (!dateStr) return "";
    const now = new Date();
    const past = new Date(dateStr);
    const diff = Math.floor((now - past) / 1000);

    if (diff < 60) return `${diff}s`;
    if (diff < 3600) return `${Math.floor(diff/60)}m`;
    if (diff < 86400) return `${Math.floor(diff/3600)}h`;
    if (diff < 604800) return `${Math.floor(diff/86400)}d`;
    if (diff < 2592000) return `${Math.floor(diff/604800)}w`;
    if (diff < 31536000) return `${Math.floor(diff/2592000)}mo`;
    return `${Math.floor(diff/31536000)}y`;
}

// 게시글용
function createFeedArticle(recipe, currentUserEmail) {
    const el = document.createElement("article");
    el.className = "card p-16 post";

    // const canEdit = recipe.userEmail === currentUserEmail;
    const editBtn = (recipe.userEmail === currentUserEmail)
        ? `<a href="/recipes/${recipe.uuid}/edit" class="btn-none float-text edit-feed">✎ Edit</a>`
        : "";

    const isLike = (recipe.isLike ?? recipe.liked ?? false) === true;
    const likeClass = recipe.isLike ? "like active" : "like";
    const likeCnt = Number(recipe.likeCount || 0); // ← 숫자로 캐스팅
    const isOwner = recipe.userEmail === currentUserEmail;
    const ctx = (typeof window !== "undefined" && window.ctx) ? window.ctx : "";
    const DEFAULT_PROFILE_IMG = ctx + "/images/default_profile.jpg";


    el.innerHTML = `
    <div class="post-head">
      <div class="avatar-sm"><img src="${recipe.profileImageUrl && recipe.profileImageUrl.trim()
        ? recipe.profileImageUrl
        : DEFAULT_PROFILE_IMG}" 
       alt="" class="avatar-sm"></div>
      <div class="post-info">
        <div class="post-id"><a href="/follow/network/${recipe.userId}">${recipe.userId}</a></div>
        <div class="muted">${timeAgo(recipe.insertTime)} • ${recipe.userLocation || '부산•대한민국'}</div>
      </div>
    </div>
    <div class="thumb">
      ${
        recipe.recipeType === "IMAGE"
            ? `<img src="${recipe.thumbnailUrl || 'https://picsum.photos/seed/default/800/500'}" alt="recipe">`
            : `<iframe src="${recipe.thumbnailUrl}" allowfullscreen></iframe>`
    }
    </div>
    <a href="/recipes/${recipe.uuid}">
    <p class="muted">${recipe.recipeIntro || ''}</p>
    </a>
    <div class="post-cta flex-box">
      <div class="leftBox">
        <button class="${likeClass} btn-none"
                data-uuid="${recipe.uuid}"
                data-like="${String(isLike)}"
                data-owner="${String(isOwner)}"
                aria-pressed="${String(isLike)}"
                ${isOwner ? 'aria-disabled="true" title="본인 레시피에는 좋아요를 누를 수 없습니다."' : ''}
                >
                <span class="icon" aria-hidden="true"></span>
                <span class="cnt">${likeCnt}</span></button>
        <a href="/recipes/${recipe.uuid}" class="btn-none post-cmt">💬 ${recipe.commentCount || 0}</a>
        <button class="btn-none share-btn float-text" data-uuid="${recipe.uuid}">↗ Share</button>
      </div>
      <div class="rightBox">${editBtn}</div>
    </div>`;
    return el;
}


// 팔로워/팔로잉용
function createFollowArticle(recipe) {

    const el = document.createElement("article");
    el.className = "card p-12 post";

    // 서버에서 내려줄 값: user.isLike (true/false)
    const userId  = recipe.userId || "unknown";
    const avatar  = recipe.profileImageUrl || "";
    const place   = recipe.userLocation || "부산•대한민국";
    const stamp   = timeAgo(recipe.insertTime);
    const thumb   = recipe.thumbnailUrl || "https://picsum.photos/seed/follow/800/500";
    const title   = recipe.recipeTitle || recipe.recipeIntro || "";
    const isLike  = (recipe.isLike ?? recipe.liked ?? false) === true;
    const likeCnt = Number(recipe.likeCount || 0);
    const isOwner = (recipe.userEmail && window.currentUserEmail)
        ? recipe.userEmail === window.currentUserEmail
        : false;

    el.innerHTML = `
    <div class="post-head">
    <div class="avatar-ss">
  <img src="${recipe.profileImageUrl && recipe.profileImageUrl.trim()
        ? recipe.profileImageUrl
        : DEFAULT_PROFILE_IMG}" 
       alt="" class="avatar-ss">
</div>
      <div class="leftBox">
        <div class="post-info">
        <div class="post-id"><a href="/follow/network/${userId}">${userId}</a></div>
        <div class="muted">${stamp} • ${place}</div>
      </div>
      </div>
        <div class="rightBox">
            <button class="like like-toggle btn-none ${isLike ? "active" : "️"}"
                    data-uuid="${recipe.uuid}"
                    data-like="${String(isLike)}"
                    aria-pressed="${String(isLike)}"
                    ${isOwner ? 'aria-disabled="true" title="본인 레시피에는 좋아요를 누를 수 없습니다."' : ''}>
                    <span class="icon" aria-hidden="true"></span>
                     <span class="cnt">${likeCnt}</span></button>
        </div>
    </div>
    <div class="thumb">
          ${
        recipe.recipeType === "IMAGE"
            ? `<img src="${thumb}" alt="recipe">`
            : `<iframe src="${thumb}" allowfullscreen></iframe>`
    }
    </div>
    <div class="post-meta flex-box">
    <p>${title}</p>
    <a class="btn-none float-text" href="/recipes/${encodeURIComponent(recipe.uuid)}">더보기</a></div>`;
    return el;
}

/* 전역 좋아요 토글 핸들러 (.like 버튼) — 기존 로직과 동일하게 유지 */
document.addEventListener("click", async (e) => {
    const heart = e.target.closest(".like");
    if (!heart || !heart.dataset.uuid) return;

    // 내 레시피에는 못누름
    if (heart.dataset.owner === "true" || heart.getAttribute("aria-disabled") === "true") {
        alert("본인 레시피에는 좋아요를 누를 수 없습니다.");
        return;
    }

    // 중복 클릭 방지
    if (heart.dataset.busy === "true") return;
    setBusy(heart, true);

    const uuid   = heart.dataset.uuid;
    const isLike = heart.dataset.like === "true";

    const ctx    = (typeof window.ctx === "string") ? window.ctx : "";
    const url    = `${ctx}/api/recipes/${encodeURIComponent(uuid)}/like`;
    let method = isLike ? "DELETE" : "POST";

    // 헤더: Accept + CSRF(있으면)
    const headers = { "Accept": "application/json" };
    if (CSRF.header && CSRF.token) headers[CSRF.header] = CSRF.token;

    try {
        let res = await fetch(url, { method, credentials: "same-origin", headers });

        // 서버가 DELETE 막을 때(405) 폴백: POST로 토글
    if (res.status === 405 && method === "DELETE") {
        method = "POST";
        res = await fetch(url, { method, credentials: "same-origin", headers });
    }

        // ✅ 안전 파싱 (절대 SyntaxError 안 터지게)
        const parsed = await parseBodySafe(res);
        const body   = parsed.data;

// ✅ 에러 처리 (401은 서버 문구 그대로 노출)
        if (!res.ok) {
            if (res.status === 401) {
                // 서버가 텍스트만 줘도 그대로 보여줌
                const msg401 = (typeof body === "string" && body) ? body
                    : (body?.message || "로그인하지 않은 사용자입니다.");
                alert(msg401);
                return;
            }

            // 그 외 에러
            const msg = (typeof body === "string" && body) ? body
                : (body?.message || body?.msg || "좋아요 처리에 실패했어요.");
            if (res.status === 400 || res.status === 403 || /SELF_LIKE|본인/.test(msg)) {
                alert("본인 레시피에는 좋아요를 누를 수 없습니다.");
            } else {
                alert(msg);
            }
            return;
        }

    // 표준/유연 응답 처리: { isLike | liked, likesCount }
    const nowLiked = (body?.isLike ?? body?.liked ?? !isLike) === true;
    const cntEl    = heart.querySelector(".cnt");
    const newCnt   = Number(body?.likesCount ?? (cntEl ? cntEl.textContent : "0"));

    // UI 반영
    heart.classList.toggle("active", nowLiked);
    heart.dataset.like = String(nowLiked);
    heart.setAttribute("aria-pressed", String(nowLiked));
    if (cntEl && Number.isFinite(newCnt)) cntEl.textContent = String(newCnt);

    } catch (err) {
        console.error(err);
        alert("네트워크 오류가 발생했어요. 잠시 후 다시 시도해주세요.");
    } finally {
        setBusy(heart, false);
    }
    // const dto = await res.json();
    //     const now = (dto.isLike ?? dto.liked ?? false) === true;
    //     heart.classList.toggle("active", now);
    //     heart.dataset.like = String(now);
    //     heart.setAttribute("aria-pressed", String(now));
    //
    //     const cntEl = heart.querySelector(".cnt");
    //     const newCnt = Number(dto.likesCount ?? (cntEl ? cntEl.textContent : "0"));
    //     if (cntEl) cntEl.textContent = String(newCnt);
    // } catch (err) {
    //     console.error(err);
    //     alert("실패했습니다. 다시 시도해주세요.");
    // }
    
});