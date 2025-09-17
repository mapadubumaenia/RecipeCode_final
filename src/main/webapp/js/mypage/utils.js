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
            : `<iframe src="${recipe.thumbnailUrl}" style="width:100%;height:100%;border:0" allowfullscreen></iframe>`
    }
    </div>
    <p class="muted">${recipe.recipeIntro || ''}</p>
    <div class="post-cta flex-box">
      <div class="leftBox">
        <span class="like">❤️${recipe.likeCount || 0}</span>
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
    const isLike = user.isLike ?? false;

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
                    data-user-id="${user.recipe.uuid}" 
                    data-like="${islike}">
                ${islike ? "💔 UnLike" : "❤️ Like"}
            </button>    
        </div>
    </div>
    <div class="thumb"><img src="${user.thumbnailUrl || 'https://picsum.photos/seed/follow/800/500'}"></div>
    <div class="post-meta"><p>${user.recipeTitle || ''}</p></div>`;
    return article;
}

// 좋아요 토글
document.addEventListener("click", (e) => {
    if (e.target.classList.contains("like-toggle")) {
        const btn = e.target;
        const uuid = btn.dataset.uuid;
        const isLike = btn.dataset.like === "true";

        fetch(`/api/recipes/${uuid}/like`, {
            method: isLike ? "DELETE" : "POST",
            headers: { "Content-Type": "application/json" }
        })
            .then(res => {
                if (!res.ok) throw new Error("Like 토글 실패");

                // UI 갱신
                btn.dataset.like = (!isLike).toString();
                btn.textContent = !isLike ? "💔 Unlike" : "❤️ Like";
            })
            .catch(err => {
                console.error("Like 토글 오류:", err);
                alert("실패했습니다. 다시 시도해주세요.");
            });
    }
});