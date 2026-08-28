<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.Year" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="kopo.kkeudeok.dto.ChildDTO" %>
<%@ page import="java.time.LocalDateTime" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "child"; %>
<%@ include file="../common/app-top.jsp" %>

<%
    ChildDTO child = (ChildDTO) request.getAttribute("child");

    String childName = "";
    String gender = "";
    String disorderType = "자폐 장애";
    String severity = "중증";
    Long childId = null;
    String charKey = "tori";
    String createdAt = "";

    int selYear = LocalDate.now().getYear();
    int selMonth = LocalDate.now().getMonthValue();
    int selDay = LocalDate.now().getDayOfMonth();
    int childAge = 0;

    if (child != null) {
        childName = child.getName() != null ? child.getName() : "";
        gender = child.getGender() != null ? child.getGender() : "";
        disorderType = (child.getDisorderType() != null && !child.getDisorderType().trim().isEmpty()) ? child.getDisorderType().trim() : "자폐 장애";
        severity = (child.getSeverity() != null && !child.getSeverity().trim().isEmpty()) ? child.getSeverity().trim() : "중증";
        childId = child.getChildId();

        if (child.getCharacterType() != null && !child.getCharacterType().isEmpty()) {
            charKey = child.getCharacterType();
        }

        if (child.getCreatedAt() != null) {
            createdAt = child.getCreatedAt().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        }

        if (child.getBirthDate() != null) {
            try {
                String bStr = child.getBirthDate().toString();
                if (bStr.length() >= 10) {
                    selYear = Integer.parseInt(bStr.substring(0, 4));
                    selMonth = Integer.parseInt(bStr.substring(5, 7));
                    selDay = Integer.parseInt(bStr.substring(8, 10));

                    LocalDate birth = LocalDate.of(selYear, selMonth, selDay);
                    childAge = Period.between(birth, LocalDate.now()).getYears();
                }
            } catch (Exception e) {
            }
        }
    }

    boolean isAutism = disorderType.contains("자폐");
    boolean isIntellectual = disorderType.contains("지적");
    boolean isDevelopmental = disorderType.contains("발달");
    if (!isAutism && !isIntellectual && !isDevelopmental) {
        isAutism = true;
    }

    boolean isMild = severity.contains("경");
    boolean isSevere = severity.contains("중");
%>

<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <div class="mp-profile">
        <span class="ava"><img data-kd-char="neutral" src="/img/char-<%= charKey %>-neutral.png" alt="<%= childName %>"></span>
        <span class="tx">
            <b data-kd="childName"><%= childName %></b>
            <span class="ds">만 <span data-kd="childAge"><%= childAge %></span>세 · 학습 시작 <%= createdAt %></span>
        </span>
    </div>
</section>

<section class="mp-sec">
    <h2>아이 정보</h2>

    <input type="hidden" id="childId" value="<%= childId != null ? childId : "" %>">

    <div class="mp-grid mp-grid--child">
        <div class="kd-field">
            <label class="kd-label" for="childName">아이 이름</label>
            <input class="kd-input" type="text" id="childName" value="<%= childName %>" autocomplete="off">
        </div>

        <div class="kd-field">
            <label class="kd-label" for="birthYear">생년월일</label>
            <div class="onb-row">
                <select class="kd-input onb-select" id="birthYear" name="birthYear" autocomplete="off">
                    <% for (int y = Year.now().getValue(); y >= 1990; y--) { %>
                    <option value="<%= y %>"<%= y == selYear ? " selected" : "" %>><%= y %></option>
                    <% } %>
                </select>
                <span class="onb-unit">년</span>
                <select class="kd-input onb-select" id="birthMonth" name="birthMonth" autocomplete="off">
                    <% for (int m = 1; m <= 12; m++) { %>
                    <option value="<%= m %>"<%= m == selMonth ? " selected" : "" %>><%= m %></option>
                    <% } %>
                </select>
                <span class="onb-unit">월</span>
                <select class="kd-input onb-select" id="birthDay" name="birthDay" autocomplete="off">
                    <% for (int d = 1; d <= 31; d++) { %>
                    <option value="<%= d %>"<%= d == selDay ? " selected" : "" %>><%= d %></option>
                    <% } %>
                </select>
                <span class="onb-unit">일</span>
            </div>
        </div>

        <div class="kd-field">
            <span class="kd-label">성별</span>
            <div class="mp-radios">
                <span class="onb-radio-item">
                    <input class="onb-radio" type="radio" id="genderBoy" name="gender" value="M"<%= "M".equals(gender) ? " checked" : "" %>>
                    <label for="genderBoy">남자</label>
                </span>
                <span class="onb-radio-item">
                    <input class="onb-radio" type="radio" id="genderGirl" name="gender" value="F"<%= "F".equals(gender) ? " checked" : "" %>>
                    <label for="genderGirl">여자</label>
                </span>
            </div>
        </div>

        <div class="kd-field">
            <span class="kd-label">장애 정보</span>
            <div class="onb-row onb-row--gap">
                <select class="kd-input onb-select" id="disabilityType" name="disabilityType" autocomplete="off">
                    <option value="자폐 장애"<%= isAutism ? " selected" : "" %>>자폐</option>
                    <option value="지적 장애"<%= isIntellectual ? " selected" : "" %>>지적</option>
                    <option value="발달 장애"<%= isDevelopmental ? " selected" : "" %>>발달</option>
                </select>
                <select class="kd-input onb-select" id="disabilityLevel" name="disabilityLevel" autocomplete="off">
                    <% if (isDevelopmental) { %>
                    <option value="경증"<%= isMild ? " selected" : "" %>>경증</option>
                    <option value="중증"<%= (isSevere || !isMild) ? " selected" : "" %>>중증</option>
                    <% } else { %>
                    <option value="중증" selected>중증</option>
                    <% } %>
                </select>
            </div>
        </div>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" onclick="updateChildProfile()">저장</button>
</div>

<script>
    function getSelectedVal(container, elementId, fallbackVal) {
        const el = container.querySelector('#' + elementId);
        if (!el || el.value === null || el.value.trim() === '') return fallbackVal;
        return el.value.trim();
    }

    function updateDisabilityLevels() {
        const typeSelect = document.getElementById('disabilityType');
        const levelSelect = document.getElementById('disabilityLevel');

        if (!typeSelect || !levelSelect) return;

        const selectedType = typeSelect.value || '';
        const prevLevel = levelSelect.value;

        levelSelect.innerHTML = '';

        if (selectedType.includes('발달')) {
            levelSelect.add(new Option('경증', '경증'));
            levelSelect.add(new Option('중증', '중증'));
            levelSelect.value = (prevLevel === '경증') ? '경증' : '중증';
        } else {
            levelSelect.add(new Option('중증', '중증'));
            levelSelect.value = '중증';
        }
    }

    function updateChildProfile() {
        const form = document.querySelector('.mp-grid--child');

        const childId = parseInt(getSelectedVal(document, 'childId', ''), 10);
        const childName = getSelectedVal(form, 'childName', '');

        const rawYear = getSelectedVal(form, 'birthYear', '');
        const rawMonth = getSelectedVal(form, 'birthMonth', '');
        const rawDay = getSelectedVal(form, 'birthDay', '');

        if (!childId) {
            alert("아이 정보를 불러오지 못했습니다. 새로고침 후 다시 시도해 주세요.");
            return;
        }

        if (!childName || !rawYear || !rawMonth || !rawDay) {
            alert("이름과 생년월일을 모두 입력해 주세요.");
            return;
        }

        const birthDate = rawYear + '-' + String(rawMonth).padStart(2, '0') + '-' + String(rawDay).padStart(2, '0');

        const genderElem = form.querySelector('input[name="gender"]:checked');
        const gender = genderElem ? genderElem.value : '';

        const disorderType = getSelectedVal(form, 'disabilityType', '');
        const severity = getSelectedVal(form, 'disabilityLevel', '');

        if (!gender || !disorderType || !severity) {
            alert("성별과 장애 정보를 모두 선택해 주세요.");
            return;
        }

        const payload = { childId, name: childName, birthDate, gender, disorderType, severity };

        fetch('/child/updateProfile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
            .then(res => {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(data => {
                if (data.result > 0) {
                    sessionStorage.setItem('profileSavedToast', '아이 프로필을 성공적으로 저장했어요');
                    window.location.href = window.location.pathname + '?t=' + new Date().getTime();
                } else {
                    alert(data.msg || "수정에 실패했습니다.");
                }
            })
            .catch(err => {
                console.error('Error:', err);
                alert("수정 중 오류가 발생했습니다. (" + err.message + ")");
            });
    }

    document.addEventListener('DOMContentLoaded', function () {
        const savedToastMsg = sessionStorage.getItem('profileSavedToast');
        if (savedToastMsg) {
            sessionStorage.removeItem('profileSavedToast');
            if (typeof kdSaved === 'function') {
                kdSaved(savedToastMsg);
            } else {
                alert(savedToastMsg);
            }
        }

        const dbName = '<%= childName.replace("'", "\\'") %>';
        if (dbName) document.getElementById('childName').value = dbName;

        document.getElementById('birthYear').value = '<%= selYear %>';
        document.getElementById('birthMonth').value = '<%= selMonth %>';
        document.getElementById('birthDay').value = '<%= selDay %>';

        const dbGender = '<%= gender %>';
        if (dbGender === 'M') document.getElementById('genderBoy').checked = true;
        if (dbGender === 'F') document.getElementById('genderGirl').checked = true;

        const disTypeSelect = document.getElementById('disabilityType');
        if (disTypeSelect) {
            disTypeSelect.addEventListener('change', updateDisabilityLevels);
        }
    });

    sessionStorage.removeItem('kdOnb');
</script>

<%@ include file="../common/app-bottom.jsp" %>
