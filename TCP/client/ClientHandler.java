package client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientHandler {
    private Socket chatSocket;
    private Socket fileSocket;
    private BufferedReader userIn;
    private BufferedWriter chatOut;
    private BufferedReader chatIn;
    private DataOutputStream fileOut;
    private DataInputStream fileIn;
    private ExecutorService fileThreadPool;

    public ClientHandler(String serverAddress, int chatPort, int filePort) throws IOException {
        userIn = new BufferedReader(new InputStreamReader(System.in));

        // 서버와 채팅 및 파일 소켓 연결
        chatSocket = new Socket(serverAddress, chatPort);
        fileSocket = new Socket(serverAddress, filePort);

        // 채팅용 스트림 초기화
        chatOut = new BufferedWriter(new OutputStreamWriter(chatSocket.getOutputStream()));
        chatIn = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));

        // 파일 스트림 초기화
        fileOut = new DataOutputStream(new BufferedOutputStream(fileSocket.getOutputStream()));
        fileIn = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));

        fileThreadPool = Executors.newFixedThreadPool(2);
    }

    // 메시지 전송 메서드
    public void sendMessage(String message) throws IOException {
        // #PUT이나 #GET 명령어를 포함한 메시지인 경우 파일 전송 또는 수신 처리
        if (message.startsWith("#PUT")) {
            fileOut.writeUTF(message);
            fileOut.flush();
            fileThreadPool.submit(() -> {
                try {
                    sendFile(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });  // 파일 업로드 스레드풀에 제출
        } else if (message.startsWith("#GET")) {
            fileOut.writeUTF(message);
            fileOut.flush();
            fileThreadPool.submit(() -> {
                try {
                    receiveFile(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });  // 파일 다운로드 스레드풀에 제출
        } else {
            // 우선 메시지를 서버로 전송
            chatOut.write(message + "\n");
            chatOut.flush();
        }
    }

    // 파일 전송
    public void sendFile(String message) throws IOException {
        String[] messageSplit = message.split(" ");
        String fileName = messageSplit[1];

        // 파일 객체 생성
        String currentDir = System.getProperty("user.dir");
        String filePath = currentDir + "/TCP/TCP2/clientfile/" + fileName;
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        long totalSize = file.length();  // 파일의 전체 크기
        long sentSize = 0;  // 전송된 데이터 크기 초기화

        // 파일 크기를 먼저 전송
        fileOut.writeLong(totalSize);
        fileOut.flush();

        // 파일 데이터를 전송
        try (FileInputStream fileStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            // 파일 데이터 전송
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                fileOut.flush();

                sentSize += bytesRead;  // 전송된 바이트 수를 누적
                System.out.printf("Progress: %d/%d bytes sent (%.2f%%)\n", sentSize, totalSize, (sentSize * 100.0 / totalSize));
            }

            sendMessage("File " + fileName + " sent successfully.");
        } catch (IOException e) {
            System.out.println("Error during file transmission: " + e.getMessage());
        }
    }

    // 파일 다운로드
    public void receiveFile(String message) throws IOException {
        String[] messageSplit = message.split(" ");
        String fileName = messageSplit[1];

        // 파일 객체 생성
        String currentDir = System.getProperty("user.dir");
        String filePath = currentDir + "/TCP/TCP2/clientfile/" + fileName;
        File file = new File(filePath);

        long totalSize = fileIn.readLong();  // 서버로부터 파일 크기를 수신
        long receivedSize = 0;

        // 파일 수신 및 저장
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Receiving file: " + fileName);

            // 파일 데이터 수신
            while (receivedSize < totalSize && (bytesRead = fileIn.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                receivedSize += bytesRead;
            }

            System.out.println("File received and saved: " + fileName);
        } catch (IOException e) {
            System.out.println("Error during file reception: " + e.getMessage());
        }
    }

    public BufferedReader getUserIn() {
        return userIn;
    }

    public BufferedReader getChatIn() {
        return chatIn;
    }

    // 자원 반환 메서드
    public void close() throws IOException {
        if (chatSocket != null && !chatSocket.isClosed()) {
            chatSocket.close();
        }
        if (fileSocket != null && !fileSocket.isClosed()) {
            fileSocket.close();
        }
        if (chatIn != null) {
            chatIn.close();
        }
        if (chatOut != null) {
            chatOut.close();
        }
        if (userIn != null) {
            userIn.close();
        }
        if (fileOut != null) {
            fileOut.close();
        }
    }
}