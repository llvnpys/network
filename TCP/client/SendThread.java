package client;

import java.io.BufferedReader;
import java.io.IOException;

public class SendThread implements Runnable {
    private ClientHandler clientHandler;
    private BufferedReader in;


    public SendThread(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.in = clientHandler.getUserIn();
    }

    @Override
    public void run() {
        try {
            String userInput;
            // 사용자 입력 받기 + while문 종료 조건 추가
            while (true) {
                userInput = in.readLine();
                if (userInput == null) continue;

                // 명령어 처리
                if (userInput.startsWith("#")) {
                    if (userInput.startsWith("#EXIT")) {
                        clientHandler.sendMessage(userInput);  // 앱 종료
                        break;
                    } else {
                        clientHandler.sendMessage(userInput);  // 다른 명령어 처리
                    }
                } else {
                    // 일반 메시지 처리
                    clientHandler.sendMessage(userInput);
                }
            }
        } catch (IOException e) {
            System.out.println("send thread shutting down.");
        }
    }
}