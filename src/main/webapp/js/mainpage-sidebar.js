// mypage-sidebar.js

document.addEventListener("DOMContentLoaded", () => {
    const sidebarTabs = document.querySelectorAll('#myfollowing .tabs .tab');
    const followContainer = document.querySelector("#followContainer");

    const ctxPrefix = (typeof window.ctx === "string" ? window.ctx : "");
    let currentTab  = "following";  // "following" | "followers"
    let page = 0, isLast = false, isLoading = false;

    function getSidebarApiUrl() {
        const base = `${ctxPrefix}/api/mypage`;
        return currentTab === "following"
            ? `${base}/following?page=${page}&size=2`
            : `${base}/followers?page=${page}&size=2`;
    }

    async function loadSidebarFeeds() {
        if (isLast || isLoading) return;
        isLoading = true;
        try {
            const res = await window.fetch(getSidebarApiUrl(), {
                credentials: "include",
                headers: {Accept: "application/json"}
            });

            const ct = res.headers.get("content-type") || "";
            if (!res.ok || !ct.includes("application/json")) {
                const txt = await res.text();
                throw new Error(`Not JSON (status ${res.status})\n${txt.slice(0,200)}`);
            }

            const data = await res.json();
            const list = data.content || [];

            list.forEach(recipe => {
                const article = createFollowArticle(recipe); // utils.js 함수
                followContainer.appendChild(article);
            });

            isLast = !!data.last;
            page += 1;

            if (page === 1 && list.length === 0) {
                followContainer.innerHTML = `<p class="muted">표시할 레시피가 없어요.</p>`;
            }

        }catch(err) {
            console.error("Error:", err);
        } finally {
            isLoading = false;
        }
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

