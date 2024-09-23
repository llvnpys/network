import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

/*
    이 클래스는 프로그램의 진입점 역할을 합니다.
*/

public class UDPChat {
    public static String roomName;
    public static String userName;
    public static InetAddress ipAddress;

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // 포트 설정
        // int port = Integer.parseInt(args[0]);
        int port = 1234;
        MulticastHandler multicastHandler = new MulticastHandler(port);

        // 송신 및 수신 쓰레드 시작
        new Thread(new SendThread(multicastHandler, in, port)).start();
        new Thread(new ReceiveThread(multicastHandler)).start();
    }
}