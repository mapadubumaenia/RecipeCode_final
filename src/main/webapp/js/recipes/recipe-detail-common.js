// 레시피 한마디 더보기/접기

document.querySelectorAll('.toggle').forEach(btn => {
  btn.addEventListener('click', () => {
    // 본문: .desc (자기 부모들 중 .desc 찾기)
    // 댓글: .collapsible (자기 부모들 중 .collapsible 찾기)
    const box = btn.closest('.desc, .collapsible');
    if (!box) return;
    box.classList.toggle('expanded');
    btn.textContent = box.classList.contains('expanded') ? '접기' : '더보기';
  });
});

(function () {
  const wrap = document.querySelector('.comments-box');
  if (!wrap) return;

  const SHOW = parseInt(wrap.getAttribute('data-show-count') || '3', 10); // 기본 3개
  const list = wrap.querySelector('.list');
  const items = Array.from(list.querySelectorAll('.comment'));
  const btn = wrap.querySelector('.toggle');
  const countSpan = document.querySelector('.comments .count');

  // 총 댓글 수 표기 업데이트
  if (countSpan) countSpan.textContent = String(items.length);

  // 숨겨야 할 게 없다면 버튼 숨기기
  if (items.length <= SHOW) {
    btn.hidden = true;
    return;
  }

  // 초기: SHOW 이후의 항목 숨김
  items.slice(SHOW).forEach(el => el.classList.add('hidden'));
  btn.hidden = false;
  btn.textContent = `더보기 (${items.length - SHOW})`;

  let expanded = false;

  btn.addEventListener('click', () => {
    expanded = !expanded;

    if (expanded) {
      // 모두 보이기
      items.forEach(el => el.classList.remove('hidden'));
      btn.textContent = '접기';
    } else {
      // 다시 접기 (SHOW 이후 숨김)
      items.slice(SHOW).forEach(el => el.classList.add('hidden'));
      btn.textContent = `더보기 (${items.length - SHOW})`;
      // 첫 댓글로 스크롤 튀는 거 방지: 필요 시 아래 주석 해제
      // items[SHOW-1]?.scrollIntoView({ block: 'nearest' });
    }
  });
})();

/** ------ 댓글 인라인 수정 ------ **/
const commentsRoot2 = document.querySelector('.comments-box');

if (commentsRoot2) {
  // 수정 버튼 클릭 → 편집 모드 진입
  commentsRoot2.addEventListener('click', (e) => {
    const editBtn = e.target.closest('.edit');
    if (!editBtn) return;

    const commentEl = editBtn.closest('.comment');
    if (!commentEl) return;

    // 이미 편집 중이면 무시
    if (commentEl.classList.contains('editing')) return;

    // 본문 엘리먼트 찾기 (가능하면 .pl-12 대신 .cmtBody 같은 전용 클래스로!)
    const bodyEl = commentEl.querySelector('.pl-12');
    if (!bodyEl) return;

    const original = bodyEl.textContent.trim();

    // 상태 토글
    commentEl.classList.add('editing');
    editBtn.disabled = true;

    // 에디터 DOM 만들기
    const editor = document.createElement('div');
    editor.innerHTML = `
      <textarea class="edit-textarea">${escapeHtml(original)}</textarea>
      <div class="edit-controls">
        <button type="button" class="btn btn--sm save-edit">저장</button>
        <button type="button" class="btn btn--sm btn--ghost cancel-edit">취소</button>
      </div>
    `;

    // 기존 내용 숨기고 에디터 삽입
    bodyEl.dataset.original = original;      // 취소용 백업
    bodyEl.hidden = true;
    bodyEl.insertAdjacentElement('afterend', editor);

    const textarea = editor.querySelector('.edit-textarea');
    textarea.focus();
    textarea.setSelectionRange(textarea.value.length, textarea.value.length);
  });

  // 저장/취소 클릭, 키보드 단축키 처리
  commentsRoot2.addEventListener('click', async (e) => {
    const isSave = e.target.closest('.save-edit');
    const isCancel = e.target.closest('.cancel-edit');
    if (!isSave && !isCancel) return;

    const editorWrap = e.target.closest('.edit-controls')?.parentElement;
    const commentEl = e.target.closest('.comment');
    const bodyEl = commentEl?.querySelector('.pl-12');
    const editBtn = commentEl?.querySelector('.edit');

    if (!editorWrap || !commentEl || !bodyEl || !editBtn) return;

    const textarea = editorWrap.querySelector('.edit-textarea');
    const cid = commentEl.dataset.cid;
    const newText = (textarea?.value || '').trim();

    // 취소
    if (isCancel) {
      cleanupEdit(commentEl, bodyEl, editorWrap, editBtn);
      return;
    }

    // 저장
    if (!newText) {
      textarea.focus();
      return;
    }

    // 옵티미스틱 업데이트
    bodyEl.textContent = newText;

    try {
      // 실제 API 호출로 교체하세요
      await updateComment(cid, newText);
    } catch (err) {
      console.error(err);
      // 실패 시 롤백 (원문 복원)
      bodyEl.textContent = bodyEl.dataset.original || bodyEl.textContent;
      // TODO: 토스트로 실패 안내해도 좋음
    } finally {
      cleanupEdit(commentEl, bodyEl, editorWrap, editBtn);
    }
  });

  // Ctrl+Enter = 저장, Esc = 취소
  commentsRoot2.addEventListener('keydown', (e) => {
    const textarea = e.target.closest('.edit-textarea');
    if (!textarea) return;
    const editorWrap = textarea.parentElement;
    if (!editorWrap) return;

    if (e.key === 'Escape') {
      editorWrap.querySelector('.cancel-edit')?.click();
    }
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      editorWrap.querySelector('.save-edit')?.click();
    }
  });
}

// 공용 정리 함수
function cleanupEdit(commentEl, bodyEl, editorWrap, editBtn) {
  editorWrap.remove();
  bodyEl.hidden = false;
  delete bodyEl.dataset.original;
  commentEl.classList.remove('editing');
  editBtn.disabled = false;
}

// 실제 PATCH API로 교체할 부분
async function updateComment(commentId, content) {
  // await fetch(`/api/comments/${commentId}`, {
  //   method: 'PATCH',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({ content })
  // });
  await new Promise(r => setTimeout(r, 200)); // 데모용
}


/** ------ 대댓글 토글/로드/등록 ------ **/
const commentsRoot = document.querySelector('.comments-box'); // 댓글 목록 래퍼

if (commentsRoot) {
  // 1) "답글" 버튼 토글 (리스너 1개면 전체 커버)
  commentsRoot.addEventListener('click', async (e) => {
    const btn = e.target.closest('.reply'); // 답글 버튼?
    if (!btn) return;

    const commentEl = btn.closest('.comment');
    if (!commentEl) return;

    // replies 컨테이너 보장 (없으면 생성)
    let repliesBox = commentEl.querySelector('.replies');
    if (!repliesBox) {
      repliesBox = document.createElement('div');
      repliesBox.className = 'replies';
      repliesBox.hidden = true;
      repliesBox.innerHTML = `
        <div class="reply-input">
          <img class="avatar avatar--sm" src="https://placehold.co/40x40" alt="" />
          <textarea class="reply-text" placeholder="답글 달기..."></textarea>
          <button type="button" class="btn btn--sm submit-reply">등록</button>
        </div>
        <div class="reply-list"></div>`;
      commentEl.appendChild(repliesBox);
    }

    const expanded = btn.getAttribute('aria-expanded') === 'true';
    if (!expanded) {
      // 처음 열 때만 로드 (이미 로드했으면 스킵)
      if (!repliesBox.dataset.loaded) {
        await loadRepliesForComment(commentEl);
        repliesBox.dataset.loaded = '1';
      }
      repliesBox.hidden = false;
      btn.setAttribute('aria-expanded', 'true');
    } else {
      repliesBox.hidden = true;
      btn.setAttribute('aria-expanded', 'false');
    }
  });

  // 2) "등록" 클릭 → 대댓글 추가 (옵티미스틱)
  commentsRoot.addEventListener('click', async (e) => {
    const submit = e.target.closest('.submit-reply');
    if (!submit) return;

    const commentEl = submit.closest('.comment');
    const repliesBox = submit.closest('.replies');
    const textarea = repliesBox.querySelector('.reply-text');
    const text = (textarea.value || '').trim();
    if (!text) { textarea.focus(); return; }

    // (선택) 유저 프로필은 로그인 연결 후 실제 데이터로 교체
    const me = { name: '나', avatar: 'https://placehold.co/40x40', createdAt: '방금 전' };

    // 옵티미스틱 렌더
    appendReply(repliesBox.querySelector('.reply-list'), {
      author: me.name, avatar: me.avatar, createdAt: me.createdAt, content: text
    });

    // 카운트 +1
    const countSpan = commentEl.querySelector('.reply span');
    if (countSpan) countSpan.textContent = String(parseInt(countSpan.textContent || '0', 10) + 1);

    textarea.value = '';

    // 실제 전송 (백엔드 붙이면 여기만 교체)
    try {
      const cid = commentEl.dataset.cid;
      await postReply(cid, text); // ← API 붙이면 성공/실패 처리
    } catch (err) {
      // 실패 시 롤백하거나 토스트 안내
      console.error(err);
    }
  });
}

// ---- 헬퍼들 ----

// 대댓글 로더 (백엔드 GET 연결 지점)
async function loadRepliesForComment(commentEl) {
  const cid = commentEl.dataset.cid;
  const repliesBox = commentEl.querySelector('.replies');
  const listEl = repliesBox.querySelector('.reply-list');

  // 1) 실제 API 예시
  // const res = await fetch(`/api/comments/${cid}/replies`);
  // const data = await res.json(); // [{author, avatar, createdAt, content}, ...]
  // 2) 데모용 더미 (카운트와 맞춰 2개 생성)
  const countText = commentEl.querySelector('.reply span')?.textContent || '0';
  const mockCount = Math.min(parseInt(countText, 10) || 0, 3);
  const data = Array.from({ length: mockCount }).map((_, i) => ({
    author: ['바질파','면수장인','치즈펑펑'][i % 3] || `게스트${i+1}`,
    avatar: 'https://placehold.co/40x40',
    createdAt: `${i+1}시간 전`,
    content: ['공감해요!', '팁 감사합니다 🙌', '이 조합 취향저격이네요'][i % 3]
  }));

  listEl.innerHTML = '';
  data.forEach(item => appendReply(listEl, item));
}

// 대댓글 추가 렌더러
function appendReply(listEl, { author, avatar, createdAt, content }) {
  const el = document.createElement('article');
  el.className = 'reply-item';
  el.innerHTML = `
    <img class="avatar avatar--sm" src="${avatar}" alt="">
    <div>
      <div class="name">${escapeHtml(author)} <span class="createdAt">${escapeHtml(createdAt)}</span></div>
      <div class="pl-12">${escapeHtml(content)}</div>
    </div>
  `;
  listEl.appendChild(el);
}

// 대댓글 POST (백엔드 POST 연결 지점)
async function postReply(commentId, text) {
  // 실제 예시:
  // await fetch(`/api/comments/${commentId}/replies`, {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({ content: text })
  // });
  await new Promise(r => setTimeout(r, 200)); // 데모용
}

// XSS 가드 (아주 가볍게)
function escapeHtml(s) {
  return s.replace(/[&<>"']/g, m => ({
    '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
  }[m]));
}


/** ------ 무한 스크롤 사이드바 ------ **/
const side = document.getElementById('sideList');
const loader = document.getElementById('loader');
const sentinel = document.getElementById('sentinel');

let page = 0;
let isLoading = false;
let isEnd = false;

// 카드 템플릿
function cardTemplate({thumb, title, author, views}) {
  const wrap = document.createElement('div');
  wrap.className = 'card';
  wrap.innerHTML = `
    <img class="thumb" src="${thumb}" alt="">
    <div class="info">
      <p class="ttl">${title}</p>
      <p class="sub">${author} · 조회수 ${views.toLocaleString()}</p>
    </div>`;
  return wrap;
}

// 가짜 API
async function fetchRecommendations(pageNum){
  await new Promise(r=>setTimeout(r, 700));
  const pageSize = 6;
  const maxPage = 4;
  if(pageNum >= maxPage) return { items: [], end:true };

  const start = pageNum * pageSize;
  const items = Array.from({length: pageSize}).map((_,i)=>({
    thumb: `https://picsum.photos/seed/${start+i+1}/320/180`,
    title: `추천 레시피 #${start+i+1} — 10분 완성 꿀팁`,
    author: ['Chef Riddle','소금후추','주말요리사'][ (start+i)%3 ],
    views: 1000 + (start+i)*37
  }));
  return { items, end:false };
}

async function loadMore(){
  if(isLoading || isEnd) return;
  isLoading = true;
  loader.hidden = false;

  const {items, end} = await fetchRecommendations(page++);
  items.forEach(item => side.insertBefore(cardTemplate(item), loader));
  loader.hidden = true;

  if(end || items.length === 0){
    isEnd = true;
    observer.disconnect();
    const done = document.createElement('div');
    done.className = 'end';
    done.textContent = '추천 레시피 끝!';
    side.appendChild(done);
  }
  isLoading = false;
}

const observer = new IntersectionObserver((entries)=>{
  entries.forEach(entry=>{
    if(entry.isIntersecting){ loadMore(); }
  });
}, { root: null, rootMargin: '0px 0px 200px 0px', threshold: 0 });

loadMore();
observer.observe(sentinel);

window.addEventListener('load', ()=>{
  if(side.getBoundingClientRect().height < window.innerHeight*0.8){ loadMore(); }
});