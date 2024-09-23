import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
    메시지와 관련된 유틸리티 기능을 제공하는 클래스입니다. 예를 들어, 메시지를 SHA-256으로 해싱하거나, 메시지를 청크 단위로 분할하는 등의 작업을 수행합니다.

    주요 기능:
    String hashRoomName(String roomName): 채팅방 이름을 해싱하고 멀티캐스트 주소를 생성하는 메서드.
    String cutByte(String source, int chunkSize): 메시지를 청크 사이즈로 자르는 메서드.
*/

public class MessageUtils {
    public static byte[] hashRoomName(String roomName) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(roomName.getBytes());
        return sha.digest();
    }

    public static String cutByte(StringBuilder source, int chunkSize) {
        StringBuilder chunk = new StringBuilder();
        int currentLength = 0;

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            int charLength = String.valueOf(ch).getBytes().length;

            // 추가할 문자가 chunkSize 넘으면 중단
            if (currentLength + charLength > chunkSize) {
                break;
            }

            chunk.append(ch);
            currentLength += charLength;
        }

        return chunk.toString();
    }
}