import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UDP{

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        MulticastSocket peer_Socket = new MulticastSocket(Integer.parseInt(args[0]));
        String[] peer_Info;

        /* JOIN */
        do {
            String str = in.readLine();
            peer_Info = str.split(" ");

        } while(!(peer_Info[0].equals("#JOIN")));

        /* peer information */
        String chatting_Room = peer_Info[1];
        String peer_Name = peer_Info[2];

        /* chatting_room hashing */
        byte[] Hashing = SHA256(chatting_Room);
        InetAddress IPAddress = InetAddress.getByName("225." + ((byte) Hashing[29] & 0xff)  + "." + ((byte) Hashing[30] & 0xff) + "." + ((byte) Hashing[31] & 0xff));
        /* receive socket과 연결 */
        peer_Socket.joinGroup(IPAddress);

        System.out.println("Successfully joined " + chatting_Room + " " + IPAddress);

        /* send thread */
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramPacket Send_Packet;
                    byte [] Send_buf = new byte [512];

                    /* greeting */
                    String send_greeting = "** " + peer_Name + " joined " + chatting_Room + " **";
                    Send_buf = send_greeting.getBytes();

                    /* send packet 생성 */
                    Send_Packet = new DatagramPacket(Send_buf, Send_buf.length, IPAddress, Integer.parseInt(args[0]));

                    /* send packet 전송 */
                    try{
                        peer_Socket.send(Send_Packet);
                    } catch(IOException e){
                        e.printStackTrace();
                    }

                    /* send packet */
                    while (true) {
                        try{
                            String send_str = in.readLine();
                            if (send_str.length() != 0){
                                /* 처음이 #일 때 */
                                if(send_str.charAt(0) == '#'){
                                    /* #EXIT 일 때*/
                                    if(send_str.equals("#EXIT")){
                                        send_str = "** " + peer_Name + " quited " + chatting_Room + " **";
                                        Send_buf = send_str.getBytes();
                                        Send_Packet = new DatagramPacket(Send_buf, Send_buf.length, IPAddress, Integer.parseInt(args[0]));

                                        try{
                                            peer_Socket.send(Send_Packet);
                                            peer_Socket.leaveGroup(IPAddress);
                                            System.exit(0);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    /* #EXIT가 아닌 #은 무시 */
                                }

                                /* send data */
                                else {
                                    /* 송신자 표시 */
                                    send_str = peer_Name + ": " + send_str;

                                    /* byte 길이가 chunk를 초과할 때 */
                                    while(send_str.getBytes().length > 512) {
                                        /* byte 길이가 chunk를 초과할 때 */
                                        String cut_byte = cut_Byte(send_str, 512);
                                        int index = cut_byte.length();
                                        Send_buf = cut_byte.getBytes();

                                        /* 잘린 부분으로 String 구성 */
                                        send_str = send_str.substring(index-1, send_str.length());
                                        send_str = peer_Name + ": " + send_str;
                                        Send_Packet = new DatagramPacket(Send_buf, Send_buf.length, IPAddress, Integer.parseInt(args[0]));

                                        try {
                                            peer_Socket.send(Send_Packet);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    /* 다 잘리고 남은 String 전송 */
                                    if(send_str.length() != 0){
                                        Send_buf = send_str.getBytes();
                                        Send_Packet = new DatagramPacket(Send_buf, Send_buf.length, IPAddress, Integer.parseInt(args[0]));
                                        try{
                                            peer_Socket.send(Send_Packet);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* receive thread */
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramPacket Receive_Packet;
                    while (true) {
                        byte [] Receive_buf = new byte [512];
                        /* receive packet 생성 */
                        Receive_Packet = new DatagramPacket(Receive_buf, Receive_buf.length);

                        /* send socket 수신 대기 */
                        try{
                            peer_Socket.receive(Receive_Packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        /* send socket의 packet으로부터 data 추출 */
                        String Receive_str = new String(Receive_Packet.getData()).substring(0, Receive_Packet.getLength());

                        /* 송신자 화면에는 나오지 않게 함 */
                        String [] equal_name = Receive_str.split(":");
                        if (!(equal_name[0].equals(peer_Name))){
                            System.out.println(Receive_str);
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* hashing method */
    public static byte [] SHA256(String room) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(room.getBytes());
        byte[] digest = sha.digest();
        return digest;
    }

    /* cut_byte method */
    public static String cut_Byte(String source, int cutLength) {
        StringBuffer sb = null;
        if (!source.isEmpty()) {
            sb = new StringBuffer(cutLength);
            int count = 0;
            for (char ch : source.toCharArray()) {
                count += String.valueOf(ch).getBytes().length;
                sb.append(ch);
                if (count > cutLength)
                    break;
            }
        }
        return sb.toString();
    }
}
