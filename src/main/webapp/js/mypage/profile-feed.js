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

    // .mini-card가 0개여도 fetch 실행되도록 선처리
    if (miniCards.length === 0) {
        fetch(`/api/follow/${profileUserId}/follower?page=0&size=5`)
            .then(res => res.json())
            .then(data => {
                const list = Array.isArray(data?.content) ? data.content : (Array.isArray(data) ? data : []);
                const box  = document.getElementById("followersList");
                if (box && !list.length) box.innerHTML = `<p class="muted">아직 팔로워가 없어요</p>`;
                else if (box && list.length) renderMiniCards(list, "followersList");
            })
            .catch(() => {
                const box = document.getElementById("followersList");
                if (box) box.innerHTML = `<p class="muted">아직 팔로워가 없어요</p>`;
            });

        fetch(`/api/follow/${profileUserId}/following?page=0&size=5`)
            .then(res => res.json())
            .then(data => {
                const list = Array.isArray(data?.content) ? data.content : (Array.isArray(data) ? data : []);
                const box  = document.getElementById("followingList");
                if (box && !list.length) box.innerHTML = `<p class="muted">아직 팔로잉이 없어요</p>`;
                else if (box && list.length) renderMiniCards(list, "followingList");
            })
            .catch(() => {
                const box = document.getElementById("followingList");
                if (box) box.innerHTML = `<p class="muted">아직 팔로잉이 없어요</p>`;
            });

        return; // ← 여기서 끝! (아래 forEach 안 타게)
    }

    // .mini-card가 1개 이상일 때
    miniCards.forEach((card, idx) => {
        const userId = card.dataset.userid; // (변경됨: data-email → data-userid)
        if (!userId) return;

        // 첫 카드에서만 호출해 중복 fetch 방지
        if (idx !== 0) return;

        // 팔로워 리스트 일부만 출력
        fetch(`/api/follow/${profileUserId}/follower?page=0&size=5`)
            .then(res => res.json())
            .then(data => {
                const list = Array.isArray(data?.content) ? data.content : (Array.isArray(data) ? data : []);
                const box  = document.getElementById("followersList");
                if (!box) return;

                // ★ 이미 서버 렌더 카드가 있으면 '없어요' 찍지 않음
                const hasInitial = !!box.querySelector(".mini-card");

                if (!list.length) {
                    if (!hasInitial) box.innerHTML = `<p class="muted">아직 팔로워가 없어요</p>`;
                    return;
                }
                renderMiniCards(list, "followersList");
            })
            .catch(() => {
                const box = document.getElementById("followersList");
                if (box && !box.querySelector(".mini-card")) {
                    box.innerHTML = `<p class="muted">아직 팔로워가 없어요</p>`;
                }
            });

        // 팔로잉 리스트 일부만 출력
        fetch(`/api/follow/${profileUserId}/following?page=0&size=5`)
            .then(res => res.json())
            .then(data => {
                const list = Array.isArray(data?.content) ? data.content : (Array.isArray(data) ? data : []);
                const box  = document.getElementById("followingList");
                if (!box) return;

                const hasInitial = !!box.querySelector(".mini-card"); // ★ 서버 렌더 존재 여부

                if (!list.length) {
                    if (!hasInitial) box.innerHTML = `<p class="muted">아직 팔로잉이 없어요</p>`;
                    return;
                }
                renderMiniCards(list, "followingList");
            })
            .catch(() => {
                const box = document.getElementById("followingList");
                if (box && !box.querySelector(".mini-card")) {
                    box.innerHTML = `<p class="muted">아직 팔로잉이 없어요</p>`;
                }
            });
    });
});

// 사이드바 mini-card (팔로잉/팔로워 리스트) - 기존 코드
// document.addEventListener("DOMContentLoaded", () => {
//     const miniCards = document.querySelectorAll(".mini-card");
//
//     miniCards.forEach(card => {
//         const userId = card.dataset.userid; // (변경됨: data-email → data-userid)
//         if (!userId) return;
//
//         // 팔로워 리스트 일부만 출력
//         fetch(`/api/follow/${profileUserId}/follower?page=0&size=5`)
//             .then(res => res.json())
//             .then(data => {
//                 renderMiniCards(data.content, "followersList");
//             });
//
//         // 팔로잉 리스트 일부만 출력
//         fetch(`/api/follow/${profileUserId}/following?page=0&size=5`)
//             .then(res => res.json())
//             .then(data => {
//                 renderMiniCards(data.content, "followingList");
//             });
//     });
// });

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
