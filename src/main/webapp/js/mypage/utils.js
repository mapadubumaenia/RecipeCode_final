// utils.js

// TODO: (운영 시 활성화) CSRF 토큰을 JS에서 쓰고 싶을 때
// const CSRF = (() => {
//     const t = document.querySelector('meta[name="_csrf"]');
//     const h = document.querySelector('meta[name="_csrf_header"]');
//     if (!t || !h) return { header: null, token: null };
//     return { header: h.content, token: t.content };
// })();


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

    el.innerHTML = `
    <div class="post-head">
      <div class="avatar-ss"><img src="${recipe.profileImageUrl || ''}" alt=""></div>
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
    <div class="avatar-ss"><img src="${avatar}" alt="${userId}"></div>
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
    if (heart.dataset.owner === "true") {
        alert("본인 레시피에는 좋아요를 누를 수 없습니다.");
        return;
    }

    const uuid   = heart.dataset.uuid;
    const isLike = heart.dataset.like === "true";
    const url    = `${typeof window.ctx === "string" ? window.ctx : ""}/api/recipes/${encodeURIComponent(uuid)}/like`;
    const method = isLike ? "DELETE" : "POST";

    try {
        const res = await fetch(url, {
            method,
            credentials: "same-origin",
            headers: { "Content-Type": "application/json" }
        });
        if (!res.ok) {
            let msg = "";
            try {
                const err = await res.json();
                msg = err.message || err.msg || "";
            } catch {}
            if (res.status === 403 || res.status === 400 || msg.includes("SELF_LIKE")) {
                alert("본인 레시피에는 좋아요를 누를 수 없습니다.");
            } else {
             alert("실패했습니다. 다시 시도해주세요.");
            }
            return;
        }
        const dto = await res.json();
        const now = (dto.isLike ?? dto.liked ?? false) === true;
        heart.classList.toggle("active", now);
        heart.dataset.like = String(now);
        heart.setAttribute("aria-pressed", String(now));

        const cntEl = heart.querySelector(".cnt");
        const newCnt = Number(dto.likesCount ?? (cntEl ? cntEl.textContent : "0"));
        if (cntEl) cntEl.textContent = String(newCnt);
    } catch (err) {
        console.error(err);
        alert("실패했습니다. 다시 시도해주세요.");
    }
    
});