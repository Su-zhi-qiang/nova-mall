package com.su.mall.security.component;

import com.su.mall.security.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器（每次请求必经）
 * <p>继承OncePerRequestFilter保证每个请求只执行一次
 * <p>处理流程：从请求头提取Token → 解析用户名 → 加载用户信息 → 校验Token → 设置SecurityContext
 * <p>此过滤器在UsernamePasswordAuthenticationFilter之前执行
 *
 * @see JwtTokenUtil
 * @see SecurityConfig#filterChain
 */
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;   // 请求头名称（如 "Authorization"）

    @Value("${jwt.tokenHead}")
    private String tokenHead;     // Token前缀（如 "Bearer "）

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        // 1. 从请求头获取Authorization值
        String authHeader = request.getHeader(this.tokenHeader);

        // 2. 判断是否携带了JWT Token
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // 3. 截取Token前缀，获取纯Token字符串
            String authToken = authHeader.substring(this.tokenHead.length());

            // 4. 从Token中解析用户名
            String username = jwtTokenUtil.getUserNameFromToken(authToken);
            LOGGER.info("checking username:{}", username);

            // 5. 用户名有效且SecurityContext中无认证信息时，执行认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. 根据用户名从数据库/缓存加载用户详情
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 7. 验证Token是否有效（签名+过期时间）
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    // 8. 创建认证对象并设置到SecurityContext
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    LOGGER.info("authenticated user:{}", username);

                    // 9. 将认证信息放入SecurityContext，后续Controller可通过SecurityContextHolder获取
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // 10. 继续执行过滤器链（进入下一个Filter或Controller）
        chain.doFilter(request, response);
    }
}
