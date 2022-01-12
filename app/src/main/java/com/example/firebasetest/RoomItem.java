package com.example.firebasetest;

public class RoomItem {
    public static final int ICE_GAME = 1;
    public static final int DAVINCI_GAME = 2;
    public static final int MASTER_USER = 1;
    public static final String USER_NUM = "com.example.firebasetest.USER_NUM";
    public static final String ROOM_NUM = "com.example.firebasetest.ROOM_NUM";
    public static final String GAMES = "Games";
    public static final String PLAYER = "Player";
    public static final String LOG = "Log";
    public static final String BOARD = "Board";
    public static final String EMPTY_PLAYER = "-";

    public String gameType;                // 1 = 살얼음판, 2 = 다빈치코드
    public String roomMasterId;            // 이메일에서 @ 이전 아이디
    public String isPassword;              // true = 패스워드 있음, false = 패스워드 없음 => 자물쇠 모양
    public String numberOfPlayer;          // 현재 방에 참가 중인 플레이어 수
    public String password = null;         // 패스워드
    public String roomNumber;              // 방 번호
    public String title;                   // 방 제목
    public String isGameStart;             // 게임 시작 했는지
    public RoomItem(){}
}
