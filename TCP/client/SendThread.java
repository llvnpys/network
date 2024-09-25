/*
    클라이언트에서 사용자 입력을 받아 서버로 메시지를 전송하는 스레드입니다.
    사용자는 채팅 메시지나 명령어(#EXIT 등)를 입력할 수 있으며, 스레드는 이를 서버로 전송합니다.
    종료 명령(#EXIT)을 받으면 스레드가 종료됩니다.

    주요 기능:
    - run(): 사용자 입력을 처리하여 서버로 메시지를 전송하고, 종료 명령어가 입력되면 스레드를 종료합니다.
*/


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