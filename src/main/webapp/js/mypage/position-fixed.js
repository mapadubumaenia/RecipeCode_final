    const fbtn = document.getElementById("faq-btn")

    window.addEventListener("scroll", () => {
      if (window.scrollY > 300) {  // 300px 이상 스크롤하면 버튼 보이기
        fbtn.style.display = "block";
      } else {
        fbtn.style.display = "none";
      }
  });

    fbtn.addEventListener("click", () => {
      window.scrollTo({ top: 0, behavior: "smooth" });
    });

    const tbtn = document.getElementById("backToTop");

    window.addEventListener("scroll", () => {
      if (window.scrollY > 300) {
        // 300px 이상 스크롤하면 버튼 보이기
        tbtn.style.display = "block";
      } else {
        tbtn.style.display = "none";
      }
    });

    tbtn.addEventListener("click", () => {
      window.scrollTo({ top: 0, behavior: "smooth" });
    });