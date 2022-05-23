package it.polimi.TIW.progetto4.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.TIW.progetto4.DAO.DAO_Conto;
import it.polimi.TIW.progetto4.beans.Conto;
import it.polimi.TIW.progetto4.beans.Utente;
import it.polimi.TIW.progetto4.util.ConnectionHandler;

@WebServlet("/GoToSuccessPage")
public class GoToSuccessPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
    public GoToSuccessPage() {
        super();
    }
    
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession sessione = request.getSession(false);
		if(sessione != null) {
			ctx.setVariable("ContoOriginePrima", (Conto)sessione.getAttribute("ContoOriginePrima"));
			ctx.setVariable("ContoOrigineDopo", (Conto)sessione.getAttribute("ContoOrigineDopo"));
			ctx.setVariable("ContoDestinazionePrima", (Conto)sessione.getAttribute("ContoDestinazionePrima"));
			ctx.setVariable("ContoDestinazioneDopo", (Conto)sessione.getAttribute("ContoDestinazioneDopo"));
			String percorso = "/WEB-INF/Successo.html";
			templateEngine.process(percorso, ctx, response.getWriter());
		} else {
			String percorso = "/Logout";
			getServletContext().getRequestDispatcher(percorso).forward(request, response);
		}
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request,response);
	}
}
