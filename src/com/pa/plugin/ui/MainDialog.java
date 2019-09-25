package com.pa.plugin.ui;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.JBColor;
import com.pa.plugin.DBTableUtil;
import com.pa.plugin.connections.DbCon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author puan
 * @version 1.0
 * @date 2019-08-12
 */
public class MainDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField host;
    private JTextField username;
    private JTextField schema;
    private JPasswordField password;
    private JLabel passwordLabel;
    private JLabel schemaLabel;
    private JLabel hostLabel;
    private JLabel usernameLabel;
    private JLabel outputPathLabel;
    private JTextField ouputPath;
    private JButton fileChoose;
    private JLabel dbLabel;
    private JComboBox<String> dbComboBox;
    private JTextField port;
    private JLabel portLabel;
    private JTextField database;
    private JButton testButton;
    private JLabel testLabel;
    private JButton rememberButton;
    private JComboBox<String> connections;
    private JLabel chooseLabel;

    private static String path = System.getProperty("user.dir") + "/.db2doc/connections.json";

    private Map<String, Object> dbConMap;
    private Gson gson = new Gson();

    public static void main(String[] args) {
        new MainDialog(null);
    }

    public MainDialog(AnActionEvent anActionEvent) {
        loadPath();
        loadMap();
        createDialog();
    }

    private void loadPath() {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdir();
            FileWriter writer = null;
            try {
                file.createNewFile();
                writer = new FileWriter(file);
                writer.write("{}");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadMap() {
        FileReader reader = null;
        try {
            connections.addItem("");
            reader = new FileReader(path);
            dbConMap = gson.fromJson(reader, HashMap.class);
            Set<String> keys = dbConMap.keySet();
            for (String key : keys) {
                connections.addItem(key);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createDialog() {
        centerDialog(this, "db2doc", 550, 300);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        dbComboBox.addActionListener(e -> {
            String jdbc = (String) dbComboBox.getSelectedItem();
            if ("mysql".equals(jdbc)) {
                port.setText("3306");
            } else if ("postgresql".equals(jdbc)) {
                port.setText("5432");
            }
        });

        connections.addActionListener(e -> onChoose());
        testButton.addActionListener(e -> onTest());
        rememberButton.addActionListener(e -> onSave());
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        fileChoose.addActionListener(e -> showFileChooser());

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setVisible(true);
    }

    private void onChoose() {
        Object object = dbConMap.get((String) connections.getSelectedItem());
        if (object == null) {
            return;
        }
        DbCon con = gson.fromJson(gson.toJson(object), DbCon.class);
        dbComboBox.setSelectedItem(con.getJdbcType());
        database.setText(con.getDatabase());
        host.setText(con.getHost());
        password.setText(con.getPassword());
        port.setText(con.getPort());
        username.setText(con.getUsername());
        schema.setText(con.getSchema());
    }

    private void onTest() {
        DbCon con = getConAndValid();
        String url = "jdbc:" + con.getJdbcType() + "://" + con.getHost() + ":" + con.getPort() + "/" + con.getDatabase() + "?currentSchema=" + con.getSchema();
        boolean isSuccess = DBTableUtil.testConnection(url, con.getUsername(), con.getPassword(), con.getSchema());
        if (isSuccess) {
            testLabel.setText("连接成功");
            testLabel.setForeground(JBColor.GREEN);
        } else {
            testLabel.setText("连接失败");
            testLabel.setForeground(JBColor.RED);
        }
    }

    private void onSave() {
        DbCon con = new DbCon();
        con.setJdbcType((String) dbComboBox.getSelectedItem());
        con.setDatabase(database.getText());
        con.setHost(host.getText());
        con.setPassword(password.getText());
        con.setPort(port.getText());
        con.setUsername(username.getText());
        con.setSchema(schema.getText());
        String name = con.getUsername() + "@" + con.getHost() + ":" + con.getPort() + "/" + con.getDatabase() + "_" + con.getSchema();
        dbConMap.put(name, con);
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
            writer.write(gson.toJson(dbConMap));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            ouputPath.setText(file.getAbsolutePath() + "\\数据库结构文档.doc");
        }
    }

    private void centerDialog(MainDialog dialog, String title, int width, int height) {
        dialog.setTitle(title);
        dialog.setPreferredSize(new Dimension(width, height));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2);
    }

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        DbCon con = getConAndValid();
        String outputPath = ouputPath.getText();
        assert outputPath != null && !"".equals(outputPath);
        String url = "jdbc:" + con.getJdbcType() + "://" + con.getHost() + ":" + con.getPort() + "/" + con.getDatabase() + "?currentSchema=" + con.getSchema();
        DBTableUtil.db2doc(url, con.getUsername(), con.getPassword(), con.getSchema(), outputPath);
        dispose();
    }

    private DbCon getConAndValid() {
        DbCon con = new DbCon();
        String jdbcType = (String) dbComboBox.getSelectedItem();
        String portText = port.getText();
        assert portText != null && !"".equals(portText);
        String hostText = host.getText();
        assert hostText != null && !"".equals(hostText);
        String usernameText = username.getText();
        assert usernameText != null && !"".equals(usernameText);
        String passwordText = password.getText();
        assert passwordText != null && !"".equals(passwordText);
        String schemaText = schema.getText();
        assert schemaText != null && !"".equals(schemaText);
        String databaseText = database.getText();
        assert databaseText != null && !"".equals(databaseText);
        con.setJdbcType(jdbcType);
        con.setDatabase(databaseText);
        con.setHost(hostText);
        con.setPassword(passwordText);
        con.setPort(portText);
        con.setUsername(usernameText);
        con.setSchema(schemaText);
        return con;
    }
}
