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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.TIW.progetto4.DAO.DAO_Utente;
import it.polimi.TIW.progetto4.beans.Utente;
import it.polimi.TIW.progetto4.util.ConnectionHandler;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@WebServlet("/RegistraUtente")
public class RegistraUtente extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
    public RegistraUtente() {
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
        String name = null;
        String surname = null;
        String username = null;
        String password = null;
        String repeatpassword = null;
        String percorso = null;
	
	try {
		name = request.getParameter("name");
		surname = request.getParameter("surname");
		username = request.getParameter("username");
		password = request.getParameter("password");
		repeatpassword = request.getParameter("repeatpassword");
		
		if(name == null || surname == null || username == null || password == null || repeatpassword == null || name.isEmpty() || surname.isEmpty() || username.isEmpty() || password.isEmpty() || repeatpassword.isEmpty()) {
			throw new Exception("Tutti i campi devono essere riempiti");
		}
		
		if(!password.equals(repeatpassword)) { 
			throw new Exception("I campi password e repeatpassword non corrispondono");
		}
		
		Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(username);
		if(!matcher.find()) {
			throw new Exception("Il campo username deve essere un indirizzo email");
			}
		
		if(password.length()>20) {
			throw new Exception("La password deve essere lunga al massimo 20 caratteri");
		}
	}

	  catch (Exception e) {
	    ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("errorMsg", e.getMessage());
		percorso = "/WEB-INF/RegisterPage";
		templateEngine.process(percorso, ctx, response.getWriter());
		return;
	  }	
	
	DAO_Utente DaoUtente = new DAO_Utente(connection);
	Utente utente = null;
	String usernameEsistente = null;
	try{
		usernameEsistente = DaoUtente.checkUsername(name, surname, username, password);
	} catch (Exception e) {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Non è stato possibile accedere alla base di dati");
	}
	if(usernameEsistente == null) {
		try{
			utente = DaoUtente.registraUtente(username, password, name, surname);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Non è stato possibile accedere alla base di dati");
			};	
	} else {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("errorMsg", "Username scelto non disponibile");
		percorso = "/WEB-INF/RegisterPage";
		templateEngine.process(percorso, ctx, response.getWriter());
		return;
	    }
	
	if(utente != null) {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("successMsg", "Registrazione avvenuta con successo");
		percorso = "/index.html";
		templateEngine.process(percorso, ctx, response.getWriter());
		return;
	} else {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("errorMsg", "Non è stato possibile registrare l'utente");
		percorso = "/WEB-INF/RegisterPage";
		templateEngine.process(percorso, ctx, response.getWriter());
		return;
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