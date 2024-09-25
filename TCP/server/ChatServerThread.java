/*
    각 클라이언트와의 통신을 처리하는 서버 측 스레드입니다.
    클라이언트로부터 메시지를 수신하고, 이를 처리하여 서버 핸들러에 전달합니다. 명령어(#CREATE, #JOIN, #STATUS, #EXIT 등)를 처리하며, 일반 메시지는 다른 클라이언트에 전달됩니다.

    주요 기능:
    - run(): 클라이언트로부터 메시지를 수신하고, 이를 서버 핸들러(ServerHandler)에 전달하여 명령어 또는 일반 메시지를 처리합니다.
*/

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
                clientSocket.closeSockets();
                System.out.println("Chat thread shutting down.");
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}