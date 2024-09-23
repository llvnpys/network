package server;

import java.io.*;

public class ChatServerThread implements Runnable {
    private ClientSocket clientSocket;
    private ServerHandler serverHandler;

    private BufferedReader in;


    public ChatServerThread(ClientSocket clientSocket, ServerHandler serverHandler) throws IOException {
        this.clientSocket = clientSocket;
        this.serverHandler = serverHandler;
        in = clientSocket.getChatIn();
    }

    @Override
    public void run() {
        try {
            String message;

            // 클라이언트로부터 메시지를 대기
            while ((message = in.readLine()) != null) {
                String[] messageSplit = message.split(" ");
                if (message.startsWith("#CREATE")) {
                    String roomName = messageSplit[1];
                    String userName = messageSplit[2];
                    serverHandler.create(clientSocket, roomName, userName);
                } else if (message.startsWith("#JOIN")) {
                    String roomName = messageSplit[1];
                    String userName = messageSplit[2];
                    serverHandler.join(clientSocket, roomName, userName);
                } else if (message.startsWith("#STATUS")) {
                    serverHandler.status(clientSocket);
                } else if (message.startsWith("#EXIT")) {
                    serverHandler.exit(clientSocket);
                } else {
                    // 일반 메시지
                    serverHandler.sendMessage(clientSocket, message);
                }
            }
        } catch (Exception e) {

        } finally {
            try {
                // 리소스 닫기
                if (in != null) {
                    in.close();
                }
                clientSocket.closeSockets();
                System.out.println("Chat thread shutting down.");
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}