package server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler {

    private ConcurrentHashMap<String, List<ClientSocket>> chatRooms;

    public ServerHandler() {
        chatRooms = new ConcurrentHashMap<>();
    }

    // 채팅방 생성
    public void create(ClientSocket mySocket, String roomName, String clientName) throws IOException {
        // 채팅방 유무 확인
        if (chatRooms.containsKey(roomName)) {
            // 오류 메시지
            String errorMessage = "The chatroom already exists.";
            sendToMe(mySocket.getChatOut(), errorMessage);
        } else {
            mySocket.setRoomName(roomName);
            mySocket.setClientName(clientName);
            // 새 채팅방 생성 및 사용자 추가
            List<ClientSocket> users = new ArrayList<>();
            users.add(mySocket);
            chatRooms.put(roomName, users);
            // 성공 메시지
            String successMessage = "Chatroom '" + roomName + "' created successfully. You have joined the " + roomName;
            sendToMe(mySocket.getChatOut(), successMessage);
        }
    }

    // 채팅방 참가
    public void join(ClientSocket mySocket, String roomName, String clientName) throws IOException {
        // 채팅방 유무 확인
        if (!chatRooms.containsKey(roomName)) {
            // 오류 메시지
            String errorMessage = "The chatroom does not exist.";
            sendToMe(mySocket.getChatOut(), errorMessage);
        } else {
            mySocket.setRoomName(roomName);
            mySocket.setClientName(clientName);

            chatRooms.get(roomName).add(mySocket);
            // 성공 메시지
            String successMessage = clientName + " have successfully joined the " + roomName;
            broadcastToRoom(chatRooms.get(roomName), mySocket, successMessage);

            String successMessage2 = "you have successfully joined the " + roomName;
            sendToMe(mySocket.getChatOut(), successMessage2);
        }
    }

    // 채팅방 상태 확인
    public void status(ClientSocket mySocekt) throws IOException {
        String roomName = mySocekt.getRoomName();

        if (roomName == null || !chatRooms.containsKey(roomName)) {
            String errorMessage = "You are not in any chatroom.";
            sendToMe(mySocekt.getChatOut(), errorMessage);
            return;
        }

        // 방 이름 출력
        String message = "Room Name: " + roomName + "\n";

        // 방에 있는 클라이언트 리스트 출력
        List<ClientSocket> clientList = chatRooms.get(roomName);
        message += "Client List: ";

        for (ClientSocket cs : clientList) {
            message += cs.getClientName() + " ";
        }

        // 상태 메시지 클라이언트에게 전송
        sendToMe(mySocekt.getChatOut(), message);
    }

    // 채팅방 나가기
    public boolean exit(ClientSocket mySocket) throws IOException {
        String roomName = mySocket.getRoomName();

        // 클라이언트가 속한 방이 없을 경우
        if (roomName == null || !chatRooms.containsKey(roomName)) {
            String errorMessage = "You are not in any chatroom.";
            sendToMe(mySocket.getChatOut(), errorMessage);
            return false;
        }

        // 클라이언트가 속한 방에서 해당 클라이언트를 제거
        List<ClientSocket> clientList = chatRooms.get(roomName);
        clientList.remove(mySocket);

        // 다른 클라이언트들에게 나갔다는 메시지 브로드캐스트
        String exitMessage = mySocket.getClientName() + " has left the " + roomName;
        broadcastToRoom(clientList, mySocket, exitMessage);

        // 만약 방에 남아있는 클라이언트가 없으면 방 제거
        if (clientList.isEmpty()) {
            chatRooms.remove(roomName);
        }
        return true;
    }

    // 일반 메시지
    public void sendMessage(ClientSocket mySocket, String message) throws IOException {
        String roomName = mySocket.getRoomName();

        // 클라이언트가 속한 방이 없을 경우
        if (roomName == null || !chatRooms.containsKey(roomName)) {
            String errorMessage = "You are not in any chatroom.";
            sendToMe(mySocket.getChatOut(), errorMessage);
            return;
        }

        // 메시지를 브로드캐스트할 채팅방의 클라이언트 리스트 가져오기
        List<ClientSocket> clientList = chatRooms.get(roomName);

        // 보내는 사람의 이름을 메시지에 포함시킴
        String broadcastMessage = mySocket.getClientName() + ": " + message;

        // 나를 제외한 다른 클라이언트들에게 메시지 전송
        broadcastToRoom(clientList, mySocket, broadcastMessage);
    }

    // 파일 수신
    public void put(ClientSocket mySocket, String fileName) throws IOException {
        // 파일 저장 경로 설정
        String currentDir = System.getProperty("user.dir");
        String filePath = currentDir + "/TCP/TCP2/serverfile/" + fileName;
        File file = new File(filePath);

        // 파일 크기 받기
        long totalSize = mySocket.getFileIn().readLong();
        long receivedSize = 0;

        // 파일 수신 및 저장
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Receiving file: " + fileName);

            // 파일 데이터 수신 및 저장
            while (receivedSize < totalSize && (bytesRead = mySocket.getFileIn().read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                receivedSize += bytesRead;
            }

            System.out.println("File received and saved: " + fileName);
        } catch (IOException e) {
            System.out.println("Error during file reception: " + e.getMessage());
        }
    }

    // 파일 전송
    public void get(ClientSocket mySocket, String fileName) throws IOException {
        // 파일 경로 설정
        String currentDir = System.getProperty("user.dir");
        String filePath = currentDir + "/TCP/TCP2/serverfile/" + fileName;
        File file = new File(filePath);

        if (!file.exists()) {
            sendToMe(mySocket.getChatOut(), "File not found.");
            return;
        }

        long totalSize = file.length();  // 파일의 전체 크기
        long sentSize = 0;  // 전송된 데이터 크기 초기화

        // 파일 크기를 먼저 전송
        mySocket.getFileOut().writeLong(totalSize);
        mySocket.getFileOut().flush();

        // 파일 데이터 전송
        try (FileInputStream fileIn = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            System.out.println("Sending file: " + fileName);

            while ((bytesRead = fileIn.read(buffer)) != -1) {
                mySocket.getFileOut().write(buffer, 0, bytesRead);
                mySocket.getFileOut().flush();
            }

            sendToMe(mySocket.getChatOut(), "File sent successfully.");
        } catch (IOException e) {
            System.out.println("Error during file transmission: " + e.getMessage());
        }
    }


    // 채팅방 내 나를 제외한 모든 사용자에게 메시지 전송
    private void broadcastToRoom(List<ClientSocket> clientSockets, ClientSocket mySocket, String message) throws IOException {
        for (ClientSocket socketPair : clientSockets) {
            if (socketPair.equals(mySocket)) continue;

            socketPair.getChatOut().write(message + "\n");
            socketPair.getChatOut().flush();
        }
    }

    // 나에게 메시지 전송
    private void sendToMe(BufferedWriter out, String message) throws IOException {
        out.write(message + "\n");
        out.flush();
    }


}