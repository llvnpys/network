/*
    서버에서 파일 송수신을 처리하는 스레드입니다.
    클라이언트로부터 파일 전송 또는 수신 요청(#PUT, #GET)을 받아 처리하며, 파일 송수신 명령을 서버 핸들러(ServerHandler)에 전달합니다.

    주요 기능:
    - run(): 클라이언트로부터 파일 송수신 명령을 수신하고, 서버 핸들러를 통해 파일 업로드 및 다운로드를 처리합니다.
*/

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