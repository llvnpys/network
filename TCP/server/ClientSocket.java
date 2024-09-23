package server;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket chatSocket;
    private Socket fileSocket;

    private BufferedReader chatIn;
    private BufferedWriter chatOut;
    private DataInputStream fileIn;  // 데이터 스트림으로 변경
    private DataOutputStream fileOut;  // 데이터 스트림으로 변경

    private String roomName;
    private String clientName;

    public ClientSocket(Socket chatSocket, Socket fileSocket) throws IOException {
        this.chatSocket = chatSocket;
        this.fileSocket = fileSocket;

        chatIn = new BufferedReader(new InputStreamReader(this.chatSocket.getInputStream()));
        chatOut = new BufferedWriter(new OutputStreamWriter(this.chatSocket.getOutputStream()));
        fileIn = new DataInputStream(this.fileSocket.getInputStream());  // 변경
        fileOut = new DataOutputStream(this.fileSocket.getOutputStream());  // 변경
    }

    public BufferedReader getChatIn() {
        return chatIn;
    }

    public DataInputStream getFileIn() {  // 리턴 타입 변경
        return fileIn;
    }

    public BufferedWriter getChatOut() {
        return chatOut;
    }

    public DataOutputStream getFileOut() {  // 리턴 타입 변경
        return fileOut;
    }

    public Socket getChatSocket() {
        return chatSocket;
    }

    public Socket getFileSocket() {
        return fileSocket;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // 모든 자원을 닫는 메서드
    public void closeSockets() {
        try {
            if (chatIn != null) {
                chatIn.close();
            }
            if (chatOut != null) {
                chatOut.close();
            }
            if (fileIn != null) {
                fileIn.close();
            }
            if (fileOut != null) {
                fileOut.close();
            }
            if (chatSocket != null && !chatSocket.isClosed()) {
                chatSocket.close();
            }
            if (fileSocket != null && !fileSocket.isClosed()) {
                fileSocket.close();
            }
            System.out.println("All resources have been closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}