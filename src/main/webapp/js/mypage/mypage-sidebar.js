document.addEventListener("DOMContentLoaded", () => {
    const sidebarTabs = document.querySelectorAll('#myfollowing .tabs .tab');
    const followContainer = document.querySelector("#followContainer");

    let currentTab = "following";
    let page = 0;
    let isLast = false;
    let isLoading = false;

    function getSidebarApiUrl() {
        return currentTab === "following"
            // ? `/api/mypage/my-recipes?page=${page}&size=5` // 임시로 재활용
            // : `/api/mypage/my-liked?page=${page}&size=5`;
            ? `/api/mypage/following?page=${page}&size=5`
            : `/api/mypage/followers?page=${page}&size=5`;
    }

    function loadSidebarFeeds() {
        if (isLast || isLoading) return;
        isLoading = true;

        fetch(getSidebarApiUrl())
            .then(res => res.json())
            .then(data => {
                data.content.forEach(user => {
                    const article = createFollowArticle(user);
                    followContainer.appendChild(article);
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

    sidebarTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            if (tab.classList.contains("is-active")) return;
            sidebarTabs.forEach(t => t.classList.remove("is-active"));
            tab.classList.add("is-active");

            currentTab = tab.dataset.tab;
            page = 0;
            isLast = false;
            followContainer.innerHTML = "";

            loadSidebarFeeds();
        });
    });

    loadSidebarFeeds();
});

