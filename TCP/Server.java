import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

/* server class */
public class Server {
    private ServerSocket serversocket;
    private ServerSocket fileserversocket;

    /* 채팅방 이름에 대한 사용자 이름과 소켓을 한 번에 관리하기 위해 중첩 hashmap 이용, thread를 이용하기 때문에 ConcurrentHashMap 이용 */
    private ConcurrentHashMap<String, ConcurrentHashMap<Socket, String>> Information = new ConcurrentHashMap<String, ConcurrentHashMap<Socket, String>>();
    public Server(ServerSocket serversocket, ServerSocket fileserversocket) {
        this.serversocket = serversocket;
        this.fileserversocket = fileserversocket;
    }
    /* client의 연결을 기다리는 메소드 */
    public void startServer() {
        try{
            while (!serversocket.isClosed()){
                System.out.println(" Waiting for connection ");
                Socket socket = serversocket.accept();
                Socket filesocket = fileserversocket.accept();
                System.out.println(" connection complete! ");

                ChattingHandler chattinghandler = new ChattingHandler(socket, Information);
                FileHandler filehandler = new FileHandler(filesocket);

                Thread thread1 = new Thread(chattinghandler);
                thread1.start();

                Thread thread2 = new Thread(filehandler);
                thread2.start();

            }
        }catch (IOException e) {
            try{
                serversocket.close();
            }catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        try {
            /* 일반 채팅을 관리하는 소켓 */
            ServerSocket serversocket = new ServerSocket(Integer.parseInt(args[0]));
            /* 파일을 관리하는 소켓 */
            ServerSocket fileserversocket = new ServerSocket(Integer.parseInt(args[1]));

            Server server = new Server(serversocket, fileserversocket);
            server.startServer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
