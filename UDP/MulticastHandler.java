import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;

/*
    멀티캐스트 그룹의 생성과 관리를 담당하는 클래스입니다.
    채팅방을 생성하고, 멀티캐스트 주소를 생성하고 그룹에 참여하는 등의 작업을 처리합니다.

    주요 기능:
    - joinGroup(String roomName): 채팅방 이름을 해싱하여 멀티캐스트 주소를 생성하고, 해당 멀티캐스트 그룹에 참여합니다.
    - leaveGroup(InetAddress ipAddress): 지정된 멀티캐스트 그룹에서 나가도록 처리합니다.
    - sendMessage(String message, InetAddress ipAddress, int port): 주어진 메시지를 멀티캐스트 그룹으로 전송합니다.
    - getSocket(): 현재 사용 중인 멀티캐스트 소켓을 반환합니다.
*/

public class MulticastHandler {
    private MulticastSocket socket;

    public MulticastHandler(int port) throws IOException {
        this.socket = new MulticastSocket(port);
    }

    public InetAddress joinGroup(String roomName) throws IOException, NoSuchAlgorithmException {
        byte[] hash = MessageUtils.hashRoomName(roomName);
        InetAddress ipAddress = InetAddress.getByName("225." + ((hash[29] & 0xff)) + "." + ((hash[30] & 0xff)) + "." + ((hash[31] & 0xff)));
        socket.joinGroup(ipAddress);
        return ipAddress;
    }

    public void leaveGroup(InetAddress ipAddress) throws IOException {
        socket.leaveGroup(ipAddress);
    }

    public void sendMessage(String message, InetAddress ipAddress, int port) throws IOException {
        byte[] buffer = message.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ipAddress, port);
        socket.send(packet);
    }

    public MulticastSocket getSocket() {
        return this.socket;
    }
}