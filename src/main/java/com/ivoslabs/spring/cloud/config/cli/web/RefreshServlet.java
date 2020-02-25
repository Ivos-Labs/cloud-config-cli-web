package com.ivoslabs.spring.cloud.config.cli.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet to reload and re-inject the properties from a spring-cloud-config-server service<br>
 * <br>
 * Final endpoint: <code>[ip]:[puerto]/[contexto]/actuator/refresh</code>
 * 
 * @since 1.0.0
 * @author www.ivoslabs.com
 *
 */
@WebServlet(name = "refresh", urlPatterns = { "/actuator/refresh" })
public class RefreshServlet extends HttpServlet {

    /** The constant serial Version UID */
    private static final long serialVersionUID = 1L;

    /** The logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshServlet.class);

    /**
     * Creates a new RefreshServlet
     */
    public RefreshServlet() {
	super();
    }

    /*
     * 
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	response.setContentType("application/json");
	response.setCharacterEncoding("UTF-8");

	try (PrintWriter out = response.getWriter()) {
	    response.addHeader(getServletName(), getServletInfo());
	    response.addHeader("Access-Control-Allow-Origin", "*");
	    ServletContext context = super.getServletContext();
	    WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
	    out.print(ctx.getBean(RefreshComp.class).refresh());
	} catch (Exception e) {
	    LOGGER.error(e.getMessage(), e);
	}

    }

    /*
     * 
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	try {
	    doGet(request, response);
	} catch (Exception e) {
	    LOGGER.error(e.getMessage(), e);
	}

    }

}
