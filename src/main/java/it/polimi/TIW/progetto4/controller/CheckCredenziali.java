package it.polimi.TIW.progetto4.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.TIW.progetto4.beans.*;
import it.polimi.TIW.progetto4.DAO.*;
import it.polimi.TIW.progetto4.util.*;
import it.polimi.TIW.progetto4.util.ConnectionHandler;

@WebServlet("/CheckCredenziali")
public class CheckCredenziali extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

    public CheckCredenziali() {
        super();
    }
    
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = null;
		String password = null;
		String percorso = null;

		try {
			username = request.getParameter("username");
			password = request.getParameter("password");
			
			if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
				throw new Exception("I campi username e password devono essere riempiti");
			}
			
			if(password.length()>20) {
				throw new Exception("La password inserita risulta troppo lunga");
			}

		} catch (Exception e) {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg1",e.getMessage());
			percorso = "/index.html";
			templateEngine.process(percorso, ctx, response.getWriter());
			return;
		}
		
		DAO_Utente DaoUtente = new DAO_Utente(connection);
		Utente utente = null;
		try {
			utente = DaoUtente.checkCredenziali(username, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Non è stato possible verificare le credenziali");
			return;
		}
		if(utente!= null) {
			request.getSession().setAttribute("user", utente);
			percorso = getServletContext().getContextPath() + "/GoToHomePage";
			response.sendRedirect(percorso);
		}
		else {
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("errorMsg1", "Username o password errati");
			percorso = "/index.html";
			templateEngine.process(percorso, ctx, response.getWriter());
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}