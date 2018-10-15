package com.polytech;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvoieRecevoir {

    private DatagramSocket mySocket;


    public void initSocket(int port) {
        try {
            mySocket = new DatagramSocket(port);
        } catch (SocketException ex) {
            System.out.println("Port already Taken" + port);
        }
    }

    public void send(InetAddress inetAddress, int port, byte[] data) {
        try {
            DatagramPacket dp = new DatagramPacket(data, data.length, inetAddress, port);
            mySocket.send(dp);
        } catch (IOException ex) {
            Logger.getLogger(EnvoieRecevoir.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DatagramPacket get(byte[] buffer) throws Exception {

        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

        mySocket.setSoTimeout(5000);
        mySocket.receive(dp);
        String tmp = new String(dp.getData(), "ascii");
        byte[] data = tmp.getBytes("ascii");
        DatagramPacket returnDp = new DatagramPacket(data, data.length, dp.getAddress(), dp.getPort());
        return dp;


    }


    public void closeSocket(int socket) {
        mySocket.close();
    }

}
