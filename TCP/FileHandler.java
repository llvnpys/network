import java.io.*;
import java.net.*;

/* file을 관리하는 class */
public class FileHandler implements Runnable {
    private Socket filesocket;
    private BufferedReader fin;
    private File file;

    public FileHandler(Socket filesocket) {
        try {
            this.filesocket = filesocket;
            this.fin = new BufferedReader(new InputStreamReader(filesocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (filesocket.isConnected()) {
                String msg = fin.readLine();
                String[] str = msg.split(" ");
                /* 진행도를 확인하기 위해 64kbyte 공간 할당 */
                byte [] buf = new byte[65536];
                int len;

                /* Case 5 : PUT */
                /* client가 server로 file을 전송하게 해주는 함수, 여기서는 채팅방에 전송함. */
                if (str[0].equals("#PUT")) {
                    /* 저장할 주소 + 파일 이름 */
                    file = new File("/Users/nayunseong/IdeaProjects/TCP/src/serverfile/" + str[1]);
                    BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
                    DataInputStream din = new DataInputStream(new BufferedInputStream(filesocket.getInputStream()));

                    /* 더이상 읽어올 것이 없으면 종료 */
                    while ((len = din.readInt()) != -1) {
                        din.read(buf, 0, len);      /* 버퍼의 저장공간만큼 읽어옴 */
                        bout.write(buf, 0, len);
                        bout.flush();
                    }
                }

                /* Case 6 : GET */
                /* client가 server의 저장공간에서 파일을 다운받는 함수, 여기서는 채팅방에서 다운받음. */
                else if (str[0].equals("#GET")) {
                    file = new File("/Users/nayunseong/IdeaProjects/TCP/src/serverfile/" + str[1]);
                    BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(filesocket.getOutputStream()));

                    while ((len = bin.read(buf)) != -1) {
                        dout.writeInt(len);
                        dout.write(buf, 0, len);
                        dout.flush();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}