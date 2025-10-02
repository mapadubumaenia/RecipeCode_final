<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <%@ include file="/WEB-INF/views/common/head.jsp" %>
  <title>Feed</title>
</head>
<body>

<section id="feed">
  <c:forEach var="it" items="${items}">
    <article class="card">
      <h3><c:out value="${it.title}" default=""/></h3>
      <p class="meta">
        <span><c:out value="${it.author}" default=""/></span>
        <span> · ❤ <c:out value="${it.likes}" default="0"/></span>
        <span> · <c:out value="${it.createdAt}" default=""/></span>
        <c:if test="${it.recScore > 0}">
          <span> · score <c:out value="${it.recScore}" default="0"/></span>
        </c:if>
      </p>
      <p class="tags">
        <c:forEach var="tg" items="${it.tags}">
          <span class="tag">#<c:out value="${tg}" default=""/></span>
        </c:forEach>
      </p>
    </article>
  </c:forEach>
</section>

<c:if test="${next ne null}">
  <button id="loadMore"
          data-next="${next}"
          data-user="${userId}"
          data-size="${size}">더 보기</button>
</c:if>

<script>
  (async () => {
    const btn = document.getElementById('loadMore');
    if (!btn) return;

    btn.addEventListener('click', async () => {
      const user  = btn.dataset.user || '';
      const after = btn.dataset.next ?? '';
      const size  = btn.dataset.size ?? 20;

      // ★ userId 유무에 따라 personal/hot 분기
      const url = (user.trim().length > 0)
              ? `/api/feed/personal?userId=\${encodeURIComponent(user)}&after=\${encodeURIComponent(after)}&size=\${size}`
              : `/api/feed/hot?after=\${encodeURIComponent(after)}&size=\${size}`;

      const res = await fetch(url);
      if (!res.ok) return;

      const data = await res.json();
      const wrap = document.getElementById('feed');

      for (const it of data.items) {
        const el = document.createElement('article');
        el.className = 'card';
        el.innerHTML = `
          <h3>\${it.title ?? ''}</h3>
          <p class="meta">
            <span>\${it.author ?? ''}</span>
            <span> · ❤ \${it.likes ?? 0}</span>
            <span> · \${it.createdAt ?? ''}</span>
            \${(it.recScore && it.recScore > 0) ? ('<span> · score ' + it.recScore + '</span>') : ''}
          </p>
          <p class="tags">\${(it.tags || []).map(t => '<span class="tag">#' + t + '</span>').join(' ')}</p>
        `;
        wrap.appendChild(el);
      }

      if (data.next) {
        btn.dataset.next = data.next;
      } else {
        btn.remove();
      }
    });
  })();
</script>

</body>
</html>
