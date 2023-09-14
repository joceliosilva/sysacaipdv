
package com.jscomp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

public class VendaDAO {
   Preferences prefs = Preferences.userNodeForPackage(ConfiguracaoBancoDados.class);
    String urlBanco = prefs.get("urlBanco", "");
    String usuario = prefs.get("usuario", "");
    String senha = prefs.get("senha", "");

    public void inserirVenda(double valorTotalDaVenda) throws SQLException {
        try (Connection conexao = DriverManager.getConnection(urlBanco, usuario, senha)) {
            String sql = "INSERT INTO venda (data, valor_total) VALUES (?, ?)";
            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dataAtual = sdf.format(new Date());
                
                stmt.setString(1, dataAtual);
                stmt.setDouble(2, valorTotalDaVenda);
                
                stmt.executeUpdate();
            }
        }
    }
}
