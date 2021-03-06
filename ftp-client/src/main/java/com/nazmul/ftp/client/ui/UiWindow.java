package com.nazmul.ftp.client.ui;

import com.nazmul.ftp.client.Constants;
import com.nazmul.ftp.client.socket.ClientHelper;
import com.nazmul.ftp.client.socket.ClientHelperImpl;
import com.nazmul.ftp.client.auth.Authentication;
import com.nazmul.ftp.client.auth.LoggedInState;
import com.nazmul.ftp.client.transfer.DownloadOperation;
import com.nazmul.ftp.client.transfer.OperationContext;
import com.nazmul.ftp.client.transfer.UploadOperation;
import com.nazmul.ftp.client.util.ClientUtils;
import com.nazmul.ftp.common.exception.InvalidArgException;
import com.nazmul.ftp.common.logger.LoggerSingleton;
import com.nazmul.ftp.common.protocol.ResponseCode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

public class UiWindow extends JFrame implements ActionListener {

  private static final LoggerSingleton LOGGER = LoggerSingleton.INSTANCE;

  private static final long serialVersionUID = 1L;

  private static final String STATUS = "Status: ";

  private static JTextField serverInput;

  private static JTextField portInput;

  private static JTextField userInput;

  private static JPasswordField passwordInput;

  private static JTextArea logArea;

  private static JButton loginButton;

  private final transient Authentication auth = new Authentication();

  private transient ClientHelper helper;

  private JTextField remoteUploadFileNameInput;

  private JFileChooser uploadChooser;

  private JTextField remoteDownloadFileNameInput;

  private JFileChooser downloadChooser;

  public UiWindow() {

    super("Datagram FTP");
    configureWindow();
    drawUI();
    setListeners();
    setClientHelper();

  }

  public static void onResponseCode(short code) {

    switch (code) {
      case ResponseCode.USER_LOGGED_IN_PROCEED:
        LOGGER.info(code + " Logged in");
        logArea.append(STATUS + code + " Logged in\n");
        loginButton.setText("Disconnect");
        userInput.setEnabled(false);
        passwordInput.setEnabled(false);
        break;
      case ResponseCode.USER_LOGGED_OUT_SERVICE_TERMINATED:
        LOGGER.info(code + " Logged out");
        logArea.append(STATUS + code + " Logged out\n");
        loginButton.setText("Connect");
        userInput.setEnabled(true);
        passwordInput.setEnabled(true);
        break;
      case ResponseCode.INVALID_USERNAME_OR_PASSWORD:
        LOGGER.info(code + " Invalid username or password");
        logArea.append(STATUS + code + " Invalid username or password\n");
        break;
      case ResponseCode.USERNAME_OK_NEED_PASSWORD:
        LOGGER.info(code + " Username ok, need password");
        logArea.append(STATUS + code + " Username ok, need password\n");
        break;
      case ResponseCode.SYNTAX_ERROR_COMMAND_UNRECOGNIZED:
        LOGGER.info(code + " Syntax error in parameters or arguments");
        logArea.append(STATUS + code + " Syntax error in parameters or arguments\n");
        break;
      case ResponseCode.CLOSING_DATA_CONNECTION:
        LOGGER.info(code + " Closing data connection. Requested file action successful");
        logArea.append(STATUS + code + " Closing data connection. Requested file action successful\n");
        break;
      case ResponseCode.COMMAND_OKAY:
        LOGGER.info(code + " The requested action has been successfully completed");
        logArea.append(STATUS + code + " The requested action has been successfully completed\n");
        break;
      case ResponseCode.REQUESTED_FILE_ACTION_NOT_TAKEN:
        LOGGER.info(code + " File transfer was unsuccessful");
        logArea.append(STATUS + code + " File transfer was unsuccessful\n");
        break;
      case ResponseCode.CANT_OPEN_DATA_CONNECTION:
        LOGGER.info(code + " Cannot open data connection");
        logArea.append(STATUS + code + " Cannot open data connection\n");
        break;
      case ResponseCode.REQUESTED_ACTION_NOT_TAKEN:
        LOGGER.info(code + " Requested action not taken. File unavailable (e.g., file not found, no access)");
        logArea.append(STATUS + code + " Requested action not taken. File unavailable (e.g., file not found, no access).\n");
        break;
      default:
        LOGGER.info("Runtime exception occurred");
    }
  }

  private void setListeners() {

    loginButton.addActionListener(this);

    uploadChooser.addActionListener(this);
    downloadChooser.addActionListener(this);

    remoteDownloadFileNameInput.addActionListener(this);
  }

  private void drawUI() {

    /** Server informations **/
    JPanel top = new JPanel();
    top.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 15));
    top.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

    JLabel serverLabel = new JLabel("Host: ");
    serverInput = new JTextField("localhost");
    serverInput.setPreferredSize(new Dimension(120, 20));
    serverInput.setEnabled(false);

    JLabel portLabel = new JLabel("Port: ");
    portInput = new JTextField("3000");
    portInput.setPreferredSize(new Dimension(50, 20));
    portInput.setEnabled(false);

    JLabel userLabel = new JLabel("Username : ");
    userInput = new JTextField("!demo@");
    userInput.setPreferredSize(new Dimension(120, 20));

    JLabel passwordLabel = new JLabel("Password : ");
    passwordInput = new JPasswordField("demo!");
    passwordInput.setPreferredSize(new Dimension(100, 20));

    loginButton = new JButton("Connect");
    loginButton.setPreferredSize(new Dimension(150, 20));

    add(top, BorderLayout.PAGE_START);

    top.add(serverLabel);
    top.add(serverInput);
    top.add(portLabel);
    top.add(portInput);
    top.add(userLabel);
    top.add(userInput);
    top.add(passwordLabel);
    top.add(passwordInput);
    top.add(loginButton);
    top.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

    /** Main content definition **/
    JPanel center = new JPanel();
    center.setLayout(new GridLayout(1, 2));

    /** Setting sending components **/
    JPanel upload = new JPanel();
    upload.setLayout(new BorderLayout());
    upload.setBorder(
            BorderFactory
                    .createTitledBorder(
                            BorderFactory
                                    .createLineBorder(Color.GRAY), "Upload a file"));
    remoteUploadFileNameInput = new JTextField(Constants.DEFAULT_REMOTE_INPUT);

    //Simulate prompt
    remoteUploadFileNameInput.addFocusListener(new UploadFocusListener());

    uploadChooser = new JFileChooser();
    uploadChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    uploadChooser.setApproveButtonText("Upload");

    JPanel sendContent = new JPanel();
    sendContent.setLayout(new BorderLayout());
    sendContent.add(uploadChooser, BorderLayout.CENTER);
    sendContent.add(remoteUploadFileNameInput, BorderLayout.PAGE_START);
    upload.add(sendContent, BorderLayout.CENTER);

    /** Setting reception components */
    JPanel download = new JPanel();
    download.setLayout(new BorderLayout());
    download.setBorder(
            BorderFactory
                    .createTitledBorder(
                            BorderFactory
                                    .createLineBorder(Color.GRAY), "Download a file"));
    remoteDownloadFileNameInput = new JTextField(Constants.DEFAULT_REMOTE_INPUT);

    //Simulate prompt
    remoteDownloadFileNameInput.addFocusListener(new DownloadFocusListener());

    downloadChooser = new JFileChooser();
    downloadChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    downloadChooser.setApproveButtonText("Download");

    JPanel receiveContent = new JPanel();
    receiveContent.setLayout(new BorderLayout());
    receiveContent.add(downloadChooser, BorderLayout.CENTER);
    receiveContent.add(remoteDownloadFileNameInput, BorderLayout.PAGE_START);
    download.add(receiveContent, BorderLayout.CENTER);

    /** Adding to the center container **/
    center.add(upload);
    center.add(download);

    /** Adding logging area **/
    logArea = new JTextArea();
    logArea.setBorder(
            BorderFactory
                    .createTitledBorder(
                            BorderFactory.createLineBorder(Color.GRAY), "Logs"));
    logArea.setEnabled(false);
    logArea.setDisabledTextColor(Color.GREEN);
    logArea.setToolTipText("System logs");

    JScrollPane scroll = new JScrollPane(
            logArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setPreferredSize(new Dimension(getWidth(), 150));

    DefaultCaret caret = (DefaultCaret) logArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    /** Adding all **/
    add(center, BorderLayout.CENTER);
    add(scroll, BorderLayout.PAGE_END);
  }

  private void configureWindow() {

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    addWindowListener(new CustomWindowAdapter());

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Double width = screen.width * 0.5;
    Double height = screen.height * 0.7;
    setSize(width.intValue(), height.intValue());
    setMinimumSize(new Dimension(850, 600));
    setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);

    setLayout(new BorderLayout());
  }

  @Override
  public void actionPerformed(ActionEvent event) {

    if (event.getSource() == loginButton) {
      setAuthenticationContext(auth);
      auth.authenticate();

    } else if (event.getSource() == uploadChooser && auth.getState() instanceof LoggedInState) {
      try {
        String command = event.getActionCommand();
        uploadFile(command);

      } catch (InvalidArgException inval) {
        LOGGER.warn(inval.getMessage());
        logArea.append(STATUS + inval.getMessage() + "\n");
      }

    } else if (event.getSource() == downloadChooser && auth.getState() instanceof LoggedInState) {
      try {
        String command = event.getActionCommand();
        downloadFile(command);

      } catch (InvalidArgException inval) {
        LOGGER.warn(inval.getMessage());
        logArea.append(STATUS + inval.getMessage() + "\n");
      }
    }
  }

  private void uploadFile(String command) throws InvalidArgException {

    File file = uploadChooser.getSelectedFile();

    ClientUtils.hasSelectedAFile(file);
    ClientUtils.doesExist(file);

    remoteUploadFileNameInput.setText(file.getName());

    ClientUtils.isValidMaxFileSize(file);

    if (command.equals(JFileChooser.APPROVE_SELECTION)) {
      LOGGER.info("Uploading " + file.getName() + " has started");
      OperationContext context = new OperationContext(new UploadOperation());
      setOperationContext(context);
      context.executeOperation();

    } else if (command.equals(JFileChooser.CANCEL_SELECTION)) {
      logArea.append("Status: Uploading cancelled\n");
    }
  }

  private void downloadFile(String command) throws InvalidArgException {

    File file = downloadChooser.getSelectedFile();

    ClientUtils.hasSelectedAFile(file);
    ClientUtils.doesExist(file);

    remoteDownloadFileNameInput.setText(file.getName());

    ClientUtils.isValidMaxFileSize(file);

    if (command.equals(JFileChooser.APPROVE_SELECTION)) {
      LOGGER.info("Downloading " + file.getName() + " has started");
      OperationContext context = new OperationContext(new DownloadOperation());
      setOperationContext(context);
      context.executeOperation();

    } else if (command.equals(JFileChooser.CANCEL_SELECTION)) {
      logArea.append("Status: Downloading cancelled\n");
    }
  }

  private void setOperationContext(OperationContext context) {

    context.setServerInput(serverInput);
    context.setPortInput(portInput);
    context.setUserInput(userInput);
    context.setPasswordInput(passwordInput);
    context.setUploadChooser(uploadChooser);
    context.setDownloadChooser(downloadChooser);
    context.setLogArea(logArea);
    context.setHelper(helper);
  }

  private void setAuthenticationContext(Authentication auth) {

    auth.setServerInput(serverInput);
    auth.setPortInput(portInput);
    auth.setUserInput(userInput);
    auth.setPasswordInput(passwordInput);
    auth.setLogArea(logArea);
    auth.setHelper(helper);
  }

  private void setClientHelper() {

    try {
      String host = ClientUtils.validHostAddress(serverInput);
      String port = ClientUtils.validServerPort(portInput);
      helper = new ClientHelperImpl(host, port);

    } catch (InvalidArgException | UnknownHostException | SocketException e) {
      logArea.append(STATUS + e.getMessage() + "\n");
    }
  }

  private class CustomWindowAdapter extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {

      if (auth.getState() instanceof LoggedInState) {
        LOGGER.info("Logout request sent");
        auth.authenticate();
      }
    }
  }

  private class UploadFocusListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {

      if (remoteUploadFileNameInput.getText().equalsIgnoreCase(Constants.DEFAULT_REMOTE_INPUT)) {
        remoteUploadFileNameInput.setText("");
      }
    }

    @Override
    public void focusLost(FocusEvent e) {

      if ("".equalsIgnoreCase(remoteUploadFileNameInput.getText())) {
        remoteUploadFileNameInput.setText(Constants.DEFAULT_REMOTE_INPUT);
      }
    }
  }

  private class DownloadFocusListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {

      if (remoteDownloadFileNameInput.getText().equalsIgnoreCase(Constants.DEFAULT_REMOTE_INPUT)) {
        remoteDownloadFileNameInput.setText("");
      }
    }

    @Override
    public void focusLost(FocusEvent e) {

      if ("".equalsIgnoreCase(remoteDownloadFileNameInput.getText())) {
        remoteDownloadFileNameInput.setText(Constants.DEFAULT_REMOTE_INPUT);
      }
    }
  }
}
