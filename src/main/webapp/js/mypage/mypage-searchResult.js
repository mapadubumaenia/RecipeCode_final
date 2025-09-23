document.addEventListener("DOMContentLoaded", () => {
    const searchBtn = document.querySelector("#searchBtn");
    const searchInput = document.querySelector("#searchInput");
    const searchResult = document.querySelector("#searchResult");

    // 공통 검색 함수
    function doSearch(keyword) {
        if (!keyword) {
            searchResult.innerHTML = "";
            return;
        }

        fetch(`/api/mypage/search?keyword=${encodeURIComponent(keyword)}`)
            .then(res => res.json())
            .then(users => {
                if (!users || users.length === 0) {
                    searchResult.innerHTML = "<p>사용자를 찾을 수 없습니다.</p>";
                    return;
                }

                searchResult.innerHTML = users.map(user => `
                  <div class="card p-12 search-user flex-box" data-user-id="${user.userId}">
                    <div class="leftBox flex-row">
                        <div class="avatar-ss"><img src="${user.profileImageUrl || ''}" alt=""></div>
                        <span>${user.userId}</span>
                    </div>
                        <div class="rightBox flex-row">
                            <div>${user.userIntroduce}</div>
                        </div>
                  </div>
                `).join("");

                // 검색된 유저 클릭 → 이동
                document.querySelectorAll(".search-user").forEach(el => {
                    el.addEventListener("click", () => {
                        const uid = el.dataset.userId;
                        // TODO: 여기여기 url 수정
                        window.location.href = "/mypage/" + uid;
                    });
                });
            })
            .catch(err => {
                console.error("검색 오류:", err);
                searchResult.innerHTML = "<p>검색 실패 😢</p>";
            });
    }

    // 버튼 검색
    searchBtn.addEventListener("click", () => {
        const keyword = searchInput.value.trim();
        doSearch(keyword);
    });

    // 입력 자동 검색 (debounce 적용)
    let timer;
    searchInput.addEventListener("input", () => {
        clearTimeout(timer);
        const keyword = searchInput.value.trim();
        timer = setTimeout(() => doSearch(keyword), 100);
    });
});