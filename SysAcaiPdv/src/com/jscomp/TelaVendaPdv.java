/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.jscomp;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Esc_Big03
 */
public class TelaVendaPdv extends javax.swing.JFrame {
    Preferences prefs = Preferences.userNodeForPackage(ConfiguracaoBancoDados.class);
    String urlBanco = prefs.get("urlBanco", "");
    String usuario = prefs.get("usuario", "");
    String senha = prefs.get("senha", "");
    private DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private DecimalFormat decimalFormatpeso = new DecimalFormat("#0.000");
    private List<ItemCarrinho> carrinho = new ArrayList<>();
    private double valorTotal = 0.0;
    String nomeUsuario = UsuarioLogado.getNomeUsuario();
    String nomeLoja = carregarNomeLoja();
    
    public TelaVendaPdv() throws SQLException {
        initComponents();
        carregarListaProdutos();
        carregarListaTigela();
        Color corDeFundo = new Color(189, 198, 199); 
        getContentPane().setBackground(corDeFundo);
        TelaVendaPdv telaVendaPdv = this;
        telaVendaPdv.setLocationRelativeTo(null);
        telaVendaPdv.setExtendedState(JFrame.MAXIMIZED_BOTH);
        telaVendaPdv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
         cmbProdutos.addItemListener(new ItemListener() {
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            carregarDadosProduto();
        }
    }
});
          valorPagoTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    atualizarTroco();
                }
            }
    
    
    });
          cmbTigelas.addItemListener(new ItemListener() {
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
           preencherCamposEdicaoTigela();
        }
    }
});
        Connection conexao = DriverManager.getConnection(urlBanco, usuario, senha);
        String sql = "SELECT logo FROM loja WHERE id = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, 1); // Substitua idDaLoja pelo ID correto da loja
        ResultSet rs = stmt.executeQuery();

        
       
        txtnomeloja.setText(nomeLoja);
   
if (rs.next()) {
    byte[] imagemBytes = rs.getBytes("logo");
    ImageIcon imagemLogo = new ImageIcon(imagemBytes);
     Image imagem = imagemLogo.getImage();
     Image novaImagem = imagem.getScaledInstance(labelImage.getWidth(), labelImage.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon novaImagemIcon = new ImageIcon(novaImagem);
        labelImage.setIcon(novaImagemIcon);
}

rs.close();
stmt.close();
conexao.close();


      if (nomeUsuario != null) {
    // Defina o texto do JLabel para mostrar o nome de usuário
    userLog.setText( nomeUsuario);
} else {
    userLog.setText("Usuário não logado");
}
       
      
    }
  
    public String carregarNomeLoja() {
    String nomeLoja = "";

    try {
        Connection conexao = DriverManager.getConnection(urlBanco, usuario, senha);
        String sql = "SELECT nome FROM loja WHERE id = 1"; // Supondo que a loja tenha um ID de 1
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            nomeLoja = rs.getString("nome");
        }

        rs.close();
        stmt.close();
        conexao.close();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro ao carregar o nome da loja: " + e.getMessage());
    }

    return nomeLoja;
}

    private void carregarListaProdutos() {
    try {
        // Estabeleça a conexão com o banco de dados (substitua com suas próprias configurações)
      Connection conn = DriverManager.getConnection(urlBanco, usuario, senha);

        // Crie uma consulta SQL para obter os nomes dos produtos da tabela "produto"
        String sql = "SELECT nome FROM produto";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        ResultSet rs = pstmt.executeQuery();

        // Limpe o ComboBox antes de adicionar os novos itens
        cmbProdutos.removeAllItems();
        cmbProdutos.addItem("Selecione...");
        // Preencha o ComboBox com os nomes dos produtos
        while (rs.next()) {
            String nomeProduto = rs.getString("nome");
            
            cmbProdutos.addItem(nomeProduto);
        }

        // Feche a conexão
        conn.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Erro ao carregar lista de produtos: " + ex.getMessage());
    }
}
   private void carregarDadosProduto() {
    try {
        // Obtém o nome do produto selecionado no ComboBox
        String nomeProdutoSelecionado = (String) cmbProdutos.getSelectedItem();

        // Verifica se um produto foi selecionado
        if (nomeProdutoSelecionado == null || nomeProdutoSelecionado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para carregar os dados.");
            return;
        }

        // Estabeleça a conexão com o banco de dados (substitua com suas próprias configurações)
       Connection conn = DriverManager.getConnection(urlBanco, usuario, senha);

        // Crie uma consulta SQL para obter os dados do produto com base no nome
        String sql = "SELECT nome, preco FROM produto WHERE nome = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, nomeProdutoSelecionado);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
             txtPreco.setText(String.valueOf(rs.getDouble("preco")));
            //txtEstoque.setText(String.valueOf(rs.getInt("estoque")));
        }

        // Feche a conexão
        conn.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Erro ao carregar dados do produto: " + ex.getMessage());
    }
    
    
}
   
   //PREENCHER COMBOX DA TIGELA
      private void carregarListaTigela() {
    try {
        // Estabeleça a conexão com o banco de dados (substitua com suas próprias configurações)
      Connection conn = DriverManager.getConnection(urlBanco, usuario, senha);

        // Crie uma consulta SQL para obter os nomes dos produtos da tabela "produto"
        String sql = "SELECT nome FROM tigela";
        PreparedStatement pstmt = conn.prepareStatement(sql);

        ResultSet rs = pstmt.executeQuery();

        // Limpe o ComboBox antes de adicionar os novos itens
        cmbTigelas.removeAllItems();
        cmbTigelas.addItem("Selecione...");
        // Preencha o ComboBox com os nomes dos produtos
        while (rs.next()) {
            String nomeProduto = rs.getString("nome");
            cmbTigelas.addItem(nomeProduto);
        }

        // Feche a conexão
        conn.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Erro ao carregar lista de produtos: " + ex.getMessage());
    }
}
     
  private void preencherCamposEdicaoTigela() {
    // Obtenha o nome da tigela selecionada no JComboBox
    String nomeTigelaSelecionada = (String) cmbTigelas.getSelectedItem();

    if (nomeTigelaSelecionada != null && !nomeTigelaSelecionada.isEmpty()) {
        try {
            // Estabeleça a conexão com o banco de dados
            Connection conn = DriverManager.getConnection(urlBanco, usuario, senha);

            // Crie uma consulta SQL para selecionar os dados da tigela
            String sql = "SELECT nome, tara, unica FROM tigela WHERE nome = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomeTigelaSelecionada);

            // Execute a consulta
            ResultSet rs = pstmt.executeQuery();

            // Preencha os campos de edição com os dados da tigela
            if (rs.next()) {
                String nomeTigela = rs.getString("nome");
                double taraTigela = rs.getDouble("tara");
                boolean tigelaUnica = rs.getBoolean("unica");
                decimalFormatpeso.format(taraTigela);
                // Preencha os campos de edição com os dados da tigela
               // txtNome.setText(nomeTigela);
                txtTara.setText(String.valueOf(taraTigela));
                //chkUnica.setSelected(tigelaUnica);
            }

            // Feche a conexão
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao preencher os campos de edição: " + ex.getMessage());
        }
    }
}
  private void adicionarItemAoCarrinho() {
    // Obtenha os valores selecionados nos campos
    String nomeProduto = (String) cmbProdutos.getSelectedItem();
    String nomeTigela = (String) cmbTigelas.getSelectedItem();
    double precoProduto = Double.parseDouble(txtPreco.getText());
    double tara = Double.parseDouble(txtTara.getText());
   // decimalFormat.format(tara);
    double peso = Double.parseDouble(txtPeso.getText());

    // Crie uma instância de ItemCarrinho e adicione ao carrinho
    ItemCarrinho item = new ItemCarrinho(nomeProduto,peso, tara, precoProduto);
    carrinho.add(item);

    // Atualize o texto na área de texto do carrinho
    //atualizarTextoCarrinho();
atualizarCarrinho();
    // Atualize o valor total
    //calcularValorTotal();
}

   private void atualizarCarrinho() {
        carrinhoTextArea.setText("");
        carrinhoTextArea.append("  ITEM" + "      " + " PESO"  + "     " + "PRECO(kg)" + "    " + "VALOR TOTAL"+"\n");
        
        for (ItemCarrinho produto : carrinho) {
              double subtotal = produto.calcularSubtotal();
              double pesocp = produto.getQuantidade()- produto.getTara();
             valorTotal += subtotal;
            carrinhoTextArea.append(produto.getNomeProduto() + "  " + decimalFormatpeso.format(pesocp) + "      " + " R$ " + decimalFormat.format(produto.getPrecoUnitario()) + "                   " +  " R$ " +decimalFormat.format(valorTotal) + "\n");
     
                valorTotalLabel.setText("R$" + decimalFormat.format(valorTotal));
                atualizarTroco();
        }
      
    }
   
   
    
    private void atualizarTroco() {
        try {
            double valorPago = Double.parseDouble(valorPagoTextField.getText());
            double troco = valorPago - valorTotal;
            trocoLabel.setText("R$" + String.format("%.2f", troco));
        } catch (NumberFormatException e) {
            trocoLabel.setText(" ");
        }
        
    }
    
   
  
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        carrinhoTextArea = new javax.swing.JTextArea();
        txtTara = new javax.swing.JTextField();
        cmbProdutos = new javax.swing.JComboBox<>();
        cmbTigelas = new javax.swing.JComboBox<>();
        txtPreco = new javax.swing.JTextField();
        valorTotalLabel = new javax.swing.JTextField();
        btnAddItem = new javax.swing.JButton();
        btnCancelarOp = new javax.swing.JButton();
        txtPeso = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        labelImage = new javax.swing.JLabel();
        txtnomeloja = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        userLog = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        bntFinalizar = new javax.swing.JButton();
        valorPagoTextField = new javax.swing.JTextField();
        trocoLabel = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JS PDV");
        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(java.awt.Color.green);
        setResizable(false);
        setType(java.awt.Window.Type.POPUP);

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        carrinhoTextArea.setEditable(false);
        carrinhoTextArea.setColumns(20);
        carrinhoTextArea.setRows(5);
        carrinhoTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder("CUPOM"));
        carrinhoTextArea.setFocusable(false);
        jScrollPane1.setViewportView(carrinhoTextArea);

        txtTara.setEditable(false);
        txtTara.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtTara.setForeground(new java.awt.Color(0, 102, 255));
        txtTara.setBorder(javax.swing.BorderFactory.createTitledBorder("TARA"));
        txtTara.setFocusable(false);

        cmbProdutos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbProdutos.setBorder(javax.swing.BorderFactory.createTitledBorder("SELECIONE O PRODUTO"));

        cmbTigelas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTigelas.setBorder(javax.swing.BorderFactory.createTitledBorder("SELECIONE TIGELA"));

        txtPreco.setEditable(false);
        txtPreco.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtPreco.setForeground(new java.awt.Color(0, 102, 255));
        txtPreco.setBorder(javax.swing.BorderFactory.createTitledBorder("PREÇO"));
        txtPreco.setFocusable(false);

        valorTotalLabel.setEditable(false);
        valorTotalLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        valorTotalLabel.setForeground(new java.awt.Color(0, 102, 255));
        valorTotalLabel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("VALOR TOTAL")));
        valorTotalLabel.setFocusable(false);
        valorTotalLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valorTotalLabelActionPerformed(evt);
            }
        });

        btnAddItem.setText("ADICIONAR");
        btnAddItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddItemActionPerformed(evt);
            }
        });

        btnCancelarOp.setText("CANCELAR");

        txtPeso.setBorder(javax.swing.BorderFactory.createTitledBorder("PESO"));
        txtPeso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtTara)
                                    .addComponent(cmbTigelas, 0, 432, Short.MAX_VALUE)
                                    .addComponent(cmbProdutos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtPreco, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                                    .addComponent(txtPeso)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(120, 120, 120)
                                .addComponent(btnAddItem)
                                .addGap(33, 33, 33)
                                .addComponent(btnCancelarOp)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(valorTotalLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cmbProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbTigelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTara, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPeso, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddItem)
                            .addComponent(btnCancelarOp))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(valorTotalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(263, 263, 263))
        );

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelImage.setBackground(new java.awt.Color(255, 153, 153));
        labelImage.setFont(new java.awt.Font("Sitka Text", 0, 24)); // NOI18N

        txtnomeloja.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txtnomeloja.setForeground(new java.awt.Color(255, 255, 255));
        txtnomeloja.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtnomeloja.setText("jLabel1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(99, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelImage, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtnomeloja, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(130, 130, 130))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtnomeloja, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(130, 130, 130)
                .addComponent(labelImage, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(281, Short.MAX_VALUE))
        );

        jButton1.setText("SAIR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        userLog.setBackground(new java.awt.Color(255, 255, 255));
        userLog.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        userLog.setText("jLabel1");

        jPanel3.setBackground(new java.awt.Color(153, 51, 255));
        jPanel3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("SYS AÇAÍ");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(680, 680, 680)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(563, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        bntFinalizar.setBackground(new java.awt.Color(0, 102, 0));
        bntFinalizar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        bntFinalizar.setForeground(new java.awt.Color(255, 255, 255));
        bntFinalizar.setText("FINALIZAR");
        bntFinalizar.setBorder(null);
        bntFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntFinalizarActionPerformed(evt);
            }
        });

        valorPagoTextField.setBorder(javax.swing.BorderFactory.createTitledBorder("VALOR PAGO"));
        valorPagoTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valorPagoTextFieldActionPerformed(evt);
            }
        });

        trocoLabel.setEditable(false);
        trocoLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        trocoLabel.setForeground(new java.awt.Color(0, 102, 0));
        trocoLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("TROCO"));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addComponent(valorPagoTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(trocoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(273, 273, 273)
                .addComponent(bntFinalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(valorPagoTextField)
                        .addGap(3, 3, 3))
                    .addComponent(trocoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(bntFinalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(userLog, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLog)
                    .addComponent(jButton1))
                .addGap(1, 1, 1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bntFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntFinalizarActionPerformed
        
    }//GEN-LAST:event_bntFinalizarActionPerformed

    private void valorPagoTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valorPagoTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_valorPagoTextFieldActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
            TelaLogin telaLogin = new TelaLogin();
            telaLogin.setLocationRelativeTo(null); 
            telaLogin.setVisible(true);
            dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void valorTotalLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valorTotalLabelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_valorTotalLabelActionPerformed

    private void txtPesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesoActionPerformed

    private void btnAddItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddItemActionPerformed
       adicionarItemAoCarrinho();
    }//GEN-LAST:event_btnAddItemActionPerformed

    
    public static void main(String args[]) {
       
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new TelaVendaPdv().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(TelaVendaPdv.class.getName()).log(Level.SEVERE, null, ex);
                }
              
            }
        });
    }
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntFinalizar;
    private javax.swing.JButton btnAddItem;
    private javax.swing.JButton btnCancelarOp;
    private javax.swing.JTextArea carrinhoTextArea;
    private javax.swing.JComboBox<String> cmbProdutos;
    private javax.swing.JComboBox<String> cmbTigelas;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelImage;
    private javax.swing.JTextField trocoLabel;
    private javax.swing.JTextField txtPeso;
    private javax.swing.JTextField txtPreco;
    private javax.swing.JTextField txtTara;
    private javax.swing.JLabel txtnomeloja;
    public javax.swing.JLabel userLog;
    private javax.swing.JTextField valorPagoTextField;
    private javax.swing.JTextField valorTotalLabel;
    // End of variables declaration//GEN-END:variables

   
}
