<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
        </div>
        <div class="onb-actions">
            <% if (onbPrev) { %>
            <button type="button" class="kd-btn kd-btn-outline" onclick="<%= onbPrevAction %>"><%= onbPrevLabel %></button>
            <% } %>
            <button type="button" class="kd-btn kd-btn-primary<%= onbPrev ? "" : " onb-btn-wide" %>"
                    onclick="<%= onbNext %>"><%= onbNextLabel %></button>
        </div>
    </div>
</div>
</body>
</html>
