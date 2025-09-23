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
    const article = document.createElement("article");
    article.className = "card p-16 post";

    const editBtn = (recipe.userEmail === currentUserEmail)
        ? `<a href="/recipes/${recipe.uuid}/edit" class="btn-none float-text edit-feed">✎ Edit</a>`
        : "";

    const isLike = (recipe.isLike ?? recipe.liked ?? false) === true;
    const likeClass = recipe.isLike ? "like active" : "like";

    article.innerHTML = `
    <div class="post-head">
      <div class="avatar-ss"><img src="${recipe.profileImageUrl || ''}" alt=""></div>
      <div class="post-info">
        <div class="post-id">${recipe.userId}</div>
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
        <button class="${likeClass} btn-none" data-uuid="${recipe.uuid}" data-like="${String(isLike)}">❤️${recipe.likeCount || 0}</button>
        <a href="/recipes/${recipe.uuid}" class="btn-none post-cmt">💬 ${recipe.commentCount || 0}</a>
        <button class="btn-none share-btn float-text" data-uuid="${recipe.uuid}">↗ Share</button>
      </div>
      <div class="rightBox">${editBtn}</div>
    </div>`;
    return article;
}

// 팔로워/팔로잉용
function createFollowArticle(user) {
    const article = document.createElement("article");
    article.className = "card p-12 post";

    // 서버에서 내려줄 값: user.isLike (true/false)
    const isLike = (user.isLike ?? user.liked ?? false) === true;
    const uuid = user.recipe?.uuid || user.uuid || "";

    article.innerHTML = `
    <div class="post-head">
    <div class="avatar-ss"><img src="${user.profileImageUrl || ''}" alt="${user.userId}"></div>
      <div class="leftBox">
        <div class="post-info">
        <div class="post-id">${user.userId}</div>
        <div class="muted">${timeAgo(user.insertTime)} • ${user.userLocation || '부산•대한민국'}</div>
      </div>
      </div>
        <div class="rightBox">
            <button class="like-toggle btn-none">
                    data-uuid="${uuid}"
                    data-like="${String(isLike)}">
                ${isLike ? "💔 UnLike" : "❤️ Like"}
            </button>    
        </div>
    </div>
    <div class="thumb"><img src="${user.thumbnailUrl || 'https://picsum.photos/seed/follow/800/500'}"></div>
    <div class="post-meta"><p>${user.recipeTitle || ''}</p></div>`;
    return article;
}

// 좋아요 토글
document.addEventListener("click", async (e) => {  // ← async 추가!
    // 1) 버튼(.like-toggle) 클릭
    if (e.target.classList.contains("like-toggle")) {
        const btn = e.target;
        const uuid = btn.dataset.uuid;
        const isLike = btn.dataset.like === "true";
        const url = (typeof ctx === "string" ? ctx : "") + "/api/recipes/" + encodeURIComponent(uuid) + "/like";
        const method = isLike ? "DELETE" : "POST"; // 백이 POST만 지원하면 "POST" 고정

        try {
            const res = await fetch(url, {
                method,
                credentials: "same-origin",
                headers: { "Content-Type": "application/json" }
                // ...getCsrfHeaders()  // 운영 시 복구
            });
            if (!res.ok) throw new Error(res.statusText);
            const dto = await res.json(); // { isLike/liked, likesCount }

            const now = (dto.isLike ?? dto.liked ?? false) === true;
            btn.dataset.like = String(now);
            btn.textContent = now ? "💔 Unlike" : "❤️ Like";

            // 같은 카드 하트 동기화
            const article = btn.closest("article");
            const heart = article?.querySelector(".like");
            if (heart) {
                heart.classList.toggle("active", now);
                heart.dataset.like = String(now);
                const newCnt = dto.likesCount ?? Number((heart.textContent.match(/\d+/) || ['0'])[0]);
                heart.textContent = `❤️${newCnt}`;
            }

            // Likes 탭에서 언라이크 시 카드 제거
            const activeTab = document.querySelector(".tab.is-active")?.dataset.tab;
            if (!now && activeTab === "likes") {
                article?.remove();
            }
        } catch (err) {
            console.error(err);
            alert("실패했습니다. 다시 시도해주세요.");
        }
        return; // 다른 분기 타지 않도록 종료
    }

    // 2) 하트(span.like) 클릭
    const heart = e.target.closest(".like");
    if (heart && heart.dataset.uuid) {
        const uuid = heart.dataset.uuid;
        const isLike = heart.dataset.like === "true";
        const url = `${typeof ctx === "string" ? ctx : ""}/api/recipes/${encodeURIComponent(uuid)}/like`;
        const method = isLike ? "DELETE" : "POST"; // 백이 POST 토글만이면 "POST"

        try {
            const res = await fetch(url, {
                method,
                credentials: "same-origin",
                headers: { "Content-Type": "application/json" }
            });
            if (!res.ok) throw new Error("Like 토글 실패");
            const dto = await res.json();

            const now = (dto.isLike ?? dto.liked ?? false) === true;
            heart.classList.toggle("active", now);
            heart.dataset.like = String(now);
            const newCnt = dto.likesCount ?? Number((heart.textContent.match(/\d+/) || ['0'])[0]);
            heart.textContent = `❤️${newCnt}`;

            // Likes 탭이면 카드 제거
            const activeTab = document.querySelector(".tab.is-active")?.dataset.tab;
            if (!now && activeTab === "likes") {
                heart.closest("article")?.remove();
            }
        } catch (err) {
            console.error(err);
            alert("실패했습니다. 다시 시도해주세요.");
        }
    }
});