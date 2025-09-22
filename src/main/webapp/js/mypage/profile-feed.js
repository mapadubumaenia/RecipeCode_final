document.addEventListener("DOMContentLoaded", () => {
    const feedContainer = document.querySelector("#feedContainer");
    const userEmail = feedContainer.dataset.user; // JSP에서 data-user로 세팅
    let page = 0;
    let isLast = false;
    let isLoading = false;

    function getApiUrl() {
        return `/api/profile/${userEmail}/recipes?page=${page}&size=5`;
    }

    function loadFeeds() {
        if (isLast || isLoading) return;
        isLoading = true;

        fetch(getApiUrl())
            .then(res => res.json())
            .then(data => {
                data.content.forEach(recipe => {
                    const article = createFeedArticle(recipe, userEmail);
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
