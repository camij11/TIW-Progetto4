package it.polimi.TIW.progetto4.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@WebServlet("/CheckConto")
public class CheckConto extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

    public CheckConto() {
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
		
		String usernameDestinatario;
		int IDContoDestinazione;
		String causale;
		int importo;
		int IDContoOrigine = 0;
		String percorso;
		
		Utente utente = (Utente)request.getSession().getAttribute("user");
		if(utente!=null) {
			try {
				IDContoOrigine = Integer.parseInt(request.getParameter("IDContoOrigine"));
				usernameDestinatario = request.getParameter("usernameDestinatario");
				IDContoDestinazione = Integer.parseInt(request.getParameter("IDContoDestinazione"));
				causale = request.getParameter("causale");
				importo = Integer.parseInt(request.getParameter("importo"));
				
				Pattern p = Pattern.compile("^[a-zA-Z0-9.!#$%&’+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)$", Pattern.CASE_INSENSITIVE);
		        Matcher matcher = p.matcher(usernameDestinatario);
				if(!matcher.find()) {
					throw new Exception("Indirizzo email non valido");
				}
				
				if (usernameDestinatario == null || usernameDestinatario.isEmpty()) {
					throw new Exception("Username nullo o vuoto");
				}
				
				if (causale == null || causale.isEmpty()) {
					throw new Exception("Causale nulla o vuota");
				}
				
				if (importo <= 0) {
					throw new Exception("Importo nullo o negativo");
				}
				
				if (IDContoOrigine == IDContoDestinazione) {
					throw new Exception("Conto di origine uguale a conto di destinazione");
				}
				
			} catch(NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Campi inseriti non validi");
				return;
			} catch (Exception e) {
				if(IDContoOrigine !=0) {
					ServletContext servletContext = getServletContext();
					final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
					ctx.setVariable("errorMsg", e.getMessage());
					ctx.setVariable("IDConto", IDContoOrigine);
					percorso = "/WEB-INF/Fallimento.html";
					templateEngine.process(percorso, ctx, response.getWriter());
					return;
				} else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Conto di origine errato");
					return;
				}
			}
			
			DAO_Conto DAOConto = new DAO_Conto(connection);
			Conto contoDestinazione = null;
			Conto contoOrigine = null;
			try {
				contoDestinazione = DAOConto.checkProprietà(IDContoDestinazione, usernameDestinatario);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile controllare la proprietà del conto");
				return;
			}
			try {
				contoOrigine = DAOConto.checkProprietà(IDContoOrigine, utente.getUsername());
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile controllare la proprietà del conto");
				return;
			}
			
			if(contoOrigine!= null && contoDestinazione !=null && contoOrigine.getSaldo()>=importo) {
				percorso = "/EseguiTransazione";
				getServletContext().getRequestDispatcher(percorso).forward(request, response);
				
			} 
			else if(contoOrigine== null || contoDestinazione ==null) {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg", "Conto di origine o conto di destinazione inesistente");
				ctx.setVariable("IDConto", IDContoOrigine);
				percorso = "/WEB-INF/Fallimento.html";
				templateEngine.process(percorso, ctx, response.getWriter());
			} else {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg", "Importo maggiore del saldo disponibile");
				ctx.setVariable("IDConto", IDContoOrigine);
				percorso = "/WEB-INF/Fallimento.html";
				templateEngine.process(percorso, ctx, response.getWriter());
			}
		} else {
			percorso = "/Logout";
			getServletContext().getRequestDispatcher(percorso).forward(request, response);
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