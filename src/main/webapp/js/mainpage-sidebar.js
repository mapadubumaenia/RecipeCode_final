
document.addEventListener("DOMContentLoaded", () => {
    const sidebarTabs = document.querySelectorAll('#myfollowing .tabs .tab');
    const followContainer = document.querySelector("#followContainer");

    // ✅ 비로그인(컨테이너 없음)일 땐 전체 로직 실행하지 않음
    if (!followContainer) return;

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
                headers: { Accept: "application/json" }
            });

            // ✅ 로그인 리다이렉트 등은 조용히 스킵
            if (res.redirected) return;

            const ct = (res.headers.get("content-type") || "").toLowerCase();
            if (!res.ok) {
                console.warn(`[sidebar] http ${res.status}`);
                return;
            }
            // ✅ JSON이 아니면(에러 JSP/HTML) 콘솔 경고만 남기고 종료
            if (!ct.includes("application/json")) {
                // 필요하면 몇 글자만 슬라이스해서 디버그
                // const txt = await res.text(); console.debug(txt.slice(0,120));
                console.warn("[sidebar] non-json response skipped");
                return;
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
                if (currentTab === "following") {
                    followContainer.innerHTML = `
      <div class="card p-16 empty-follow" style="text-align:center; padding:24px;">
        <div style="font-size:32px; line-height:1.2; margin-bottom:8px;">📭</div>
        <p style="margin:4px 0;"><strong>팔로우한 유저가 없어요</strong></p>
        <p class="muted" style="margin:8px 0 0;">관심 있는 셰프를 팔로우하면 새 레시피가 여기 표시됩니다.</p>
      </div>`;
                } else {
                    followContainer.innerHTML = `<p class="muted">표시할 레시피가 없어요.</p>`;
                }
            }

        } catch (err) {
            // ✅ 이제는 error 대신 warn으로만
            console.warn("[sidebar] load failed:", err?.message || err);
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