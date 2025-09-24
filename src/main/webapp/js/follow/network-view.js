// network-view.js

// 헬퍼: 현재 데스크탑인지
const isDesktop = () => window.matchMedia("(min-width: 768px)").matches;

// 서버에서 내려온 값
// const ctx = typeof ctx === "string" ? ctx : "";
const ownerEmail = (typeof profileOwnerEmail === "string" && profileOwnerEmail)
    ? profileOwnerEmail
    : (typeof currentUserEmail === "string" ? currentUserEmail : "");

// DOM
const followingListEl = document.getElementById("followingList");
const followerListEl  = document.getElementById("followerList");
const btnMoreFollowing = document.getElementById("btnMoreFollowing");
const btnMoreFollower  = document.getElementById("btnMoreFollower");
const tabs = document.querySelectorAll('[data-follow-tab]');

// 상태
const state = {
    following: { page: 0, size: 10, last: false, loading: false },
    follower:  { page: 0, size: 10, last: false, loading: false }
};

// API URL 생성 (컨트롤러 시그니처와 정확히 일치!)
function apiUrl(kind) {
    const base = `${ctx}/api/follow/${encodeURIComponent(ownerEmail)}`;
    if (kind === "following") return `${base}/following?page=${state.following.page}&size=${state.following.size}`;
    if (kind === "follower")  return `${base}/follower?page=${state.follower.page}&size=${state.follower.size}`;
    throw new Error("unknown kind");
}


function pick(row) {
    const m = row.member ?? row;                           // 유저 정보

    return {
        userId: m.userId ?? "unknown",
        userEmail: m.userEmail ?? "unknown",
        userNickname: m.userNickname ?? "",
        profileImageUrl: m.profileImageUrl ?? "",
        userLocation: m.userLocation ?? "",
        followingStatus: !!(row.followingStatus ?? false),
        followerStatus: !!(row.followerStatus ?? false),
    };
}

// 초기 로딩
if (isDesktop()) {
    if (state.following.page === 0) load("following");
    if (state.follower.page  === 0) load("follower");
} else {
    // 모바일은 기본 탭(following)만 먼저
    if (state.following.page === 0) load("following");
}

// 뷰포트가 데스크탑으로 바뀌면, 아직 안 불러온 쪽도 불러오기
const mq = window.matchMedia("(min-width: 768px)");
mq.addEventListener("change", (e) => {
    if (e.matches) { // 데스크탑 진입
        if (state.following.page === 0) load("following");
        if (state.follower.page  === 0) load("follower");
    }
});

function renderItem(row) {
    const d = pick(row);
    const el = document.createElement("article");
    el.className = "card p-12 post";
    const email = d.userEmail;

    const profileHref = `/follow/profile/${encodeURIComponent(
        d.userId.startsWith("@") ? d.userId.slice(1) : d.userId
    )}`;

    const followText = d.followingStatus ? "UnFollow" : "Follow";


    el.innerHTML = `
    <div class="post-head">
      <div class="avatar-ss">
        <img src="${d.profileImageUrl || ""}" alt="${d.userId}">
      </div>
      <div class="post-info">
        <div class="post-id">${d.userId}</div>
        <div class="muted">${d.userNickname}</div>
      </div>
      <div class="rightBox">
        <button class="follow-btn"
                data-email="${email}"
                data-following="${String(d.followingStatus)}"
                >${followText}</button>
      </div>
    </div>
`;
    return el;
}

document.addEventListener("click", async (e) => {
    const btn = e.target.closest(".follow-btn");
    if (!btn) return;

    const email = btn.dataset.email;
    if (!email) {console.warn("no email on button"); return; }

    const following = btn.dataset.following === "true";
    const method = following ? "DELETE" : "POST";
    const url = `${ctx}/api/follow/${encodeURIComponent(email)}`;


    // btn.disabled = true;
    try {
        const res = await fetch(url,{
            method,
            credentials: "include"
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // UI 즉시 갱신
        const now = !following;
        btn.dataset.following = String(now);
        btn.textContent = now ? "UnFollow" : "Follow";

        // 만약 '팔로잉' 리스트에서 언팔하면 카드 제거하고 싶으면:
        // if (!now && /* 현재 pane이 following이면 */) btn.closest("article")?.remove();
    } catch (err) {
        console.error(err);
        alert("실패했습니다. 다시 시도해주세요.");
    }
});


// 로드 공통
async function load(kind) {
    const S = state[kind];
    if (S.loading || S.last) return;
    S.loading = true;
    try {
        const res = await fetch(apiUrl(kind), { credentials: "include" });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json(); // Slice<FollowDto>
        const list = (data && Array.isArray(data.content)) ? data.content : [];

        const targetEl = (kind === "following") ? followingListEl : followerListEl;
        list.forEach(u => targetEl.appendChild(renderItem(u)));

        S.last = !!data.last;
        S.page += 1;

        // 더보기 버튼 토글
        if (kind === "following") {
            btnMoreFollowing.style.display = S.last ? "none" : "block";
        } else {
            btnMoreFollower.style.display = S.last ? "none" : "block";
        }
    } catch (e) {
        console.error(`[${kind}] load error:`, e);
    } finally {
        S.loading = false;
    }
}

// 탭 전환 (모바일에서만 한쪽씩 보이게 쓰는 경우)
tabs.forEach(tab => {
    tab.addEventListener("click", () => {
        const target = tab.dataset.followTab; // "following" | "follower"
        tabs.forEach(t => t.classList.toggle("is-active", t === tab));

        document.querySelectorAll(".list-pane").forEach(pane => {
            pane.classList.toggle("active", pane.dataset.pane === target);
        });

        // 최초 로드 안됐으면 가져오기
        if (state[target].page === 0) load(target);

        // 모바일에서는 탭 전환 후 스크롤 살짝 올리기
        if (window.matchMedia("(max-width: 1023px)").matches) {
            const activePane = document.querySelector(`.list-pane.active`);
            activePane?.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    });
});

// 더보기
btnMoreFollowing?.addEventListener("click", () => load("following"));
btnMoreFollower?.addEventListener("click", () => load("follower"));

// 초기: following 먼저 로드
load("following");