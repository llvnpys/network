import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

/* 일반 채팅을 관리하는 class */
public class ChattingHandler implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientInfo;      /* client의 정보를 입력받는 변수 */
    private String [] clientArr;    /* 입력받은 정보를 hashmap에 넣기 위해 string을 분리해줌 */
    private String clientName;
    private String chattingroom;
    private boolean flag = true;
    private ConcurrentHashMap<String, ConcurrentHashMap<Socket, String>> Information;
    private ConcurrentHashMap<Socket, String> Innermap;

    public ChattingHandler(Socket socket, ConcurrentHashMap Information){
        try {
            this.socket = socket;
            this.Information  = Information;
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        do{
            try{
                out.write("ex) #command chattingroom clientname" + "\n");
                out.flush();
                clientInfo = in.readLine();
                clientArr = clientInfo.split(" ");
                /* hash map의 value로 들어갈 hash map */
                Innermap = new ConcurrentHashMap<Socket, String>();

                if(clientArr.length != 3 || clientArr[0].charAt(0) != '#'){
                    out.write("failure. please again" + "\n");
                    out.flush();
                }
                else{
                    chattingroom = clientArr[1];
                    clientName =  clientArr[2];

                    /* 성공 시력성공, 실패 시 실패 메시지를 client에게 출력 */
                    /* Case 1 : CREATE */
                    if(clientArr[0].equals("#CREATE")) {
                        if (!(Information.containsKey(chattingroom))) {
                            Innermap = new ConcurrentHashMap<Socket, String>();
                            Information.put(chattingroom, Innermap);
                            Innermap.put(socket, clientName);
                            out.write("Successfully created the chattingroom " + "\n");
                            out.flush();
                            flag = false;
                        }
                        else {
                            out.write("failure. please again" + "\n");
                            out.flush();
                        }
                    }

                    /* Case 2 : JOIN */
                    else if(clientArr[0].equals("#JOIN")){
                        if(Information.containsKey(chattingroom)){
                            Information.get(chattingroom).put(socket, clientName);
                            out.write("Successfully joined the chattingroom" + "\n");
                            out.flush();
                            broadcastMsg(clientName + " joined the " + chattingroom);
                            flag = false;
                        }
                        else{
                            out.write("failure. please again" + "\n");
                            out.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }while (flag);  /* 실패 시 반복 */

        try {
            while (socket.isConnected()) {
                String msg = in.readLine();
                if (msg.length() != 0) {
                    /* 처음이 #일 때 */
                    if (msg.charAt(0) == '#') {

                        /* case 3 :EXIT */
                        if (msg.equals("#EXIT")) {
                            msg = "** " + clientName + " quited " + chattingroom + " **";
                            broadcastMsg(msg);
                            RemoveClient();
                        }

                        /* case 4 :STATUS */
                        /* 채팅방 이름과 참여한 사용자의 이름을 출력 */
                        else if(msg.equals("#STATUS")){
                            msg = "chattring room : " + chattingroom + " / client list : " + Information.get(chattingroom).values();
                            out.write(msg + "\n");
                            out.flush();
                        }
                    }
                    /* 일반 메시지 출력 */
                    else{
                        broadcastMsg("FROM " + clientName + ": " + msg);
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /* 나를 제외한 채팅방 멤버에게 메세지를 보내는 메소드 */
    public synchronized void broadcastMsg(String msg){
        try {
            /* 채팅방을 key로 한 value를 가져옴 */
            for(Socket s : Information.get(chattingroom).keySet()){
                /* 나는 pass */
                if(s == socket) continue;

                BufferedWriter broad = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                broad.write(msg + "\n");
                broad.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* client list에서 사용자를 제거해주는 메소드 */
    public synchronized void RemoveClient(){
        Information.get(chattingroom).remove(socket);
    }
}

