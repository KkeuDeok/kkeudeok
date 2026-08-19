<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.Year" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="kopo.kkeudeok.dto.ProfileDTO" %>
<%@ page import="java.time.LocalDateTime" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "child"; %>
<%@ include file="../common/app-top.jsp" %>

<%
    // 1. DB에서 꺼내온 아이 정보를 변수에 안전하게 담습니다.
    ProfileDTO child = (ProfileDTO) request.getAttribute("child");

    String childName = "";
    String gender = "";
    String disorderType = "자폐 장애";
    String severity = "";
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
        disorderType = (child.getDisorderType() != null && !child.getDisorderType().isEmpty()) ? child.getDisorderType() : "자폐 장애";
        severity = child.getSeverity() != null ? child.getSeverity() : "";
        childId = child.getChildId();

        // DB에서 캐릭터 키(bada, tori 등) 가져오기
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
                // 날짜 파싱 실패 시 기본값 유지
            }
        }
    }
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
            <!-- 브라우저 자동완성 방지를 위해 autocomplete="off" 추가 -->
            <input class="kd-input" type="text" id="childName" value="<%= childName %>" autocomplete="off">
        </div>

        <div class="kd-field">
            <label class="kd-label" for="birthYear">생년월일</label>
            <div class="onb-row">
                <select class="kd-input onb-select" id="birthYear" name="birthYear">
                    <% for (int y = Year.now().getValue(); y >= 1990; y--) { %>
                    <option value="<%= y %>"<%= y == selYear ? " selected" : "" %>><%= y %></option>
                    <% } %>
                </select>
                <span class="onb-unit">년</span>
                <select class="kd-input onb-select" id="birthMonth" name="birthMonth">
                    <% for (int m = 1; m <= 12; m++) { %>
                    <option value="<%= m %>"<%= m == selMonth ? " selected" : "" %>><%= m %></option>
                    <% } %>
                </select>
                <span class="onb-unit">월</span>
                <select class="kd-input onb-select" id="birthDay" name="birthDay">
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
                <select class="kd-input onb-select" id="disabilityType" name="disabilityType">
                    <option value="자폐 장애"<%= "자폐 장애".equals(disorderType) ? " selected" : "" %>>자폐</option>
                    <option value="지적 장애"<%= "지적 장애".equals(disorderType) ? " selected" : "" %>>지적</option>
                    <option value="발달 장애"<%= "발달 장애".equals(disorderType) ? " selected" : "" %>>발달</option>
                </select>
                <select class="kd-input onb-select" id="disabilityLevel" name="disabilityLevel"></select>
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

    // 장애 유형에 따라 장애 정도(경증/중증) 옵션을 동적으로 변경하는 함수
    function updateDisabilityLevels(targetSeverity) {
        const typeSelect = document.getElementById('disabilityType');
        const levelSelect = document.getElementById('disabilityLevel');

        if (!typeSelect || !levelSelect) return;

        const selectedType = typeSelect.value;
        const currentVal = targetSeverity || levelSelect.value;

        // 기존 옵션 초기화
        levelSelect.innerHTML = '';

        // '발달 장애' 또는 '발달'인 경우에만 경증, 중증 모두 표시
        if (selectedType === '발달 장애' || selectedType === '발달') {
            levelSelect.add(new Option('경증', '경증'));
            levelSelect.add(new Option('중증', '중증'));
        } else {
            // 자폐 장애, 지적 장애 등은 '중증'만 표시
            levelSelect.add(new Option('중증', '중증'));
        }

        // 기존에 선택되어 있던 값이나 DB 저장값이 새로 생성된 옵션에 있으면 선택 유지
        if (currentVal && Array.from(levelSelect.options).some(opt => opt.value === currentVal)) {
            levelSelect.value = currentVal;
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

        fetch('/profile/updateProfile', {
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
                    if (typeof kdSaved === 'function') kdSaved('아이 프로필을 성공적으로 저장했어요');
                    else alert('아이 프로필을 성공적으로 저장했어요');

                    // 1.2초(1200ms) 지연 후 새로고침
                    setTimeout(function() {
                        window.location.reload();
                    }, 1200);
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
        // DB 데이터 강제 복원 (자동완성 및 스크립트 초기화 방지)
        const dbName = '<%= childName.replace("'", "\\'") %>';
        if (dbName) document.getElementById('childName').value = dbName;

        document.getElementById('birthYear').value = '<%= selYear %>';
        document.getElementById('birthMonth').value = '<%= selMonth %>';
        document.getElementById('birthDay').value = '<%= selDay %>';

        const dbGender = '<%= gender %>';
        if (dbGender === 'M') document.getElementById('genderBoy').checked = true;
        if (dbGender === 'F') document.getElementById('genderGirl').checked = true;

        const dbDisType = '<%= disorderType %>';
        if (dbDisType) document.getElementById('disabilityType').value = dbDisType;

        // 장애 정도(severity) 세팅 및 옵션 생성
        const savedSeverity = '<%= severity %>';
        updateDisabilityLevels(savedSeverity);

        // 장애 유형 변경 시 이벤트 연결
        const disTypeSelect = document.getElementById('disabilityType');
        if (disTypeSelect) {
            disTypeSelect.addEventListener('change', function() {
                updateDisabilityLevels();
            });
        }
    });

    sessionStorage.removeItem('kdOnb');
</script>

<%@ include file="../common/app-bottom.jsp" %>