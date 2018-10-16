package com.polytech;

import java.io.*;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class Client extends EnvoieRecevoir {

    private static final String IP_Def = "127.0.0.1";
    private Vector<Integer> myPort;
    private int ECOUTE;

    private static final byte CODE_RRQ = 1;
    private static final byte CODE_WRQ = 2;
    private static final byte CODE_DATAPACKET = 3;
    private static final byte CODE_ACK = 4;
    private static final byte CODE_ERROR = 5;

    private final static int PACKET_SIZE = 516;
    private final static int SEND_DATA_PACKET_SIZE = 512;

    /**
     * Initialize client to send or receive Data
     */
    public String runClient(String local, String distant, String IP, boolean  receive) {
        if(IP.isEmpty()){
            IP=IP_Def;
        }
        String messageRetour="";
        myPort = getAvailablePorts(4001, 5000);
        ECOUTE = myPort.firstElement();
        System.out.println("Mon port d'écoute: " + ECOUTE);
        initSocket(ECOUTE);

        try {
            if(receive){
                messageRetour=receiveFile(local, distant, IP);
            }else{
                messageRetour=sendFile(local,distant,IP);
            }

        } catch (IOException e) {
            messageRetour="Une erreur est survenue";
            e.printStackTrace();
        }
        closeSocket(ECOUTE);
        return messageRetour;
    }

    private static Vector getAvailablePorts(int start, int end) {
        DatagramSocket d;
        Vector<Integer> v = new Vector<>();
        for (int i = start; i <= end; i++) {
            try {
                d = new DatagramSocket(i);
                v.add(i);
                d.close();
            } catch (SocketException se) {
                //System.out.println("Port Bloquer:" + i);
            }
        }
        return v;
    }

    /**
     * @param fileName     File name in the local directory
     * @param distFileName File Name in the Distant repository
     * @param addrServ     Ip addresse of the server
     * @return
     * @throws IOException
     */
    public String receiveFile(String fileName, String distFileName, String addrServ) throws IOException {
        ByteArrayOutputStream byteOutOS = new ByteArrayOutputStream();

        int block = -1;
        DatagramPacket dp =null;
        byte[] data = createRequest(CODE_RRQ, distFileName, "octet");
        this.send(InetAddress.getByName(addrServ), 69, data);
        do {

            try{
                System.out.println("TFTP Packet count: " + block);
                block++;
                byte[] bufferByteArray = new byte[PACKET_SIZE];
                dp = this.get(bufferByteArray);
                byte[] opCode = {bufferByteArray[0], bufferByteArray[1]};

                if (opCode[1] == CODE_ERROR) {
                    return receivedError(dp, bufferByteArray);
                } else if (opCode[1] == CODE_DATAPACKET) {
                    // Check for the TFTP packets block number
                    byte[] blockNumber = {bufferByteArray[2], bufferByteArray[3]};

                    DataOutputStream dos = new DataOutputStream(byteOutOS);
                    dos.write(dp.getData(), 4, dp.getLength() - 4);

                    //STEP 2.2: send ACK to TFTP server for received packet
                    this.send(dp.getAddress(), dp.getPort(), sendAcknowledgment(blockNumber));

                }
            }catch (Exception e){
                return "ERROR 0 - TIMEOUT";
            }
        } while (!isLastPacket(dp)|| dp ==null );

        try (OutputStream outputStream = new FileOutputStream(fileName)) {
            byteOutOS.writeTo(outputStream);
        }

        return "Reçu";
    }


    public String sendFile(String fileName, String distFileName, String addrServ) throws IOException {
        ByteArrayOutputStream byteOutOS = new ByteArrayOutputStream();
        int block = 0;
        DatagramPacket dp;

        FileInputStream inputStream = new FileInputStream(fileName); //creation du reader
        byte [] byteDataArray= new byte[SEND_DATA_PACKET_SIZE];
        byte[] data = createRequest(CODE_WRQ, distFileName, "octet");
        int remainingByte=inputStream.available();
        int readLength=512;
        this.send(InetAddress.getByName(addrServ), 69, data);

            while (inputStream.read(byteDataArray,0,readLength)!=-1) {
                try{
                    System.out.println("TFTP Packet count: " + block);
                    System.out.println(inputStream.available());
                    byte[] bufferByteArray = new byte[SEND_DATA_PACKET_SIZE];
                    dp = this.get(bufferByteArray);
                    byte[] opCode = {bufferByteArray[0], bufferByteArray[1]};

                    if (opCode[1] == CODE_ERROR) {
                        inputStream.close();
                        return receivedError(dp, bufferByteArray);
                    } else if (opCode[1] == CODE_ACK) {
                        block++;
                        // Check for the TFTP packets block number
                        byte[] blockNumber = {bufferByteArray[2], bufferByteArray[3]};

                        //STEP 2.2: send ACK to TFTP server for received packet

                        this.send(dp.getAddress(), dp.getPort(), sendData(blockNumber,byteDataArray, block));
                        remainingByte=inputStream.available()+1;
                        if(remainingByte>SEND_DATA_PACKET_SIZE){
                            byteDataArray= new byte[SEND_DATA_PACKET_SIZE];
                        }else{
                            byteDataArray= new byte[remainingByte];
                            readLength=remainingByte;
                        }

                    }
                }catch (Exception e){
                    inputStream.close();
                    return "ERROR 0 - TIMEOUT";
                }
            }

        inputStream.close();
        return "Envoi réalisé avec succès";
    }
    /**
     * @param opCode   TFTP Code
     * @param fileName File name located in Server
     * @param mode
     * @return
     */
    private byte[] createRequest(final byte opCode, final String fileName, final String mode) {

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
     * @param blockNumber get the block number to compose the Acknowledgment message
     * @return
     */
    private byte[] sendAcknowledgment(byte[] blockNumber) {
        byte[] ACK = {0, CODE_ACK, blockNumber[0], blockNumber[1]};
        return ACK;
    }


    public ByteArrayInputStream readFile(String fileName){
        try{
            FileInputStream inputStream = new FileInputStream(fileName); //creation du reader
            DataInputStream dis = new DataInputStream(inputStream); //stream de byte à lire
            byte[] buff = new byte[inputStream.available()]; //lecture de tous les bytes du stream

            dis.read(buff);

            ByteArrayInputStream fileData = new ByteArrayInputStream(buff);  //creation du byte array qui contiendra la donnée du fichier

            return fileData;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * @param datagramPacket datagramPacket that has the information
     * @return
     */
    private boolean isLastPacket(DatagramPacket datagramPacket) {
        if (datagramPacket.getLength() < 512)
            return true;
        else
            return false;
    }

    private String receivedError(DatagramPacket dp, byte[] data) {
        String errorCode = new String(data, 3, 1);
        String errorText = new String(data, 4, dp.getLength() - 4);
        String error="Error: " + errorCode + " " + errorText;
        System.err.println(error);
        return error;
    }

    private byte [] sendData (byte [] blockNumber, byte [] fileData, int block_id){


        byte[] DATA = {0, CODE_DATAPACKET, 0, (byte)block_id};


        byte[] c = new byte[DATA.length + fileData.length];
        System.arraycopy(DATA, 0, c, 0, DATA.length);
        System.arraycopy(fileData, 0, c, DATA.length, fileData.length);
        System.out.println(fileData.length);
        return c;
    }
}
