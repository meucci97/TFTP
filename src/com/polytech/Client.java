package com.polytech;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends EnvoieRecevoir{

    public static final String IP="127.0.0.1";
    private Vector<Integer> myPort;
    private int ECOUTE;

    private static final byte CODE_RRQ = 1;
    private static final byte CODE_DATAPACKET = 3;
    private static final byte CODE_ACK = 4;
    private static final byte CODE_ERROR = 5;

    private final static int PACKET_SIZE = 516;

    /**
     * Initialize client to send or receive Data
     */
    public void runClient(){

        Scanner sc = new Scanner(System.in);
        DatagramPacket dp;
        String message;
        myPort = getAvailablePorts(4001, 5000);
        ECOUTE = myPort.firstElement();
        System.out.println("Mon port d'écoute: "+ECOUTE);
        initSocket(ECOUTE);

        System.out.print("Type d'opération (envoyer=1, recevoir=0) : ");
        message = sc.nextLine();
        System.out.print("Nom fichier distant: ");
        String distFileName = sc.nextLine();
        System.out.print("Nom du fichier local: ");
        String fileName = sc.nextLine();

        try {
            receiveFile(fileName,distFileName,IP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeSocket(ECOUTE);

/*
            boolean follow = true;
        try {
            System.out.println("Request sent");

            message = new String(dp.getData(), StandardCharsets.UTF_8);
            System.out.println("Réponse du serveur au Request: "+message);
            while (follow) {
                System.out.print("Message à envoyer: ");
                message = sc.nextLine();

                send(dp.getAddress(), dp.getPort(), message);
                dp =this.get();
                if (message.equalsIgnoreCase("end")) {
                    follow = false;
                }
                message=new String(dp.getData(), StandardCharsets.UTF_8);
                System.out.println("Réponse du serveur: " + message);
                message = message.trim();

            }
            closeSocket(ECOUTE);
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    public static Vector getAvailablePorts(int start, int end) {
        DatagramSocket d;
        Vector<Integer> v = new Vector<>();
        for (int i = start; i <= end; i++) {
            try {
                d = new DatagramSocket(i);
                v.add(i);
                d.close();
            } catch (SocketException se) {
                //  System.out.println("Port Bloquer:" + i);
            }
        }
        return v;
    }

    /**
     *
     * @param fileName File name in the local directory
     * @param distFileName File Name in the Distant repository
     * @param addrServ Ip addresse of the server
     * @return
     * @throws IOException
     */
    public int receiveFile(String fileName, String distFileName, String  addrServ) throws IOException {
        ByteArrayOutputStream byteOutOS = new ByteArrayOutputStream();
        int block = 1;
        DatagramPacket dp;
        byte[] data=createRequest(CODE_RRQ, distFileName, "octet");
        this.send(InetAddress.getByName(addrServ), 69, data);
        do {
            System.out.println("TFTP Packet count: " + block);
            block++;
            byte[] bufferByteArray = new byte[PACKET_SIZE];
            dp= this.get(bufferByteArray);
            byte[] opCode = { bufferByteArray[0], bufferByteArray[1] };

            if (opCode[1] == CODE_ERROR) {
                receivedError(dp,bufferByteArray);
            } else if (opCode[1] == CODE_DATAPACKET) {
                // Check for the TFTP packets block number
                byte[] blockNumber = { bufferByteArray[2], bufferByteArray[3] };

                DataOutputStream dos = new DataOutputStream(byteOutOS);
                dos.write(dp.getData(), 4, dp.getLength() - 4);

                //STEP 2.2: send ACK to TFTP server for received packet
                this.send(dp.getAddress(), dp.getPort(),sendAcknowledgment(blockNumber));

                System.out.println(new String(dp.getData(), StandardCharsets.UTF_8));
            }

        }while(!isLastPacket(dp));

        try(OutputStream outputStream = new FileOutputStream(fileName)) {
            byteOutOS.writeTo(outputStream);
        }

        return 0;
    }

    /**
     *
     * @param opCode TFTP Code
     * @param fileName File name located in Server
     * @param mode
     * @return
     */
    private byte[] createRequest(final byte opCode, final String fileName,final String mode) {

        byte zeroByte = 0;
        int rrqByteLength = 2 + fileName.length() + 1 + mode.length() + 1;
        byte[] rrqByteArray = new byte[rrqByteLength];

        int position = 0;
        rrqByteArray[position] = zeroByte;
        position++;
        rrqByteArray[position] = opCode;
        position++;
        for (int i = 0; i < fileName.length(); i++) {
            rrqByteArray[position] = (byte) fileName.charAt(i);
            position++;
        }
        rrqByteArray[position] = zeroByte;
        position++;
        for (int i = 0; i < mode.length(); i++) {
            rrqByteArray[position] = (byte) mode.charAt(i);
            position++;
        }
        rrqByteArray[position] = zeroByte;
        return rrqByteArray;
    }

    /**
     *
     * @param blockNumber get the block number to compose the Acknowledgment message
     * @return
     */
    private byte[] sendAcknowledgment(byte[] blockNumber) {
        byte[] ACK = { 0, CODE_ACK, blockNumber[0], blockNumber[1] };
        return ACK;
    }

    /**
     *
     * @param datagramPacket datagramPacket that has the information
     * @return
     */
    private boolean isLastPacket(DatagramPacket datagramPacket) {
        if (datagramPacket.getLength() < 512)
            return true;
        else
            return false;
    }

    private void receivedError(DatagramPacket dp,byte[] data ) {
        String errorCode = new String(data, 3, 1);
        String errorText = new String(data, 4, dp.getLength() - 4);
        System.err.println("Error: " + errorCode + " " + errorText);
    }

}
