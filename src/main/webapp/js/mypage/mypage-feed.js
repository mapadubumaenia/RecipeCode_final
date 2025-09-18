document.addEventListener("DOMContentLoaded", () => {
    const tabs = document.querySelectorAll('#myposts .tabs .tab');
    const feedContainer = document.querySelector("#feedContainer");

    let page = 0;
    let isLast = false;
    let isLoading = false;
    let currentTab = "myposts";

    function getApiUrl(){
        const base = (typeof ctx === "string" ? ctx : "");
        return currentTab === "myposts"
            ? `/api/mypage/my-recipes?page=${page}&size=5`
            : `/api/mypage/my-liked?page=${page}&size=5`;
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

    // 탭 클릭 이벤트
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            if (tab.classList.contains("is-active")) return;
            tabs.forEach(t => t.classList.remove("is-active"));
            tab.classList.add("is-active");
            const feedContainer =  document.querySelector("#feedContainer");

            currentTab = tab.dataset.tab;
            page = 0;
            isLast = false;
            feedContainer.innerHTML = "";

            loadFeeds();
        });
    });

    // 첫 로딩
    loadFeeds();

    // 무한 스크롤
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