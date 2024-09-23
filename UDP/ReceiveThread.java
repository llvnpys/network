
/*
    수신 쓰레드를 담당하는 클래스입니다. 이 클래스도 Runnable을 구현하여 메시지 수신 로직을 관리합니다.

    주요 기능:
    run(): 메시지 수신 루프를 실행하고, 수신된 메시지를 출력합니다.
    receiveMessage(): 수신된 메시지를 받아 출력하는 메서드. 메시지의 발신자가 자신인지 확인하고, 필터링하는 로직을 포함합니다.
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class ReceiveThread implements Runnable {
    private MulticastHandler multicastHandler;

    public ReceiveThread(MulticastHandler handler) {
        this.multicastHandler = handler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                receiveMessage();
            }
            // 소켓이 종료되면 스레드도 자동 종료
        } catch (IOException e) {
        }
    }

    private void receiveMessage() throws IOException {

        byte[] receiveBuffer = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        // 메시지 수신 대기
        multicastHandler.getSocket().receive(receivePacket);

        // 수신된 패킷의 데이터를 문자열로 변환
        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

        // 발신자가 자신이 아닌 경우에만 메시지 출력
        String[] messageParts = message.split(":");
        if (!messageParts[0].equals(UDPChat.userName)) {
            System.out.println(message);
        }

        // 버퍼 초기화
        Arrays.fill(receiveBuffer, (byte) 0);
    }
}