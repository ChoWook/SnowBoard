package com.example.firebasetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingDeque;

public class DavinciGameActivity extends AppCompatActivity {

    //  게임 순서
    // 플레이어가 4개씩 블럭을 가져감
    // 순서대로 놓아야 함
    // 자기 차례에 타일 중 하나를 가져와 순서에 맞게 끼우고 나서 추리
    // 추리에 실패하면 방금 가져온 타일 까고 턴을 넘김
    // 추리에 맞으면 턴을 넘길지 더 추리할지 정함

    public final static int NONE = 0;       // 0 = 아무도 가져가지 않음
    public final static int UP_1P = 1;      // 1 = p1인데 추리당하지 않음
    public final static int UP_2P = 2;      // 2 = p2인데 추리당하지 않음
    public final static int UP_3P = 3;      // 3 = p3인데 추리당하지 않음
    public final static int DOWN_1P = 11;   // 11 = p1인데 추리 당함
    public final static int DOWN_2P = 12;   // 12 = p2인데 추리 당함
    public final static int DOWN_3P = 13;   // 13 = p3인데 추리 당함
    public final static int MAX_BLOCK = 26;
    public final static int BLACK = 21;
    public final static int WHITE = 22;
    public final static int WIN = 31;
    public final static int LOSE = 32;
    public final static int[] blocksID = {
            R.id.img_opp1_block_black_00,   R.id.img_opp1_block_white_00,
            R.id.img_opp1_block_black_01,   R.id.img_opp1_block_white_01,
            R.id.img_opp1_block_black_02,   R.id.img_opp1_block_white_02,
            R.id.img_opp1_block_black_03,   R.id.img_opp1_block_white_03,
            R.id.img_opp1_block_black_04,   R.id.img_opp1_block_white_04,
            R.id.img_opp1_block_black_05,   R.id.img_opp1_block_white_05,
            R.id.img_opp1_block_black_06,   R.id.img_opp1_block_white_06,
            R.id.img_opp1_block_black_07,   R.id.img_opp1_block_white_07,
            R.id.img_opp1_block_black_08,   R.id.img_opp1_block_white_08,
            R.id.img_opp1_block_black_09,   R.id.img_opp1_block_white_09,
            R.id.img_opp1_block_black_10,   R.id.img_opp1_block_white_10,
            R.id.img_opp1_block_black_11,   R.id.img_opp1_block_white_11,
            R.id.img_opp1_block_black_12,   R.id.img_opp1_block_white_12,

            R.id.img_opp2_block_black_00,   R.id.img_opp2_block_white_00,
            R.id.img_opp2_block_black_01,   R.id.img_opp2_block_white_01,
            R.id.img_opp2_block_black_02,   R.id.img_opp2_block_white_02,
            R.id.img_opp2_block_black_03,   R.id.img_opp2_block_white_03,
            R.id.img_opp2_block_black_04,   R.id.img_opp2_block_white_04,
            R.id.img_opp2_block_black_05,   R.id.img_opp2_block_white_05,
            R.id.img_opp2_block_black_06,   R.id.img_opp2_block_white_06,
            R.id.img_opp2_block_black_07,   R.id.img_opp2_block_white_07,
            R.id.img_opp2_block_black_08,   R.id.img_opp2_block_white_08,
            R.id.img_opp2_block_black_09,   R.id.img_opp2_block_white_09,
            R.id.img_opp2_block_black_10,   R.id.img_opp2_block_white_10,
            R.id.img_opp2_block_black_11,   R.id.img_opp2_block_white_11,
            R.id.img_opp2_block_black_12,   R.id.img_opp2_block_white_12,

            R.id.img_my_block_black_00,   R.id.img_my_block_white_00,
            R.id.img_my_block_black_01,   R.id.img_my_block_white_01,
            R.id.img_my_block_black_02,   R.id.img_my_block_white_02,
            R.id.img_my_block_black_03,   R.id.img_my_block_white_03,
            R.id.img_my_block_black_04,   R.id.img_my_block_white_04,
            R.id.img_my_block_black_05,   R.id.img_my_block_white_05,
            R.id.img_my_block_black_06,   R.id.img_my_block_white_06,
            R.id.img_my_block_black_07,   R.id.img_my_block_white_07,
            R.id.img_my_block_black_08,   R.id.img_my_block_white_08,
            R.id.img_my_block_black_09,   R.id.img_my_block_white_09,
            R.id.img_my_block_black_10,   R.id.img_my_block_white_10,
            R.id.img_my_block_black_11,   R.id.img_my_block_white_11,
            R.id.img_my_block_black_12,   R.id.img_my_block_white_12
    };

    public final static int[] downBlockDrawable = {
            R.drawable.ice_fall_block_0,    R.drawable.white_fall_block_0,
            R.drawable.ice_fall_block_1,    R.drawable.white_fall_block_1,
            R.drawable.ice_fall_block_2,    R.drawable.white_fall_block_2,
            R.drawable.ice_fall_block_3,    R.drawable.white_fall_block_3,
            R.drawable.ice_fall_block_4,    R.drawable.white_fall_block_4,
            R.drawable.ice_fall_block_5,    R.drawable.white_fall_block_5,
            R.drawable.ice_fall_block_6,    R.drawable.white_fall_block_6,
            R.drawable.ice_fall_block_7,    R.drawable.white_fall_block_7,
            R.drawable.ice_fall_block_8,    R.drawable.white_fall_block_8,
            R.drawable.ice_fall_block_9,    R.drawable.white_fall_block_9,
            R.drawable.ice_fall_block_10,   R.drawable.white_fall_block_10,
            R.drawable.ice_fall_block_11,   R.drawable.white_fall_block_11,
            R.drawable.ice_fall_block_12,   R.drawable.white_fall_block_12,
    };

    int[] blockOwner;

    int[] blackOrder, whiteOrder;

    int userNum, roomNum;
    DavinciGameBoard davinciGameBoard;
    TextView[] textOpponentsID;
    TextView textMyID, textGuide, textLeftBlackBlock, textLeftWhiteBlock, textGuessNum;
    ImageView[][] imgOpponentsBlock;
    ImageView[] imgMyBlock, imgOpponents;
    ImageView imgStartReady, imgPass, imgGuess, imgMyTurn, imgLeftBlackBlock, imgLeftWhiteBlock;
    TextView imgGuessLeft, imgGuessRight;
    TableLayout layoutGuess;

    DatabaseReference gameRef, playerRef, logRef, boardRef, blackRef, whiteRef;
    FirebaseDatabase database;
    FirebaseUser user;
    String email, id, numberOfPlayer, isGameStart;
    boolean isDestroyCauseByMaster, isMyTurn, isGuessBlock, isOver;
    Player player;
    int guessNum;
    int lastBlock;

    View dialogView;
    AlertDialog.Builder dlg;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_davinci_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        userNum = intent.getIntExtra(RoomItem.USER_NUM, 0);
        roomNum = intent.getIntExtra(RoomItem.ROOM_NUM, 0);

        blockOwner = new int[MAX_BLOCK];
        for(int i = 0; i < MAX_BLOCK; i++){
            blockOwner[i] = NONE;
        }
        blackOrder = new int[MAX_BLOCK / 2];
        whiteOrder = new int[MAX_BLOCK / 2];
        imgOpponents = new ImageView[3];
        textOpponentsID = new TextView[3];
        imgOpponentsBlock = new ImageView[3][MAX_BLOCK];
        imgMyBlock = new ImageView[MAX_BLOCK];
        imgOpponents[1] = findViewById(R.id.img_turn_opp1);
        imgOpponents[2] = findViewById(R.id.img_turn_opp2);
        imgStartReady = findViewById(R.id.img_davinci_ready_start);
        imgPass = findViewById(R.id.img_davinici_turn_pass);
        imgGuess = findViewById(R.id.img_davinici_guess);
        imgMyTurn = findViewById(R.id.img_turn_my);
        textOpponentsID[1] = findViewById(R.id.txt_opp1_id);
        textOpponentsID[2] = findViewById(R.id.txt_opp2_id);
        textMyID = findViewById(R.id.txt_my_id);
        textGuide = findViewById(R.id.txt_guide);
        imgLeftBlackBlock = findViewById(R.id.img_left_ice_block);
        imgLeftWhiteBlock = findViewById(R.id.img_left_white_block);
        textLeftBlackBlock = findViewById(R.id.txt_num_left_ice_block);
        textLeftWhiteBlock = findViewById(R.id.txt_num_left_white_block);
        layoutGuess = findViewById(R.id.layout_guess);
        textGuessNum = findViewById(R.id.txt_guess_num);
        imgGuessLeft = findViewById(R.id.img_guess_left);
        imgGuessRight = findViewById(R.id.img_guess_right);

        for (int i = 1; i <= 2; i++) {
            for (int j = 0; j < MAX_BLOCK; j++) {
                imgOpponentsBlock[i][j] = findViewById(blocksID[(i - 1) * MAX_BLOCK + j]);
                if(j % 2 == 0){
                    imgOpponentsBlock[i][j].setImageResource(R.drawable.ice_back_block);
                }
                else{
                    imgOpponentsBlock[i][j].setImageResource(R.drawable.white_back_block);
                }
                imgOpponentsBlock[i][j].setVisibility(View.GONE);
                final int _i = i;
                final int _j = j;
                imgOpponentsBlock[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isGuessBlock){
                            layoutGuess.setVisibility(View.GONE);
                            Map<String, Object> block = new HashMap<>();
                            Map<String, Object> cntP = new HashMap<>();
                            int tmpBlockOwner = NONE;
                            String tmpPlayer = "0";
                            String tmpCnt = "13";
                            int myBlockOwner = NONE;
                            String myPlayer = "0";
                            String myCnt = "13";
                            switch (userNum){
                                case 1 :
                                    if( _i == 1){
                                        tmpBlockOwner = DOWN_2P;
                                        tmpPlayer = "cnt2P";
                                        tmpCnt = davinciGameBoard.cnt2P;
                                    }
                                    else{
                                        tmpBlockOwner = DOWN_3P;
                                        tmpPlayer = "cnt3P";
                                        tmpCnt = davinciGameBoard.cnt3P;
                                    }
                                    myBlockOwner = DOWN_1P;
                                    myPlayer = "cnt1P";
                                    myCnt = davinciGameBoard.cnt1P;
                                    break;
                                case 2 :
                                    if( _i == 1){
                                        tmpBlockOwner = DOWN_1P;
                                        tmpPlayer = "cnt1P";
                                        tmpCnt = davinciGameBoard.cnt1P;
                                    }
                                    else{
                                        tmpBlockOwner = DOWN_3P;
                                        tmpPlayer = "cnt3P";
                                        tmpCnt = davinciGameBoard.cnt3P;
                                    }
                                    myBlockOwner = DOWN_2P;
                                    myPlayer = "cnt2P";
                                    myCnt = davinciGameBoard.cnt2P;
                                    break;
                                case 3 :
                                    if( _i == 1){
                                        tmpBlockOwner = DOWN_1P;
                                        tmpPlayer = "cnt1P";
                                        tmpCnt = davinciGameBoard.cnt1P;
                                    }
                                    else{
                                        tmpBlockOwner = DOWN_2P;
                                        tmpPlayer = "cnt2P";
                                        tmpCnt = davinciGameBoard.cnt2P;
                                    }
                                    myBlockOwner = DOWN_3P;
                                    myPlayer = "cnt3P";
                                    myCnt = davinciGameBoard.cnt3P;
                                    break;
                            }
                            if(guessNum == _j / 2){
                                // 맞았으므로 이거 깜
                                tmpCnt = Integer.toString(Integer.parseInt(tmpCnt) - 1);
                                block.put(Integer.toString(_j), tmpBlockOwner);
                                cntP.put(tmpPlayer, tmpCnt);
                                boardRef.updateChildren(block);
                                logRef.updateChildren(cntP);

                                // 더할지 말지 선택
                                textGuide.setText(R.string.guess_choice);
                                imgPass.setVisibility(View.VISIBLE);
                                imgGuess.setVisibility(View.VISIBLE);
                            }
                            else{
                                // 틀렸으므로 자기 카드 오픈
                                myCnt = Integer.toString(Integer.parseInt(myCnt) - 1);
                                cntP.put(myPlayer, myCnt);
                                block.put(Integer.toString(lastBlock), myBlockOwner);
                                boardRef.updateChildren(block);
                                logRef.updateChildren(cntP);

                                // 턴 넘기기
                                nextTurn();
                            }
                            isGuessBlock = false;
                        }
                    }
                });
            }
        }
        for (int j = 0; j < MAX_BLOCK; j++) {
            imgMyBlock[j] = findViewById(blocksID[2 * MAX_BLOCK + j]);
            imgMyBlock[j].setVisibility(View.GONE);
        }

        isDestroyCauseByMaster = false;
        isMyTurn = false;
        isGuessBlock = false;
        isGameStart = "false";
        isOver = false;

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference(RoomItem.GAMES).child(Integer.toString(roomNum));
        playerRef = gameRef.child(RoomItem.PLAYER);
        logRef = gameRef.child(RoomItem.LOG);
        boardRef= logRef.child(RoomItem.BOARD);
        blackRef = logRef.child("black");
        whiteRef = logRef.child("white");

        if (user != null) {
            email = user.getEmail();
            String[] splits = email.split("@", 2);
            id = splits[0];
        }

        textMyID.setText(id);

        if (userNum == 0 || roomNum == 0) {
            Log.w("DAVINCI_GAME", RoomItem.USER_NUM + "== 0 || " + RoomItem.ROOM_NUM + "== 0");
            finish();
        }

        gameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (userNum != RoomItem.MASTER_USER) {
                    isDestroyCauseByMaster = true;
                    Toast.makeText(getApplicationContext(), "방장이 게임에서 나갔습니다.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child("numberOfPlayer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numberOfPlayer = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child("isGameStart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isGameStart = dataSnapshot.getValue(String.class);
                if (isGameStart.equals("true")) {
                    imgStartReady.setVisibility(View.GONE);
                    textGuide.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isGameStart.equals("false")) {

                    player = dataSnapshot.getValue(Player.class);
                    if (player == null) return;
                    if (userNum == RoomItem.MASTER_USER) {
                        if (player.p2.equals(RoomItem.EMPTY_PLAYER)) {
                            imgOpponents[1].setVisibility(View.INVISIBLE);
                            textOpponentsID[1].setVisibility(View.INVISIBLE);
                        } else {
                            imgOpponents[1].setVisibility(View.VISIBLE);
                            textOpponentsID[1].setText(player.p2);
                            textOpponentsID[1].setVisibility(View.VISIBLE);
                        }
                        if (player.p3.equals(RoomItem.EMPTY_PLAYER)) {
                            imgOpponents[2].setVisibility(View.INVISIBLE);
                            textOpponentsID[2].setVisibility(View.INVISIBLE);
                        } else {
                            imgOpponents[2].setVisibility(View.VISIBLE);
                            textOpponentsID[2].setText(player.p3);
                            textOpponentsID[2].setVisibility(View.VISIBLE);
                        }
                    } else {
                        imgOpponents[1].setImageResource(R.drawable.davinci_1p);
                        textOpponentsID[1].setText(player.p1);
                        imgOpponents[1].setVisibility(View.VISIBLE);

                        if (userNum == 2) {
                            imgMyTurn.setImageResource(R.drawable.davinci_2p);
                            if (player.p3.equals(RoomItem.EMPTY_PLAYER)) {
                                imgOpponents[2].setVisibility(View.INVISIBLE);
                                textOpponentsID[2].setVisibility(View.INVISIBLE);
                            } else {
                                imgOpponents[2].setVisibility(View.VISIBLE);
                                textOpponentsID[2].setText(player.p3);
                                textOpponentsID[2].setVisibility(View.VISIBLE);
                            }
                        } else {
                            imgMyTurn.setImageResource(R.drawable.davinci_3p);
                            if (player.p2.equals(RoomItem.EMPTY_PLAYER)) {
                                imgOpponents[2].setVisibility(View.INVISIBLE);
                                textOpponentsID[2].setVisibility(View.INVISIBLE);
                            } else {
                                imgOpponents[2].setImageResource(R.drawable.davinci_2p);
                                imgOpponents[2].setVisibility(View.VISIBLE);
                                textOpponentsID[2].setText(player.p2);
                                textOpponentsID[2].setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    String tmp = " ";

                    if (!player.p2.equals(RoomItem.EMPTY_PLAYER)) {
                        tmp += "2P ";
                        tmp += player.p2;
                        tmp += " : ";
                        if (player.p2Ready.equals("true")) {
                            tmp += " READY    ";
                        } else {
                            tmp += "   -      ";
                        }
                    }
                    if (!player.p3.equals(RoomItem.EMPTY_PLAYER)) {
                        tmp += "3P ";
                        tmp += player.p3;
                        tmp += " : ";
                        if (player.p3Ready.equals("true")) {
                            tmp += " READY ";
                        } else {
                            tmp += "   -   ";
                        }
                    }

                    textGuide.setText(tmp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        logRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                davinciGameBoard = dataSnapshot.getValue(DavinciGameBoard.class);
                if(davinciGameBoard == null) return;
                textLeftBlackBlock.setText(davinciGameBoard.cntBlack);
                textLeftWhiteBlock.setText(davinciGameBoard.cntWhite);
                if(isGameStart.equals("true")) {
                    if ((userNum == 1 && davinciGameBoard.cnt1P.equals("0"))
                            || (userNum == 2 && davinciGameBoard.cnt2P.equals("0"))
                            || (userNum == 3 && davinciGameBoard.cnt3P.equals("0"))) {
                        switch (userNum){
                            case 1 :
                                davinciGameBoard.cnt1P = "-1";
                                break;
                            case 2 :
                                davinciGameBoard.cnt2P = "-1";
                                break;
                            case 3 :
                                davinciGameBoard.cnt3P = "-1";
                                break;
                        }
                        getResult(LOSE);
                        davinciGameBoard.cntPlayer = Integer.toString(Integer.parseInt(davinciGameBoard.cntPlayer) - 1);
                        logRef.updateChildren(davinciGameBoard.logToMap());
                    } else if (davinciGameBoard.cntPlayer.equals("1")) {
                        getResult(WIN);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        boardRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    int block = dataSnapshot.getValue(Integer.class);
                    int key = Integer.parseInt(dataSnapshot.getKey());
                    setBlockOwner(key, block);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    int block = dataSnapshot.getValue(Integer.class);
                    int key = Integer.parseInt(dataSnapshot.getKey());
                    setBlockOwner(key, block);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String turnString = dataSnapshot.getValue(String.class);
                if(turnString == null || turnString.equals("0")) return;
                int turn = Integer.parseInt(turnString);

                if(turn == userNum){
                    if(davinciGameBoard.cntBlack.equals("0") && davinciGameBoard.cntWhite.equals("0")){
                        guessBlock();
                        return;
                    }
                    isMyTurn = true;
                    textGuide.setText(R.string.player_turn);
                }
                else{
                    textGuide.setText(R.string.guessing_anyone);
                    textGuide.setText(turnString + textGuide.getText().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        whiteRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                whiteOrder[Integer.parseInt(dataSnapshot.getKey())] = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        blackRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                blackOrder[Integer.parseInt(dataSnapshot.getKey())] = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (userNum == RoomItem.MASTER_USER) {    // 방장일 때
            Map<String, Object> map = new HashMap<>();

            map.put("p1", id);
            map.put("p2", RoomItem.EMPTY_PLAYER);
            map.put("p3", RoomItem.EMPTY_PLAYER);
            map.put("p2Ready", "false");
            map.put("p3Ready", "false");

            playerRef.updateChildren(map);
            imgStartReady.setImageResource(R.drawable.start_game_btn);
            davinciGameBoard = new DavinciGameBoard();
            davinciGameBoard.initMaster();

            imgStartReady.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(player.p2.equals(RoomItem.EMPTY_PLAYER) && player.p3.equals(RoomItem.EMPTY_PLAYER))
                            &&
                            (
                                    (player.p2.equals(RoomItem.EMPTY_PLAYER) || player.p2Ready.equals("true"))
                                            &&
                                            (player.p3.equals(RoomItem.EMPTY_PLAYER) || player.p3Ready.equals("true"))
                            )) {   // 다 레디 되어야 시작
                        Map<String, Object> start = new HashMap<>();
                        start.put("isGameStart", "true");
                        boardRef.updateChildren(boardToMap());
                        shuffleBlock();
                        logRef.updateChildren(davinciGameBoard.logToMap());
                        gameRef.updateChildren(start);
                        nextTurn();
                    }
                }
            });
        }
        else { // 방장이 아닐 때
            Map<String, Object> map = new HashMap<>();
            switch (userNum) {
                case 2:
                    map.put("p2", id);
                    break;
                case 3:
                    map.put("p3", id);
                default:
                    break;
            }
            playerRef.updateChildren(map);

            imgStartReady.setImageResource(R.drawable.ready_game_btn);
            imgStartReady.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, Object> ready = new HashMap<>();
                    String playerString = "p" + userNum + "Ready";

                    if (userNum == 2) {
                        if (player.p2Ready.equals("true")) {
                            ready.put(playerString, "false");
                        } else {
                            ready.put(playerString, "true");
                        }
                    } else {
                        if (player.p3Ready.equals("true")) {
                            ready.put(playerString, "false");
                        } else {
                            ready.put(playerString, "true");
                        }
                    }
                    playerRef.updateChildren(ready);
                }
            });
        }

        if(userNum == RoomItem.MASTER_USER) {
            logRef.updateChildren(davinciGameBoard.logToMap());
        }

        imgLeftBlackBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyTurn && !davinciGameBoard.cntBlack.equals("0")){
                    drawBlock(BLACK, userNum);
                    isMyTurn = false;
                    guessBlock();
                }
            }
        });

        imgLeftWhiteBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyTurn && !davinciGameBoard.cntWhite.equals("0")){
                    drawBlock(WHITE, userNum);
                    isMyTurn = false;
                    guessBlock();
                }
            }
        });

        imgGuessRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guessNum = (guessNum + 1) % (MAX_BLOCK / 2);
                textGuessNum.setText(Integer.toString(guessNum));
            }
        });

        imgGuessLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guessNum = (guessNum + (MAX_BLOCK / 2) - 1) % (MAX_BLOCK / 2);
                textGuessNum.setText(Integer.toString(guessNum));
            }
        });

        imgPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgPass.setVisibility(View.INVISIBLE);
                imgGuess.setVisibility(View.INVISIBLE);
                nextTurn();
            }
        });

        imgGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgPass.setVisibility(View.INVISIBLE);
                imgGuess.setVisibility(View.INVISIBLE);
                guessBlock();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(userNum == RoomItem.MASTER_USER){
            if(isGameStart.equals("false")){
                gameRef.removeValue();
            }
        }
        else{
            if(!isDestroyCauseByMaster) {
                Map<String, Object> map = new HashMap<>();
                switch (userNum) {
                    case 2:
                        map.put("p2", RoomItem.EMPTY_PLAYER);
                        map.put("p2Ready", "false");
                        break;
                    case 3:
                        map.put("p3", RoomItem.EMPTY_PLAYER);
                        map.put("p3Ready", "false");
                    default:
                        break;
                }
                playerRef.updateChildren(map);

                map = new HashMap<>();
                String minusNumberOfPlayer = Integer.toString(Integer.parseInt(numberOfPlayer) - 1);
                map.put("numberOfPlayer", minusNumberOfPlayer);
                gameRef.updateChildren(map);
                finish();
            }
        }
        if(numberOfPlayer.equals("0")){
            gameRef.removeValue();
        }


        super.onDestroy();
    }

    public Map<String, Object> boardToMap(){
        Map<String, Object> map = new HashMap<>();
        for(int i = 0; i < MAX_BLOCK; i++){
            map.put(Integer.toString(i), blockOwner[i]);
        }
        return map;
    }

    public void setBlockOwner(int i, int val){
        blockOwner[i] = val;
        if(userNum == 1){
            switch (val){
                case UP_1P : imgMyBlock[i].setVisibility(View.VISIBLE);
                    break;
                case UP_2P : imgOpponentsBlock[1][i].setVisibility(View.VISIBLE);
                    break;
                case UP_3P : imgOpponentsBlock[2][i].setVisibility(View.VISIBLE);
                    break;
                case DOWN_1P : imgMyBlock[i].setImageResource(downBlockDrawable[i]);
                    break;
                case DOWN_2P : imgOpponentsBlock[1][i].setImageResource(downBlockDrawable[i]);
                    break;
                case DOWN_3P : imgOpponentsBlock[2][i].setImageResource(downBlockDrawable[i]);
                    break;
            }
        }
        else if(userNum == 2){
            switch (val){
                case UP_1P : imgOpponentsBlock[1][i].setVisibility(View.VISIBLE);
                    break;
                case UP_2P : imgMyBlock[i].setVisibility(View.VISIBLE);
                    break;
                case UP_3P : imgOpponentsBlock[2][i].setVisibility(View.VISIBLE);
                    break;
                case DOWN_1P : imgOpponentsBlock[1][i].setImageResource(downBlockDrawable[i]);
                    break;
                case DOWN_2P : imgMyBlock[i].setImageResource(downBlockDrawable[i]);
                    break;
                case DOWN_3P : imgOpponentsBlock[2][i].setImageResource(downBlockDrawable[i]);
                    break;
            }
        }
        else {
            switch (val){
                case UP_1P : imgOpponentsBlock[1][i].setVisibility(View.VISIBLE);
                    break;
                case UP_2P : imgOpponentsBlock[2][i].setVisibility(View.VISIBLE);
                    break;
                case UP_3P : imgMyBlock[i].setVisibility(View.VISIBLE);
                    break;
                case DOWN_1P : imgOpponentsBlock[1][i].setImageResource(downBlockDrawable[i]);
                    break;
                case DOWN_2P : imgOpponentsBlock[2][i].setImageResource(downBlockDrawable[i]);
                    break;
                case DOWN_3P : imgMyBlock[i].setImageResource(downBlockDrawable[i]);
                    break;
            }
        }
    }

    public void shuffleBlock(){
        for(int i = 0; i < MAX_BLOCK / 2; i++){
            blackOrder[i] = i;
            whiteOrder[i] = i;
        }
        Random random = new Random();

        for(int i = 0; i < 100; i++){
            int r = random.nextInt(MAX_BLOCK / 2);
            int tmp = blackOrder[0];
            blackOrder[0] = blackOrder[r];
            blackOrder[r] = tmp;
        }
        for(int i = 0; i < 100; i++){
            int r = random.nextInt(MAX_BLOCK / 2);
            int tmp = whiteOrder[0];
            whiteOrder[0] = whiteOrder[r];
            whiteOrder[r] = tmp;
        }

        drawBlock(BLACK, 1);
        drawBlock(BLACK, 1);
        drawBlock(WHITE, 1);
        drawBlock(WHITE, 1);

        if(!player.p2.equals(RoomItem.EMPTY_PLAYER)){
            drawBlock(BLACK, 2);
            drawBlock(BLACK, 2);
            drawBlock(WHITE, 2);
            drawBlock(WHITE, 2);
            davinciGameBoard.cntPlayer = Integer.toString(Integer.parseInt(davinciGameBoard.cntPlayer) + 1);
            logRef.updateChildren(davinciGameBoard.logToMap());
        }
        if(!player.p3.equals(RoomItem.EMPTY_PLAYER)){
            drawBlock(BLACK, 3);
            drawBlock(BLACK, 3);
            drawBlock(WHITE, 3);
            drawBlock(WHITE, 3);
            davinciGameBoard.cntPlayer = Integer.toString(Integer.parseInt(davinciGameBoard.cntPlayer) + 1);
            logRef.updateChildren(davinciGameBoard.logToMap());
        }
        orderToMap();
    }

    public void orderToMap(){
        Map<String, Object> whiteMap = new HashMap<>();
        Map<String, Object> blackMap = new HashMap<>();
        for(int i = 0; i < MAX_BLOCK / 2; i++){
            whiteMap.put(Integer.toString(i), whiteOrder[i]);
            blackMap.put(Integer.toString(i), blackOrder[i]);
        }
        whiteRef.updateChildren(whiteMap);
        blackRef.updateChildren(blackMap);
    }

    public void guessBlock(){
        textGuessNum.setText("0");
        guessNum = 0;
        layoutGuess.setVisibility(View.VISIBLE);
        textGuide.setText(R.string.guess_block);
        isGuessBlock = true;
    }

    public void nextTurn(){
        Hashtable<String, Object> nextTurn = new Hashtable<>();

        String turn;
        if(davinciGameBoard.turn.equals("1")){
            if(player.p2.equals(RoomItem.EMPTY_PLAYER) || davinciGameBoard.cnt2P.equals("-1")){
                turn = "3";
            }
            else{
                turn = "2";
            }
        }
        else if(davinciGameBoard.turn.equals("2")){
            if(player.p3.equals(RoomItem.EMPTY_PLAYER) || davinciGameBoard.cnt3P.equals("-1")){
                turn = "1";
            }
            else{
                turn = "3";
            }
        }
        else{
            if(davinciGameBoard.cnt1P.equals("-1")){
                turn = "2";
            }
            else{
                turn = "1";
            }
        }
        nextTurn.put("turn", turn);
        logRef.updateChildren(nextTurn);
    }

    public void drawBlock(int color, int player){
        switch (player){
            case 1 :
                davinciGameBoard.cnt1P = Integer.toString(Integer.parseInt(davinciGameBoard.cnt1P) + 1);
                break;
            case 2 :
                davinciGameBoard.cnt2P = Integer.toString(Integer.parseInt(davinciGameBoard.cnt2P) + 1);
                break;
            case 3 :
                davinciGameBoard.cnt3P = Integer.toString(Integer.parseInt(davinciGameBoard.cnt3P) + 1);
                break;
        }
        switch (color){
            case BLACK :
                if(!davinciGameBoard.cntBlack.equals("0")){
                    int i = Integer.parseInt(davinciGameBoard.cntBlack) - 1;
                    lastBlock = blackOrder[i] * 2;
                    Map<String, Object> board = new HashMap<>();
                    board.put(Integer.toString(lastBlock),  player);
                    davinciGameBoard.cntBlack = Integer.toString(i);
                    boardRef.updateChildren(board);
                }
                break;
            case WHITE :
                if(!davinciGameBoard.cntWhite.equals("0")){
                    int i = Integer.parseInt(davinciGameBoard.cntWhite) - 1;
                    lastBlock = whiteOrder[i] * 2 + 1;
                    Map<String, Object> board = new HashMap<>();
                    board.put(Integer.toString(lastBlock),  player);
                    davinciGameBoard.cntWhite = Integer.toString(i);
                    boardRef.updateChildren(board);
                }
                break;
        }
        logRef.updateChildren(davinciGameBoard.logToMap());
    }

    public void getResult(int result){
        if(isOver) return;
        isOver = true;
        dialogView = View.inflate(DavinciGameActivity.this, R.layout.dialog_result_davinci, null);
        dlg = new AlertDialog.Builder(DavinciGameActivity.this);

        final RelativeLayout relativeLayout = dialogView.findViewById(R.id.img_result_davinci);
        final ImageView imgExit = dialogView.findViewById(R.id.img_exit_davinci_game);

        dlg.setView(dialogView);
        dialog = dlg.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if(result == WIN){
            relativeLayout.setBackgroundResource(R.drawable.win_davinci);
        }
        imgExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                finish();
            }
        });
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 1800;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Window window = dialog.getWindow();
        window.setAttributes(lp);

        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if(isGameStart.equals("false")){
            super.onBackPressed();
        }
    }
}
