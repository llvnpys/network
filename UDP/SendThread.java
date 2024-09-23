import java.io.BufferedReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/*
    송신 쓰레드를 담당하는 클래스입니다. 이 클래스는 Runnable을 구현하여 메시지 송신 로직을 처리합니다.

    주요 기능:
    run(): 메시지 송신 루프를 실행하고, 사용자의 명령어를 처리합니다.
    sendStatusMessage(String status): 채팅방 참여, 종료 메시지를 전송하는 메서드.
    processCommand(StringBuilder message): 명령어 메시지를 처리하는 메서드.
    sendRegularMessage(StringBuilder message): 일반 메시지를 전송하는 메서드. 메시지 크기를 확인하고, 필요한 경우 MessageUtils 클래스를 이용해 청크 단위로 분리합니다.
*/


public class SendThread implements Runnable {
    private MulticastHandler multicastHandler;
    private BufferedReader in;

    private int port;

    public SendThread(MulticastHandler handler, BufferedReader in, int port) {
        this.multicastHandler = handler;
        this.in = in;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // 사용자 입력 및 메시지 전송 루프
            while (true) {
                StringBuilder message = new StringBuilder(in.readLine());

                if (message != null && message.length() != 0) {

                    // 명령어 확인
                    if(message.charAt(0) == '#'){
                        processCommand(message);

                    } else {
                        // 일반 메시지 전송
                        if (UDPChat.roomName != null) {
                            sendRegularMessage(message);
                        } else {
                            System.out.println("You have not joined a chat room. Cannot send the message.");
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendStatusMessage(String status) {
        String greeting = "** " + UDPChat.userName + " " + status + " " + UDPChat.roomName + " **";
        try {
            multicastHandler.sendMessage(greeting, UDPChat.ipAddress, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void processCommand(StringBuilder message) {
        try {
            String[] messageSplit = message.toString().split(" ");

            if (messageSplit[0].equals("#JOIN") && messageSplit.length == 3){
                UDPChat.roomName = messageSplit[1];
                UDPChat.userName = messageSplit[2];
                UDPChat.ipAddress = multicastHandler.joinGroup(UDPChat.roomName);

                sendStatusMessage("joined");
                System.out.println("Successfully joined " + UDPChat.roomName + " " + UDPChat.ipAddress);
                return;
            }
            if (messageSplit[0].equals("#EXIT")) {
                if(UDPChat.roomName != null){
                    sendStatusMessage("quited");
                    multicastHandler.leaveGroup(UDPChat.ipAddress);
                }
                System.exit(0);
                return;
            }

            // 잘못된 명령어 처리
            System.out.println("Invalid command.");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendRegularMessage(StringBuilder message) {

        try {
            String prefix = UDPChat.userName + ": ";

            do {
                // 청크 단위로 메시지 자르기
                message.insert(0, prefix);
                String chunk = MessageUtils.cutByte(message, 512);
                multicastHandler.sendMessage(chunk, UDPChat.ipAddress, port);


                // 잘라낸 청크 이후의 메시지를 업데이트
                message.delete(0, chunk.length());
            } while (message.length() > 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}