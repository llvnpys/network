# **UDP 멀티캐스트 채팅 프로그램**

## **1. 개요**

본 프로그램은 UDP 멀티캐스트를 이용하여 여러 사용자가 동시에 채팅할 수 있는 채팅 애플리케이션입니다. 멀티캐스트 통신을 활용하여 하나의 메시지를 그룹 내 모든 사용자에게 전송하며, 자바의 멀티스레드 기능을 사용하여 비동기적인 채팅 환경을 구현했습니다.

## **2. 프로그램 구조**

프로그램은 크게 4개의 주요 클래스(UDPChat, SendThread, ReceiveThread, MulticastHandler)로 구성되어 있습니다. 이 클래스들은 각각 채팅 프로그램의 초기화, 메시지 송신, 메시지 수신, 멀티캐스트 그룹 관리 및 통신을 담당합니다.

## **3. 설계 구조**

### **3.1. UDPChat (진입점 클래스)**

- **역할**: 프로그램의 진입점 역할을 담당하며, 채팅 방을 생성하고 송신 및 수신 스레드를 시작합니다.
- **주요 필드**:
    - public static String roomName: 참여할 채팅방의 이름을 저장합니다.
    - public static String userName: 현재 사용자의 이름을 저장합니다.
    - public static InetAddress ipAddress: 멀티캐스트 그룹의 IP 주소를 저장합니다.
- **주요 메서드**:
    - main(String[] args): 프로그램의 진입점으로, 사용자 입력을 처리하기 위한 BufferedReader와 MulticastHandler 객체를 생성하고, 송신 및 수신 스레드를 시작합니다.

### **3.2. MulticastHandler (멀티캐스트 그룹 관리)**

- **역할**: 멀티캐스트 그룹 생성 및 관리, 메시지 전송을 담당합니다.
- **주요 필드**:
    - private MulticastSocket socket: 멀티캐스트 통신을 수행할 소켓을 저장합니다.
- **주요 메서드**:
    - MulticastHandler(int port): 멀티캐스트 소켓을 생성하고 지정된 포트로 바인딩합니다.
    - InetAddress joinGroup(String roomName): 채팅방 이름을 해싱하여 멀티캐스트 그룹의 IP 주소를 생성하고, 해당 그룹에 참여합니다.
    - void leaveGroup(InetAddress ipAddress): 현재 참여 중인 멀티캐스트 그룹에서 나갑니다.
    - void sendMessage(String message, InetAddress ipAddress, int port): 주어진 메시지를 멀티캐스트 그룹으로 전송합니다.
    - MulticastSocket getSocket(): 현재 사용 중인 멀티캐스트 소켓을 반환합니다.


### **3.3. SendThread (송신 스레드)**

- **역할**: 사용자 입력을 받아 메시지를 전송하는 기능을 담당하는 스레드입니다.
- **주요 필드**:
    - private MulticastHandler multicastHandler: 멀티캐스트 그룹 관리 객체.
    - private BufferedReader in: 사용자 입력을 처리하는 BufferedReader.
    - private int port: 멀티캐스트 그룹의 포트 번호.
- **주요 메서드**:
    - run(): 사용자 입력을 받아 송신할 메시지를 처리합니다. 입력된 메시지가 명령어인지 여부를 확인하고, 일반 메시지일 경우 전송하며, 명령어인 경우 해당 명령을 처리합니다.
    - sendStatusMessage(String status): 사용자의 참여 및 종료와 관련된 상태 메시지를 멀티캐스트 그룹에 전송합니다.
    - processCommand(StringBuilder message): #JOIN 및 #EXIT와 같은 명령어를 처리합니다. 채팅방 참여 시 roomName, peerName, ipAddress를 설정하고, 채팅 종료 시 소켓을 종료합니다.
    - sendRegularMessage(StringBuilder message): 일반 채팅 메시지를 전송합니다. 메시지 크기를 확인하고, 필요한 경우 MessageUtils 클래스를 이용해 메시지를 청크 단위로 분리하여 전송합니다.


### **3.4. ReceiveThread (수신 스레드)**

- **역할**: 멀티캐스트 그룹에서 수신된 메시지를 받는 기능을 담당하는 스레드입니다.
- **주요 필드**:
    - private MulticastHandler multicastHandler: 멀티캐스트 그룹 관리 객체.
- **주요 메서드**:
    - run(): 소켓을 통해 멀티캐스트 메시지를 수신하고, 수신된 메시지가 자신이 보낸 것이 아닌 경우 이를 출력합니다.
    - receiveMessage(): 소켓으로부터 메시지를 수신하고, 메시지의 발신자가 자신이 아닌 경우 출력합니다.


### **3.5. MessageUtils (메시지 유틸리티 클래스)**

- **역할**: 메시지와 관련된 유틸리티 기능을 제공하는 클래스입니다. 메시지를 SHA-256으로 해싱하거나, 메시지를 청크 단위로 분할하는 등의 작업을 수행합니다.
- **주요 메서드**:
    - **byte[] hashRoomName(String roomName)**: 채팅방 이름을 해싱하고 멀티캐스트 주소를 생성하는 메서드입니다.
    - **String cutByte(StringBuilder source, int chunkSize)**: 메시지를 청크 사이즈로 자르는 메서드입니다.

## **4. 프로토콜 및 메시지 처리**

### **4.1. 명령어 처리**

- #JOIN roomName peerName: 채팅방에 참여하는 명령어로, 멀티캐스트 그룹의 IP 주소를 생성하고 참여합니다. 이 명령을 통해 roomName과 peerName을 설정하고, SendThread에서 참여 메시지를 전송합니다.
- #EXIT: 현재 참여 중인 채팅방에서 나가는 명령어입니다. 해당 명령을 통해 참여 중인 그룹에서 나가고 프로그램을 종료합니다.

### **4.2. 일반 메시지 처리**

- 일반 채팅 메시지는 송신 스레드에서 전송됩니다.
- **메시지 분할**: 메시지가 512바이트를 초과할 경우, MessageUtils 클래스의 cutByte 메서드를 사용해 청크 단위로 분할한 후 전송합니다.
- 수신 스레드에서는 수신된 메시지가 자신의 이름이 아닌 경우 출력하여, 다른 사용자의 메시지를 확인할 수 있습니다.

## **5. 프로그램 실행 방법**

### **1. 컴파일 및 실행**:

- 프로그램을 컴파일한 후, 실행 시 포트 번호를 인자로 전달합니다.
- 예시: javac UDPChat.java
  java UDPChat.java 5000

### **2. 명령어 사용**:

- #JOIN roomName peerName 명령어를 사용하여 채팅방에 참여합니다.
- #EXIT 명령어를 사용하여 채팅방을 종료합니다.

### **3. 메시지 송신 및 수신**:

- 채팅방에 참여한 후, 입력창에 메시지를 입력하고 Enter 키를 누르면 메시지가 전송됩니다.
- 다른 사용자가 보낸 메시지는 자동으로 수신되어 출력됩니다.