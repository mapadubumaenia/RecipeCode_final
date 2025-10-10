// 공통 유틸
const $ = (s, el= document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];

document.addEventListener("DOMContentLoaded", () => {
    const emailInput = document.getElementById("email");
    const codeInput = document.getElementById("code");
    const pwInput = document.getElementById("pw");
    const pw2Input = document.getElementById("pw2");
    const formError = document.getElementById("formError");

    const sendBtn = document.getElementById("codebtn");  // 인증코드 발송 버튼
    const resetBtn = document.getElementById("savebtn"); // 비밀번호 재설정 버튼

    let timerElement = document.getElementById("timer");
    let countdown;    // setInterval 핸들러
    let remainingTime = 0;

    // 타이머 시작 함수
    function startTimer() {
        // 기존 타이머 초기화
        clearInterval(countdown);

        // 타이머 엘리먼트가 없으면 생성
        if (!timerElement) {
            timerElement = document.createElement("div");
            timerElement.id = "timer";
            timerElement.style = "margin-top:8px; color:red; font-weight:bold;";
            sendBtn.parentElement.appendChild(timerElement);
        }

        remainingTime = 10 * 60; // 10분 = 600초

        countdown = setInterval(() => {
            const min = String(Math.floor(remainingTime / 60)).padStart(2, "0");
            const sec = String(remainingTime % 60).padStart(2, "0");
            timerElement.textContent = `${min}:${sec}`;
            remainingTime--;

            if (remainingTime < 0) {
                clearInterval(countdown);
                timerElement.textContent = "인증시간 만료";
                alert("인증 시간이 만료되었습니다. 다시 요청해주세요.");

                // 입력 & 버튼 비활성화
                codeInput.disabled = true;
                resetBtn.disabled = true;
            }
        }, 1000);
    }

    // 타이머 초기화 함수
    function resetTimer() {
        clearInterval(countdown);
        timerElement.textContent = "";
        codeInput.disabled = false;
        resetBtn.disabled = false;
    }

    //  인증코드 발송
    sendBtn.addEventListener("click", (e) => {
        e.preventDefault();
        const email = emailInput.value.trim();

        if (!email) {
            alert("이메일을 입력하세요.");
            return;
        }

        fetch("/api/email-certify/send", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        })
            .then(async res => {
                const data = await res.json();
                if (res.ok && data.status === "ok") {
                    alert(data.message); // "인증 메일 발송 완료"
                    resetTimer(); // 타이머 초기화
                    startTimer(); // 타이머 시작
                } else {
                    alert(data.message || "등록되지 않은 이메일입니다.");
                }
            })
            .catch(() => alert("서버 오류가 발생했습니다."));
    });

    //  비밀번호 재설정
    resetBtn.addEventListener("click", (e) => {
        e.preventDefault();
        const email = emailInput.value.trim();
        const code = codeInput.value.trim();
        const password = pwInput.value.trim();
        const password2 = pw2Input.value.trim();

        if (!email || !code || !password || !password2) {
            formError.textContent = "모든 값을 입력하세요.";
            return;
        }
        if (password !== password2) {
            formError.textContent = "비밀번호가 일치하지 않습니다.";
            return;
        }

        // 먼저 코드 검증 요청
        fetch("/api/email-certify/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, code })
        })
            .then(res => res.json())
            .then(data => {
                if (data.status === "ok") {
                    // 검증 성공 시 비밀번호 변경 요청
                    return fetch("/api/email-certify/reset", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ email, code, newPassword: password })
                    });
                } else {
                    throw new Error(data.message || "인증 실패");
                }
            })
            .then(res => res.json())
            .then(data => {
                if (data.status === "ok") {
                    alert("비밀번호가 변경되었습니다. 다시 로그인하세요.");
                    window.location.href = "/auth/login"; // 로그인 페이지로 이동
                } else {
                    formError.textContent = data.message;
                }
            })
            .catch(err => {
                formError.textContent = err.message;
            });
    });
});