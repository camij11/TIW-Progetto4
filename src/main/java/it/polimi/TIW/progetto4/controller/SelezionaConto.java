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
import it.polimi.TIW.progetto4.DAO.DAO_Trasferimento;
import it.polimi.TIW.progetto4.beans.Conto;
import it.polimi.TIW.progetto4.beans.Trasferimento;
import it.polimi.TIW.progetto4.beans.Utente;
import it.polimi.TIW.progetto4.util.ConnectionHandler;

import java.util.ArrayList;

@WebServlet("/SelezionaConto")
public class SelezionaConto extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

    public SelezionaConto() {
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
		int IDConto = 0;
		DAO_Conto DaoConto = new DAO_Conto(connection);
		DAO_Trasferimento DaoTrasferimento = new DAO_Trasferimento(connection);
		Conto conto;
		String percorso;
		Utente utente = (Utente)request.getSession().getAttribute("user");
		if(utente!=null) {
			try{
				String IDContoStringa = request.getParameter("conto");
				if(IDContoStringa == null || IDContoStringa.isEmpty()) {
					throw new Exception("Il campo IDConto è vuoto o nullo");
				} else {
					IDConto = Integer.parseInt(IDContoStringa);
				}
			} catch(Exception e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Selezione conto fallita. "+e.getMessage());
				return;
			}
			
			try{
				if(DaoConto.checkProprietà(IDConto, utente.getUsername())!= null) {
					conto = DaoConto.getContoByID(IDConto);
				}
				else {
					throw new Exception("L'utente in sessione non è proprietario del conto selezionato");
				}
			} catch(Exception e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossibile selezionare conto desiderato. "+e.getMessage());
				return;
			}
			
			ArrayList<Trasferimento> listaTrasferimenti = new ArrayList<>();
			
			try {
				listaTrasferimenti = DaoTrasferimento.trovaTrasferimentiByIDConto(IDConto);
			} catch(Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile accedere ai dati del conto");
				return;
			}
			
			if(conto != null) {
				percorso = "/WEB-INF/DettaglioConto.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("DettaglioConto", conto);
				ctx.setVariable("Trasferimenti", listaTrasferimenti);
				templateEngine.process(percorso, ctx, response.getWriter());
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossibile selezionare il conto");
				return;
			}
		}else {
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