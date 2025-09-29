document.addEventListener("DOMContentLoaded", () => {
    const feedContainer = document.querySelector("#feedContainer");
    const userId = feedContainer.dataset.user;

    let page = 0;
    let isLast = false;
    let isLoading = false;

    function getApiUrl() {
        return `/api/profile/${userId}/recipes?page=${page}&size=5`;
    }

    function loadFeeds() {
        if (isLast || isLoading) return;
        isLoading = true;

        fetch(getApiUrl())
            .then(res => res.json())
            .then(data => {
                if (!data.content || data.content.length === 0) {
                    // 게시글이 없으면 안내 메시지 카드 추가 (중복 방지)
                    if (!feedContainer.querySelector(".no-feed")) {
                        const emptyCard = document.createElement("article");
                        emptyCard.className = "card post no-feed";
                        emptyCard.innerHTML = `
            <div class="post-body">
                <p class="muted">아직 작성한 글이 없습니다.</p>
            </div>
        `;
                        feedContainer.appendChild(emptyCard);
                    }
                } else {
                    // 기존 "없습니다" 메시지가 있으면 제거
                    const oldMsg = feedContainer.querySelector(".no-feed");
                    if (oldMsg) oldMsg.remove();

                    // 실제 게시글 렌더링
                    data.content.forEach(recipe => {
                        const article = createFeedArticle(recipe, currentUserEmail);
                        feedContainer.appendChild(article);
                    });
                }

                isLast = data.last;
                page++;
            })
            .catch(err => console.error("Error:", err))
            .finally(() => {
                isLoading = false;
            });
    }

    //  첫 로딩
    loadFeeds();

    //  무한 스크롤
    window.addEventListener("scroll", () => {
        if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
            loadFeeds();
        }
    });
});


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


// 팔로워/팔로잉 카운트 표시
document.addEventListener("DOMContentLoaded", () => {
    const profileUserId = document.querySelector("#feedContainer").dataset.user; // (변경됨)

    // follower count
    fetch(`/api/follow/${profileUserId}/follower/count`)
        .then(res => res.json())
        .then(count => {
            const el = document.getElementById("followerCount");
            if (el) el.textContent = count;
        })
        .catch(err => console.error("팔로워 수 가져오기 실패:", err));

    // following count
    fetch(`/api/follow/${profileUserId}/following/count`)
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

        // 팔로워 리스트 일부만 출력
        fetch(`/api/follow/${profileUserId}/follower?page=0&size=5`)
            .then(res => res.json())
            .then(data => {
                renderMiniCards(data.content, "followersList");
            });

        // 팔로잉 리스트 일부만 출력
        fetch(`/api/follow/${profileUserId}/following?page=0&size=5`)
            .then(res => res.json())
            .then(data => {
                renderMiniCards(data.content, "followingList");
            });
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
