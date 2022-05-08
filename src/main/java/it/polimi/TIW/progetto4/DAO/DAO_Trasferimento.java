package it.polimi.TIW.progetto4.DAO;
import it.polimi.TIW.progetto4.beans.Trasferimento;
import it.polimi.TIW.progetto4.beans.Conto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;


public class DAO_Trasferimento {
	private Connection connessione;
	
	public DAO_Trasferimento(Connection connessione) {
		this.connessione = connessione;
	}
	
	public ArrayList<Trasferimento> trovaTrasferimentiByIDConto(int IDConto) throws SQLException{
		ArrayList<Trasferimento> risultato = new ArrayList<Trasferimento>();
		
		String query = "SELECT * FROM Trasferimento WHERE IDContoOrigine = ? or IDContoDestinazione = ? ORDER BY Data DESC";
		ResultSet result = null;
		PreparedStatement statement = null;
		try {
			statement = connessione.prepareStatement(query);
			statement.setInt(1, IDConto);
			statement.setInt(2, IDConto);
			result = statement.executeQuery();
			
			while (result.next()) {
				Trasferimento t = new Trasferimento();
				t.setIDTrasferimento(result.getInt("IDTrasferimento"));
				t.setData(result.getDate("Data"));
				t.setImporto(result.getInt("Importo"));
				t.setCausale(result.getString("Causale"));
				t.setIDContoOrigine(result.getInt("IDContoOrigine"));
				t.setIDContoDestinazione(result.getInt("IDContoDestinazione"));
				
				risultato.add(t);
			}
		} catch (SQLException e) {
			throw e;

		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (SQLException e1) {
				throw e1;
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2) {
				throw e2;
			}
		}

		return risultato;
	}
	
	public int eseguiTransazione(int IDContoOrigine, int IDContoDestinazione, int importo, String causale) throws SQLException {
		int risultato = 0;
		String query = "INSERT INTO Trasferimento(Data, Importo, Causale, IDContoOrigine, IDContoDestinazione) VALUES(?, ?, ?, ?, ?)";
		
		DAO_Conto DAOConto = new DAO_Conto(connessione);
		connessione.setAutoCommit(false);
		PreparedStatement statement = null;
		try {
			statement = connessione.prepareStatement(query);
			Date data = Date.valueOf(LocalDate.now());
			statement.setDate(1, data);
			statement.setInt(2, importo);
			statement.setString(3, causale);
			statement.setInt(4, IDContoOrigine);
			statement.setInt(5, IDContoDestinazione);
			
			risultato = statement.executeUpdate();
			
			Conto ContoOrigine = DAOConto.getContoByID(IDContoOrigine);
			int importoOriginePrima = ContoOrigine.getSaldo();
			ContoOrigine.setSaldo(importoOriginePrima - importo);
			DAOConto.updateConto(ContoOrigine);
			
			Conto ContoDestinazione = DAOConto.getContoByID(IDContoDestinazione);
			int importoDestinazioneDopo = ContoDestinazione.getSaldo();
			ContoDestinazione.setSaldo(importoDestinazioneDopo + importo);
			DAOConto.updateConto(ContoDestinazione);
			
			connessione.commit();
			
		}catch (SQLException e1) {
			connessione.rollback();
			throw e1;
		} finally {
			try {
				statement.close();
			}
			catch (SQLException e2){
				throw e2;
			}
			connessione.setAutoCommit(true);
		}
		return risultato;
	}

}
