(function setupSideReco(){
    const $list     = document.getElementById('sideList');     // 사이드 컨테이너
    const $loader   = document.getElementById('loader');       // 로더
    const $sentinel = document.getElementById('sentinel');     // 관찰 지점
    if (!$list || !$loader || !$sentinel) return;

    // === 메인과 동일한 네이밍 ===
    let pageSize    = 6;
    let nextCursor  = null;
    let busy        = false;

    // 로그인 유저 이메일 (메인과 동일 컨벤션. 전역이 없으면 빈 문자열)
    const USER_EMAIL = (window.USER_EMAIL || window.currentUserEmail || '');

    // --- 유틸 (메인과 호환) ---
    const esc = (s) => (s==null?'':String(s))
        .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

    const isUuid36 = (s) => typeof s === 'string' && s.length === 36 && s.indexOf('-')>0;

    const detailUrl = (idOrUuid) => (window.ctx ? (window.ctx + '/recipes/' + idOrUuid) : ('/recipes/' + idOrUuid));


    // 썸네일 픽커 (필드 이름 다양성 흡수)
    function pickThumb(it){
        return it.poster || it.mediaSrc || it.thumb || it.thumbnailUrl || it.thumbnail || it.image || 'https://placehold.co/320x180';
    }

    // 태그
    function tagsHtml(it){
        if (!it.tags || !it.tags.length) return '';
        // 너무 길면 상위 3개만
        const chips = it.tags.slice(0, 3).map(t => '<span class="tag">#'+ esc(t) +'</span>').join(' ');
        return '<p class="tags">'+ chips +'</p>';
    }

    // 시간 포맷
    const KTZ = 'Asia/Seoul';

    function toLocalDate(iso){
        // ISO "…Z"는 UTC → Date로 파싱하면 내부적으로 로컬로 변환됨
        const d = new Date(iso);
        return isNaN(d) ? null : d;
    }

    function fmtDateK(d){
        // 2025. 9. 24. 16:15 형식 (로캘/타임존 한글)
        return d.toLocaleString('ko-KR', { timeZone: KTZ, year:'numeric', month:'numeric', day:'numeric', hour:'2-digit', minute:'2-digit' });
    }

    const rtf = new Intl.RelativeTimeFormat('ko', { numeric: 'auto' });
    function timeAgoK(d){
        const now = new Date();
        const diffMs = d - now; // 음수(과거)
        const sec = Math.round(diffMs / 1000);
        const min = Math.round(sec / 60);
        const hour = Math.round(min / 60);
        const day = Math.round(hour / 24);
        if (Math.abs(sec) < 60)  return rtf.format(sec, 'second');
        if (Math.abs(min) < 60)  return rtf.format(min, 'minute');
        if (Math.abs(hour) < 24) return rtf.format(hour, 'hour');
        if (Math.abs(day)  < 7)  return rtf.format(day,  'day');
        // 7일 넘으면 날짜로
        return fmtDateK(d);
    }

    // === 메인과 동일하게 URL 조립 (개인화/핫피드) ===
    function buildUrl(){
        let url;
        if (USER_EMAIL && USER_EMAIL.length > 0) {
            url = '/api/feed/personal?userEmail=' + encodeURIComponent(USER_EMAIL);
        } else {
            url = '/api/feed/hot?';
        }
        if (url.indexOf('?') === -1) url += '?'; else if (!/[&?]$/.test(url)) url += '&';
        if (nextCursor) url += 'after=' + encodeURIComponent(nextCursor) + '&';
        url += 'size=' + encodeURIComponent(pageSize);
        return url;
    }

    // === 사이드용 미니 카드 (썸네일 + 제목 + 조회수) ===
    function miniCardHtml(it){
        const rid   = (it.id || it.uuid || it._id || '').toString();
        const href  = isUuid36(rid) ? detailUrl(rid) : '#';
        const thumb = pickThumb(it);
        const title = it.title || it.recipeTitle || '(제목 없음)';
        const views = (typeof it.views === 'number') ? it.views
            : (typeof it.viewCount === 'number') ? it.viewCount
                : (it.views || it.viewCount || 0);

        const created = it.createdAt ? timeAgoK(toLocalDate(it.createdAt)) : '';
        const subParts = [];
        if (Number.isFinite(Number(views)) && Number(views) > 0) subParts.push('조회수 ' + Number(views).toLocaleString());
        if (created) subParts.push(created);
        const subLine = subParts.length ? ('<p class="sub">'+ subParts.join(' · ') +'</p>') : '';

        return ''
            + '<a class="card side-reco" href="'+esc(href)+'" aria-label="'+esc(title)+'">'
            + '  <img class="thumb" src="'+esc(thumb)+'" alt="">'
            + '  <div class="info">'
            + '    <p class="ttl">'+esc(title)+'</p>'
            +       subLine
            +       tagsHtml(it)
            + '  </div>'
            + '</a>';
    }

    async function loadMore(){
        if (busy) return;
        busy = true;
        $loader.hidden = false;

        try{
            // 1) 메인과 동일한 추천 피드 호출 (개인화/핫피드)
            const url = buildUrl();
            const res = await fetch(url, { headers:{'Accept':'application/json'}, credentials:'same-origin' });
            if (!res.ok) throw new Error('HTTP '+res.status);
            const data = await res.json();

            // items 렌더
            if (data && Array.isArray(data.items) && data.items.length){
                const frag = document.createDocumentFragment();
                for (let i=0;i<data.items.length;i++){
                    const temp = document.createElement('div');
                    temp.innerHTML = miniCardHtml(data.items[i]);
                    frag.appendChild(temp.firstChild);
                }
                // 로더 앞에 삽입
                $list.insertBefore(frag, $loader);
            }

            // next 커서 갱신 (없으면 null)
            nextCursor = (data && data.next) ? data.next : null;

            // 2) 커서 없는 구조이거나, 더 이상 로드할 게 없으면 옵저버 해제 + 끝 마크
            if (!nextCursor){
                observer && observer.disconnect();
                if (!document.getElementById('sideEndMark')){
                    const done = document.createElement('div');
                    done.id = 'sideEndMark';
                    done.className = 'end';
                    done.textContent = '추천 레시피 끝!';
                    $list.appendChild(done);
                }
            }
        }catch(err){
            console.error('[side-reco]', err);
            // 백업 플랜: ES 트렌딩 API 한 번이라도 채워넣기
            try{
                const backup = await fetch('/api/home/trending?days=7&size='+encodeURIComponent(pageSize), {credentials:'same-origin'});
                if (backup.ok){
                    const jd = await backup.json();
                    if (jd && Array.isArray(jd.items) && jd.items.length){
                        const frag = document.createDocumentFragment();
                        jd.items.forEach(it=>{
                            const temp = document.createElement('div');
                            temp.innerHTML = miniCardHtml(it);
                            frag.appendChild(temp.firstChild);
                        });
                        $list.insertBefore(frag, $loader);
                    }
                }
            }catch(_e){
                // 조용히 무시
            }
        }finally{
            $loader.hidden = true;
            busy = false;
        }
    }

    // === IntersectionObserver (미리 로드 마진 크게)
    const observer = ('IntersectionObserver' in window)
        ? new IntersectionObserver((entries)=>{
            entries.forEach(entry=>{
                if (entry.isIntersecting && !busy && (nextCursor!==null || $list.children.length<=2)){
                    loadMore();
                }
            });
        }, { root:null, rootMargin:'600px 0px', threshold:0 })
        : null;

    // 초기 로드
    loadMore();
    if (observer) observer.observe($sentinel);
})();