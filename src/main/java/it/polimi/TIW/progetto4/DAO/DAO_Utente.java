package it.polimi.TIW.progetto4.DAO;
import it.polimi.TIW.progetto4.beans.Utente;
import it.polimi.TIW.progetto4.DAO.DAO_Conto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO_Utente {
	private Connection connessione;
	
	public DAO_Utente(Connection connessione) {
		this.connessione = connessione;
	}
	
	public Utente checkCredenziali(String username, String password) throws SQLException {
		String query = "SELECT username, nome, cognome FROM Utente WHERE username = ? AND password = ?";
		try (PreparedStatement statement = connessione.prepareStatement(query);) {
			statement.setString(1, username);
			statement.setString(2, password);
			try (ResultSet result = statement.executeQuery();) {
				if (result.next()) {
					Utente utente = new Utente();
					utente.setUsername(result.getString("username"));
					utente.setNome(result.getString("nome"));
					utente.setCognome(result.getString("cognome"));
					return utente;
				}
				else {
					return null;
				}
			}
		}
	}
	
	public String checkUsername(String name, String surname, String username, String password) throws SQLException {
		String query = "SELECT username FROM Utente WHERE username = ?";
		try(PreparedStatement statement = connessione.prepareStatement(query);) {
			statement.setString(1, username);
		try(ResultSet result = statement.executeQuery();) {
			if(result.next()) {
					return username;
				} else { 
					return null;
				}
				     }
			      }
		}
	
	public Utente registraUtente(String username,String password,String name,String surname) throws SQLException {
		String query = "INSERT INTO Utente VALUES(?,?,?,?)";
		DAO_Conto DaoConto = new DAO_Conto(connessione);
		connessione.setAutoCommit(false);
		try(PreparedStatement statement = connessione.prepareStatement(query);) {
			statement.setString(1, username);
			statement.setString(2, password);
			statement.setString(3, name);
			statement.setString(4, surname);
			if(statement.executeUpdate() == 1) {
			    Utente utente = new Utente();
				utente.setNome(name);
			    utente.setCognome(surname);
				utente.setPassword(password);
				utente.setUsername(username);
				try {
					DaoConto.addContoDefault(username);
					connessione.commit();
				} catch(SQLException e){
					connessione.rollback();
					throw e;
				} finally {
					connessione.setAutoCommit(true);
				}
				return utente;
			} else {
				connessione.setAutoCommit(true);
				return null;
			}
		}
	}
}
	
