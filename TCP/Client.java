import java.io.*;
import java.net.*;
import java.util.*;

/* client class */
public class Client {
    private Socket socket;
    private Socket filesocket;
    private BufferedReader in;
    private BufferedWriter out;
    private BufferedReader fin;
    private BufferedWriter fout;
    private File file;

    public Client(Socket socket, Socket filesocket){
        try {
            this.socket = socket;
            this.filesocket = filesocket;

            this.out  = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.fout = new BufferedWriter(new OutputStreamWriter(filesocket.getOutputStream()));
            this.fin  = new BufferedReader(new InputStreamReader(filesocket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 일반 채팅을 전송하는 메소드 */
    public void sendMsg(String Msg) {
        try {
            out.write(Msg + "\n");
            out.flush();
            /* 사용자가 입력한 값이 종료면 소켓 전부 닫음 */
            if(Msg.equals("#EIXT")){
                out.close();
                in.close();
                socket.close();
                filesocket.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* file 관련 내용을 전송ㅅ하는 메소드 */
    public void sendFile(String Msg, String [] str) {
        try{
            fout.write(Msg + "\n");
            fout.flush();

            byte [] buf = new byte[65536];
            int len;
            if(str[0].equals("#PUT")){
                file = new File("/Users/nayunseong/IdeaProjects/TCP/src/clientfile/" + str[1]);
                BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(filesocket.getOutputStream()));

                while ((len = bin.read(buf)) != -1) {
                    dout.writeInt(len);
                    dout.write(buf, 0, len);
                    dout.flush();
                    System.out.println("#");    /* 진행도를 표현 */
                }
                /* 전송완료를 알림 */
                out.write(str[1] + " file transmitted the chattingroom\n" );
                out.flush();
            }
            else if(str[0].equals("#GET")){
                receiveFile(str[1]);
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* 입력받은 메시지를 사용자에게 출력해주는 메소드 */
    public void receiveMsg() {
        new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    while(socket.isConnected()){
                        /* 메시지 수신 대기 */
                        String receiveMsg = in.readLine();
                        System.out.println(receiveMsg);
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /* 채팅방에서 파일을 다운받게 해주는 메소드 */
    public void receiveFile(String filename) {
        new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    while(filesocket.isConnected()){
                        file = new File("/Users/nayunseong/IdeaProjects/TCP/src/clientfile/" + filename);
                        byte [] buf = new byte[65536];
                        int len;
                        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
                        DataInputStream din = new DataInputStream(new BufferedInputStream(filesocket.getInputStream()));

                        while ((len = din.readInt()) != -1) {
                            din.read(buf, 0, len);
                            bout.write(buf, 0, len);
                            bout.flush();
                            System.out.println("#");
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        Socket socket= new Socket(args[0], Integer.parseInt(args[1]));
        Socket filesocket = new Socket(args[0], Integer.parseInt(args[2]));

        Client client = new Client(socket, filesocket);
        client.receiveMsg();


        Scanner sc = new Scanner(System.in);

        /* 입력할 메시지를 구분헤서 맞는 메소드에 넣어줌 */
        while(socket.isConnected()) {
                String Msg = sc.nextLine();
                if (Msg.charAt(0) == '#'){
                    String [] str = Msg.split(" ");
                    if(str[0].equals("#CREATE") || str[0].equals("#JOIN")|| str[0].equals("#EXIT") || str[0].equals("#STATUS")){
                        client.sendMsg(Msg);
                    }
                    else if(str[0].equals("#PUT") || str[0].equals("#GET")){
                        client.sendFile(Msg, str);
                    }
                }
                else{
                    client.sendMsg(Msg);
                }
            }
        }
    }



