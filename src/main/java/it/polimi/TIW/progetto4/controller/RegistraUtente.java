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

import it.polimi.TIW.progetto4.DAO.DAO_Conto;
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
	
	try {
		name = request.getParameter("name");
		surname = request.getParameter("surname");
		username = request.getParameter("username");
		password = request.getParameter("password");
		repeatpassword = request.getParameter("repeatpassword");
		
		if(name == null || surname == null || username == null || password == null || repeatpassword == null || name.isEmpty() || surname.isEmpty() || username.isEmpty() || password.isEmpty() || repeatpassword.isEmpty()) {
			throw new Exception("I campi username e password devono essere riempiti");
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
			throw new Exception("La password non può essere più lunga di 20 caratteri");
		}
	}

	  catch (Exception e) {
		// for debugging only e.printStackTrace();
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Valori delle credenziali errate");
		return;
	  }	
	
	DAO_Utente DaoUtente = new DAO_Utente(connection);
	DAO_Conto DaoConto = new DAO_Conto(connection);
	Utente utente = null;
	String usernameEsistente = null;
	try{
		usernameEsistente = DaoUtente.checkUsername(name, surname, username, password);
	} catch (Exception e) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non è stato possibile accedere alla BD");
	}
	if(usernameEsistente == null) {
		try{
			utente = DaoUtente.registraUtente(username, password, name, surname);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non è stato possibile accedere alla BD");
			};	
	}
	
	String percorso;
	int result;
	
	if(utente!= null) {
		try {
			result = DaoConto.addContoDefault(utente.getUsername());
		} catch(Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non è stato possibile aggiungere un conto di default");
		    return;
		}
		if(result == 1) {
			String path = "/index.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("successMsg","Registrazione avvenuta con successo");
			templateEngine.process(path, ctx, response.getWriter());
		}
	} 
	else {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("errorMsg", "Username già in uso");
		percorso = "/WEB-INF/RegisterPage";
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