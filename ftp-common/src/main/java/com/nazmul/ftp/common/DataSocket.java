package com.nazmul.ftp.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

public class DataSocket extends AbstractSocket {

    public DataSocket() throws SocketException {
    }

    public DataSocket(int portNo) throws SocketException {
        super(portNo);
    }

    public Data receivePacketsWithSender() throws IOException {
        byte[] receiveBuffer = new byte[MAX_LEN];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
        receive(datagram);
        Data returnVal = new Data();
        returnVal.putVal(new String(receiveBuffer), datagram.getAddress(), datagram.getPort());
        return returnVal;
    }

    public Data receiveCredentials() throws IOException {
        return receivePacketsWithSender();
    }


}
