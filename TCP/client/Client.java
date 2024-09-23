package client;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
//        String serverAddress = args[0];
//        int chatPort = Integer.parseInt(args[1]);
//        int filePort = Integer.parseInt(args[2]);

        String serverAddress = "localhost";
        int chatPort = 2020;
        int filePort = 2021;

        try {
            ClientHandler clientHandler = new ClientHandler(serverAddress, chatPort, filePort);

            // 핸들러 전달
            SendThread sendThread = new SendThread(clientHandler);
            ReceiveThread receiveThread = new ReceiveThread(clientHandler);

            // 스레드 시작
            new Thread(receiveThread).start();
            new Thread(sendThread).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}