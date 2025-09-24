document.addEventListener("DOMContentLoaded", () => {
    const feedContainer = document.querySelector("#feedContainer");
    const userId = feedContainer.dataset.user; // (변경됨) JSP에서 data-userid로 세팅
    let page = 0;
    let isLast = false;
    let isLoading = false;

    function getApiUrl() {
        // (변경됨) userEmail → userId
        return `/api/profile/${userId}/recipes?page=${page}&size=5`;
    }

    function loadFeeds() {
        if (isLast || isLoading) return;
        isLoading = true;

        fetch(getApiUrl())
            .then(res => res.json())
            .then(data => {
                data.content.forEach(recipe => {
                    const article = createFeedArticle(recipe, currentUserEmail);
                    feedContainer.appendChild(article);
                });
                isLast = data.last;
                page++;
                isLoading = false;
            })
            .catch(err => {
                console.error("Error:", err);
                isLoading = false;
            });
    }

    // 첫 로딩
    loadFeeds();

    // 무한 스크롤
    window.addEventListener("scroll", () => {
        if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
            loadFeeds();
        }
    });
});

// 공유 버튼 이벤트 (재활용)
document.addEventListener("click", (e) => {
    if (e.target.classList.contains("share-btn")) {
        const uuid = e.target.dataset.uuid;
        const url = window.location.origin + "/recipes/" + uuid;
        navigator.clipboard.writeText(url)
            .then(() => alert("링크가 복사되었습니다!"))
            .catch(() => alert("복사 실패 😢"));
    }
});


// 팔로워/팔로잉 카운트 표시
document.addEventListener("DOMContentLoaded", () => {
    const profileUserId = document.querySelector("#feedContainer").dataset.user; // (변경됨)

    // follower count
    fetch(`/api/follow/${profileUserId}/follower/count`) // (변경됨)
        .then(res => res.json())
        .then(count => {
            const el = document.getElementById("followerCount");
            if (el) el.textContent = count;
        })
        .catch(err => console.error("팔로워 수 가져오기 실패:", err));

    // following count
    fetch(`/api/follow/${profileUserId}/following/count`) // (변경됨)
        .then(res => res.json())
        .then(count => {
            const el = document.getElementById("followingCount");
            if (el) el.textContent = count;
        })
        .catch(err => console.error("팔로잉 수 가져오기 실패:", err));
});

// 사이드바 mini-card (팔로잉/팔로워 리스트)
document.addEventListener("DOMContentLoaded", () => {
    const miniCards = document.querySelectorAll(".mini-card");

    miniCards.forEach(card => {
        const userId = card.dataset.userid; // (변경됨: data-email → data-userid)
        if (!userId) return;

        const followerEl = card.querySelector(".mini-stats .f-count:nth-child(1) b");
        const followingEl = card.querySelector(".mini-stats .f-count:nth-child(2) b");

        fetch(`/api/follow/${userId}/follower/count`) // (변경됨)
            .then(res => res.json())
            .then(count => { if (followerEl) followerEl.textContent = count; });

        fetch(`/api/follow/${userId}/following/count`) // (변경됨)
            .then(res => res.json())
            .then(count => { if (followingEl) followingEl.textContent = count; });
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const tabs = document.querySelectorAll(".follow-tabs .tab-btn");
    const followersList = document.getElementById("followersList");
    const followingList = document.getElementById("followingList");

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("is-active"));
            tab.classList.add("is-active");

            if (tab.dataset.tab === "followers") {
                followersList.classList.remove("hidden");
                followingList.classList.add("hidden");
            } else {
                followingList.classList.remove("hidden");
                followersList.classList.add("hidden");
            }
        });
    });
});
