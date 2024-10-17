package cn.gson.oasys.handler;

import cn.gson.oasys.entity.User;
import cn.gson.oasys.support.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

/**
 * JWT过滤器
 */
@WebFilter(filterName = "JwtFilter", urlPatterns = "/*")
public class JwtFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        // 调用方法获取 oa.gc.controller 下所有接口
        List<String> endpoints = getEndpointsInPackage("cn.gson.oasys.controller");
        endpoints.remove("deploy");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        // 获取 header 里的 token
        final String token = request.getHeader("token");

        String requestURI = request.getRequestURI();
        if (requestURI.equals("/") || endpoints.stream().noneMatch(requestURI::contains) || Arrays.asList("/logout","/web/login").contains(requestURI) || requestURI.split("/").length > 2) {
            chain.doFilter(req, res);
        } else {
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "no such data", HttpServletResponse.SC_UNAUTHORIZED, "未登录");
                return;
            }
            User userData = JwtUtil.verifyToken(token);
            if (userData == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                writeJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, "no such data", HttpServletResponse.SC_FORBIDDEN, "token解析错误");
                return;
            }
            Long id = userData.getId();
            String userName = userData.getUserName();
            String password = userData.getPassword();
            // 拦截器 拿到用户信息，放到 request 中
            request.setAttribute("id", id);
            request.setAttribute("userName", userName);
            request.setAttribute("password", password);
            chain.doFilter(req, res);
        }
    }

    private void writeJsonResponse(HttpServletResponse response, int statusCode, String message, int code, String list) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        data.put("msg", message);
        data.put("code", code);
        data.put("list", list);

        String jsonResponse = mapper.writeValueAsString(data);
        response.setStatus(statusCode);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush(); // 刷新响应
        response.getWriter().close(); // 关闭响应输出流
    }

    @Override
    public void destroy() {
    }

    public List<String> getEndpointsInPackage(String packageName) {
        List<String> endpoints = new ArrayList<>();
        try {
            // 获取包下所有类
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace(".", "/");
            java.net.URL resource = classLoader.getResource(path);
            File directory = new File(resource.getFile());
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    // 获取类中所有方法
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        endpoints.add(method.getName()); // 添加方法名作为接口
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return endpoints;
    }

}