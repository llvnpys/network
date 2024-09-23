package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ReceiveThread implements Runnable {
    private ClientHandler clientHandler;

    private BufferedReader in;

    public ReceiveThread(ClientHandler clientHandler) throws IOException {
        this.clientHandler = clientHandler;
        this.in = clientHandler.getChatIn();
    }

    @Override
    public void run() {
        try {
            String serverMessage;
            // 종료를 감지
            while (true) {
                // 서버로부터 메시지를 대기
                serverMessage = in.readLine();

                // 서버 소켓 닫힘 확인
                if (serverMessage != null) {
                    System.out.println(serverMessage);
                } else {
                    System.out.println("Server connection closed.");
                    break;
                }
            }
        } catch (IOException e) {

        } finally {
            try {
                clientHandler.close();
                System.out.println("receive thread shutting down.");
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
}