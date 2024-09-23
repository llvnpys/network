package server;

import java.io.DataInputStream;
import java.io.IOException;

public class FileServerThread implements Runnable {
    private ClientSocket clientSocket;
    private ServerHandler serverHandler;
    private DataInputStream in;

    public FileServerThread(ClientSocket clientSocket, ServerHandler serverHandler) throws IOException {
        this.clientSocket = clientSocket;
        this.serverHandler = serverHandler;
        this.in = clientSocket.getFileIn();
    }

    @Override
    public void run() {
        try {
            String message;
            while (true) {
                message = in.readUTF(); // 파일 송수신 명령어 수신
                if (message.startsWith("#PUT")) {
                    String fileName = message.split(" ")[1];
                    serverHandler.put(clientSocket, fileName); // 파일 업로드 처리
                } else if (message.startsWith("#GET")) {
                    String fileName = message.split(" ")[1];
                    serverHandler.get(clientSocket, fileName); // 파일 다운로드 처리
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("file thread shutting down.");
        }
    }
}