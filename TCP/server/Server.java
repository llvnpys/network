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
