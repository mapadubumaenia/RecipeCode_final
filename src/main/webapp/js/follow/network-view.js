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
        userNickname: m.nickname ?? "",
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
    const followText = d.followingStatus ? "UnFollow" : "Follow";
    const followCls  = d.followingStatus ? "is-following" : "";
    const hideBtn = email === viewerEmail;

    el.innerHTML = `
    <div class="post-head flex-row">
      <div class="leftBox jump flex-box" data-uid="${d.userId}">
      <div class="avatar-ss">
        <img src="${d.profileImageUrl || ""}" alt="${d.userId}">
      </div>
      <div class="post-info ml-8">
        <div class="post-id">${d.userId}</div>
        <div class="muted">${d.userNickname} ${d.userLocation}</div>
      </div>
      </div>
      <div class="rightBox">
 ${hideBtn ? "" : `
        <button class="follow-btn ${followCls}"
                data-email="${email}"
                data-following="${String(d.followingStatus)}"
                data-uid="${d.userId}"
                >${followText}</button>
`}
      </div>
    </div>
`;
    return el;
}

// 유저 카드 클릭 → 이동
document.addEventListener("click", (e) => {
    const jump = e.target.closest(".jump");
    if (!jump) return;

    if (e.target.closest(".follow-btn")) return;

    const uid = jump.dataset.uid;
    if(!uid) return;
        // TODO: 여기여기 url 수정
        window.location.href = `${ctx}/follow/profile/${encodeURIComponent(uid)}`;
    });

function resetAndReload(kind) {
    const S = state[kind];
    S.page = 0;
    S.last = false;
    S.loading = false;
    (kind === "following" ? followingListEl : followerListEl).innerHTML = "";
    return load(kind);
}

function refreshAfterChange() {
    if (isDesktop()) {
        // 동시 재로딩
        return Promise.all([resetAndReload("following"), resetAndReload("follower")]);
    } else {
        const active = document.querySelector(".list-pane.active")?.dataset.pane || "following";
        return resetAndReload(active);
    }
}


document.addEventListener("click", async (e) => {
    const btn = e.target.closest(".follow-btn");
    if (!btn) return;

    const email = btn.dataset.email;
    if (!email) {console.warn("no email on button"); return; }

    const following = btn.dataset.following === "true";
    const method = following ? "DELETE" : "POST";
    const url = `${ctx}/api/follow/${encodeURIComponent(email)}`;
    const who = btn.dataset.uid || email;

    // 언팔로우일 때만 확인
    if (following) {
        const ok = window.confirm(`정말 ${who} 님을 언팔로우하시겠습니까?`);
        if (!ok) return;
    }


    btn.disabled = true;
    try {
        const res = await fetch(url,{
            method,
            credentials: "include"
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // 버튼 즉시 반영
        const now = !following;
        btn.dataset.following = String(now);
        btn.textContent = now ? "UnFollow" : "Follow";
        btn.classList.toggle("is-following", now);

        // 팔로우/언팔로우 즉시 리스트 새로 불러오기
        await refreshAfterChange();

    } catch (err) {
        console.error(err);
        alert("실패했습니다. 다시 시도해주세요.");
    } finally {
        btn.disabled = false;
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