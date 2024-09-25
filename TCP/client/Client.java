/*
    클라이언트의 메인 클래스로, 서버와의 연결을 설정하고 송신 및 수신 스레드를 시작합니다.
    서버 주소와 포트를 설정한 후, ClientHandler를 생성하여 클라이언트의 통신을 관리합니다.

    주요 기능:
    - main(String[] args): 프로그램의 진입점으로, 서버 주소 및 포트를 설정하고, 송신 및 수신 스레드를 시작합니다.
*/

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