// /js/login-to-follow.js
(function () {
    "use strict";
    const CTX = (typeof window !== "undefined" && window.__CTX__) ? window.__CTX__ : "";
    const USER_EMAIL = (typeof window !== "undefined" && window.__USER_EMAIL__) ? String(window.__USER_EMAIL__).trim() : "";

    // 로그인 상태에서만 Login 버튼을 Follow로 교체
    document.addEventListener('DOMContentLoaded', function () {
        try {
            if (!USER_EMAIL) return;

            // href에 /auth/login 이 들어간 모든 a 태그를 대상
            const anchors = Array.from(document.querySelectorAll('a[href*="/auth/login"]'));
            anchors.forEach(a => {
                // 이미 바꿨던 거면 스킵
                if (a.dataset.swapped === "1") return;

                // 텍스트 교체
                a.textContent = 'Follow';
                // 목적지: 백엔드가 me → 내 userId로 리다이렉트
                a.setAttribute('href', (CTX || '') + '/follow/network/me');
                a.setAttribute('title', '내 네트워크로 이동');
                a.setAttribute('aria-label', '내 팔로잉/팔로워 보기');
                a.classList.add('follow-cta'); // 원하면 스타일링
                a.dataset.swapped = "1";
            });
        } catch (e) {
            console.warn('[login-to-follow] swap failed:', e);
        }
    });
})();
