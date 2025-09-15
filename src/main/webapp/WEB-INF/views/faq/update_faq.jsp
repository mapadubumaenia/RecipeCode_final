<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="card">
  <div class="hd">
    <h3>FAQ 수정/삭제</h3>
  </div>
  <div class="bd">
    <form id="faqForm" name="faqForm" method="post">
      <!-- CSRF -->
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <!-- PK -->
      <input type="hidden" name="faq_num" value="<c:out value='${faq.faq_num}'/>">

      <div class="mb-12">
        <label for="faq_question">질문</label>
        <input type="text" id="faq_question" name="faq_question"
               class="input"
               value="<c:out value='${faq.faq_question}'/>" required />
      </div>

      <div class="mb-12">
        <label for="faq_answer">답변</label>
        <textarea id="faq_answer" name="faq_answer" rows="5"
                  class="input" required><c:out value='${faq.faq_answer}'/></textarea>
      </div>

      <div class="mb-12">
        <label for="faq_tag">태그</label>
        <input type="text" id="faq_tag" name="faq_tag"
               class="input"
               value="<c:out value='${faq.faq_tag}'/>" />
      </div>

      <div class="actions">
        <button type="button" class="btn green" onclick="fn_save()">수정</button>
        <button type="button" class="btn red" onclick="fn_delete()">삭제</button>
      </div>
    </form>
  </div>
</div>

<!-- jQuery -->
<script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
<script>
  function fn_save() {
    $("#faqForm").attr("action", "/faq/update").submit();
  }
  function fn_delete() {
    if (confirm("정말 삭제하시겠습니까?")) {
      $("#faqForm").attr("action", "/faq/delete").submit();
    }
  }
</script>
