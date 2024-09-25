/*
    클라이언트의 연결을 처리하는 서버 클래스입니다.
    두 개의 포트를 사용하여 하나는 채팅을, 다른 하나는 파일 전송을 처리합니다.
    클라이언트 연결 시 채팅과 파일 전송을 위한 스레드를 생성하고, 멀티스레딩을 통해 여러 클라이언트의 요청을 동시 처리합니다.
    ExecutorService를 사용해 스레드 풀을 관리하여 효율적인 자원 관리를 구현합니다.

    주요 기능:
    - handleClientConnections(): 클라이언트 연결을 수락하고, 각 클라이언트에 대해 채팅 및 파일 전송 스레드를 생성하여 스레드 풀에서 실행합니다.
    - main(): 서버를 시작하고 클라이언트 연결을 처리하는 메인 메서드입니다.
*/


package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket chatServerSocket;
    private ServerSocket fileServerSocket;
    private ServerHandler serverHandler;
    private ExecutorService threadPool;

    public Server(int chatPort, int filePort) {
        try {
            chatServerSocket = new ServerSocket(chatPort);
            fileServerSocket = new ServerSocket(filePort);

            // ServerHandler 생성 (공통 로직을 담당)
            serverHandler = new ServerHandler();
            // 효율적인 자원 관리를 위한 쓰레드 풀, 최대 20개의 스레드 처리
            threadPool = Executors.newFixedThreadPool(20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleClientConnections() {
        try {
            while (!chatServerSocket.isClosed()) {
                System.out.println("Waiting for connection...");

                // 클라이언트 연결 수락
                Socket chatSocket = chatServerSocket.accept();
                Socket fileSocket = fileServerSocket.accept();
                System.out.println("Connection complete!");

                // 소켓 묶어서 관리
                ClientSocket clientSocket = new ClientSocket(chatSocket, fileSocket);

                // 각 스레드 생성 및 실행
                ChatServerThread chatServerThread = new ChatServerThread(clientSocket, serverHandler);
                FileServerThread fileServerThread = new FileServerThread(clientSocket, serverHandler);

                threadPool.execute(chatServerThread);
                threadPool.execute(fileServerThread);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
//        int chatPort = Integer.parseInt(args[0]);
//        int filePort = Integer.parseInt(args[1]);

        int chatPort = 2020;
        int filePort = 2021;


        Server server = new Server(chatPort, filePort);
        server.handleClientConnections();
    }
}
