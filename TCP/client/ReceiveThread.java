/*
    서버로부터 수신된 메시지를 처리하는 스레드입니다.
    클라이언트는 서버로부터 메시지를 지속적으로 대기하며, 메시지를 받으면 이를 콘솔에 출력합니다. 서버 연결이 종료되면 해당 스레드도 종료됩니다.

    주요 기능:
    - run(): 서버로부터 메시지를 수신하고 이를 출력하며, 연결이 끊어지면 스레드를 종료합니다.
*/

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