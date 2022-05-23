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
		try (PreparedStatement statement = connessione.prepareStatement(query);){
			statement.setInt(1, IDConto);
			statement.setInt(2, IDConto);
			try(ResultSet result = statement.executeQuery();){
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
			}
		}
		return risultato;
	} 	
	
	public int eseguiTransazione(int IDContoOrigine, int IDContoDestinazione, int importo, String causale) throws SQLException, Exception {
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
			if(risultato != 0) {
				Conto ContoOrigine = DAOConto.getContoByID(IDContoOrigine);
				int importoOriginePrima = ContoOrigine.getSaldo();
				ContoOrigine.setSaldo(importoOriginePrima - importo);
				if(DAOConto.updateConto(ContoOrigine)==0) {
					connessione.rollback();
					throw new Exception("Impossibile eseguire la transazione");
				}
				
				Conto ContoDestinazione = DAOConto.getContoByID(IDContoDestinazione);
				int importoDestinazioneDopo = ContoDestinazione.getSaldo();
				ContoDestinazione.setSaldo(importoDestinazioneDopo + importo);
				if(DAOConto.updateConto(ContoDestinazione)==0) {
					connessione.rollback();
					throw new Exception("Impossibile eseguire la transazione");
				}
				
				connessione.commit();
			} else {
				throw new SQLException();
			}
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
