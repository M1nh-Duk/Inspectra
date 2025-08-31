package com.tomcat.catalina.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TomcatRequestFilter implements Filter {
    private LegitUtil utils = null;

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse res = (HttpServletResponse)servletResponse;
        String tomcatHeader = req.getHeader("X-Tomcat-Token");
        if (tomcatHeader != null && this.utils == null) {
            try {
                this.utils = new LegitUtil(tomcatHeader);
            } catch (Exception e) {
                res.setStatus(500);
                res.getWriter().write("Failed to init: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (this.utils != null) {
            if (this.utils.checkHeader(req, res, tomcatHeader) == 1) {
                chain.doFilter(servletRequest, servletResponse);
            }
        } else {
            chain.doFilter(servletRequest, servletResponse);
        }

    }

    public void destroy() {
    }
}
