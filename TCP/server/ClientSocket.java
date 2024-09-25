/*
    클라이언트와 서버 간의 채팅 및 파일 전송을 처리하는 소켓 관리 클래스입니다.
    채팅과 파일 전송을 위한 소켓 및 스트림을 초기화하고, 클라이언트의 채팅방 이름과 클라이언트 이름을 관리합니다.
    자원을 효율적으로 관리하기 위해 모든 소켓과 스트림을 닫는 기능도 포함되어 있습니다.

    주요 기능:
    - getChatIn(), getChatOut(): 채팅용 입력 및 출력 스트림을 반환합니다.
    - getFileIn(), getFileOut(): 파일 전송용 입력 및 출력 스트림을 반환합니다.
    - closeSockets(): 모든 소켓과 스트림을 닫아 자원을 반환합니다.
*/

package server;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket chatSocket;
    private Socket fileSocket;

    private BufferedReader chatIn;
    private BufferedWriter chatOut;
    private DataInputStream fileIn;
    private DataOutputStream fileOut;

    private String roomName;
    private String clientName;

    public ClientSocket(Socket chatSocket, Socket fileSocket) throws IOException {
        this.chatSocket = chatSocket;
        this.fileSocket = fileSocket;

        chatIn = new BufferedReader(new InputStreamReader(this.chatSocket.getInputStream()));
        chatOut = new BufferedWriter(new OutputStreamWriter(this.chatSocket.getOutputStream()));
        fileIn = new DataInputStream(this.fileSocket.getInputStream());
        fileOut = new DataOutputStream(this.fileSocket.getOutputStream());
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

    // 모든 자원을 반환하는 메서드
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