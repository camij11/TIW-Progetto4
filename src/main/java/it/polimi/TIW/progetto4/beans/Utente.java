package it.polimi.TIW.progetto4.beans;

public class Utente {
	private String username;
	private String password;
	private String nome;
	private String cognome;
	
	public void setUsername(String username) { 
		this.username = username; 
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
}
