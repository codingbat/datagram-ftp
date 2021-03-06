package com.nazmul.ftp.common;

import com.nazmul.ftp.common.io.FileEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * Proxy pattern to implement a client-server TFTP application
 *
 * To support plug-compatibility of wrapper and target, create an interface
 *
 * Intent
 * a) Provide a surrogate or placeholder for another object to control access to it.
 * b) Use an extra level of indirection to support distributed, controlled, or intelligent access.
 * c) Add a wrapper and delegation to protect the real component from undue complexity.
 */
public interface DataSocket extends Serializable {

  void sendDataPackets(InetAddress receiverHost, int receiverPort, String message) throws IOException;
  String receiveConfirmationMessage() throws IOException;
  Data receiveDataPacketsWithSender() throws IOException;

  void sendCredentials(InetAddress host, int port, String opcode, String username, String password)
          throws IOException;
  Data receiveCredentials() throws IOException;

  void sendFilePackets(FileEvent event, InetAddress host, int port) throws IOException;
  FileEvent receiveFilePacketsWithSender() throws IOException, ClassNotFoundException;

}
