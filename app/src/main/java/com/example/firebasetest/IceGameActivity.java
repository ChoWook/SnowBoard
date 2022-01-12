package com.example.firebasetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.Map;
import java.util.Random;

public class IceGameActivity extends AppCompatActivity {
    public static final int R_MAX = 11;
    public static final int C_MAX = 8;
    public static final int WHITE = 2;
    public static final int BLUE = 3;
    public static final int DROP = 4;
    public static final int PENGUIN_BLOCK = 43;
    public static final String PUSH_WHITE = "white";
    public static final String PUSH_BLUE = "blue";
    public static final String PUSH_ALL = "all";
    public static final String PUSH_NONE = "none";

    int userNum, roomNum;
    RelativeLayout[] imgPlayer;
    ImageView[] blocksImg;
    ImageView[] imgArrow;
    ImageView[] imgTurnWhat;
    ImageView imgStartReadyGame, imgPenguin;
    TextView[] textID;
    LinearLayout iceGameLayout;

    DatabaseReference gameRef, playerRef, logRef, boardRef;
    FirebaseDatabase database;
    FirebaseUser user;
    String email, id, numberOfPlayer, isGameStart;
    boolean isDestroyCauseByMaster, isClicked, isOver;
    Player player;
    IceGameBoard iceGameBoard;
    int degree;

    // 다이얼로그
    View dialogView;
    AlertDialog.Builder dlg;
    ImageView roulettePanel, rouletteStart;
    Dialog dialog;


    int[] blocksInt ={
            0, 0, 0, 0, 0, 0, 0, 0,     // 0 = 테두리
            0, 0, 0, 1, 0, 0, 0, 0,    // 1 = 블럭이 가능한 공간
            0, 0, 1, 1, 1, 1, 0, 0,     // 2 = 흰색
            0, 1, 1, 1, 1, 1, 0, 0,    // 3 = 파란색
            0, 1, 1, 1, 1, 1, 1, 0,     // 4 = 부서진 공간
            0, 1, 1, 1, 1, 1, 0, 0,
            0, 1, 1, 1, 1, 1, 1, 0,
            0, 1, 1, 1, 1, 1, 0, 0,
            0, 0, 1, 1, 1, 1, 0, 0,
            0, 0, 0, 1, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
    };

    int[] blocksId ={
            0,
            R.id.img_ice_block_01,            R.id.img_ice_block_02,            R.id.img_ice_block_03,
            R.id.img_ice_block_04,            R.id.img_ice_block_05,            R.id.img_ice_block_06,
            R.id.img_ice_block_07,            R.id.img_ice_block_08,            R.id.img_ice_block_09,
            R.id.img_ice_block_10,            R.id.img_ice_block_11,            R.id.img_ice_block_12,
            R.id.img_ice_block_13,            R.id.img_ice_block_14,            R.id.img_ice_block_15,
            R.id.img_ice_block_16,            R.id.img_ice_block_17,            R.id.img_ice_block_18,
            R.id.img_ice_block_19,            R.id.img_ice_block_20,            R.id.img_ice_block_21,
            R.id.img_ice_block_22,            R.id.img_ice_block_23,            R.id.img_ice_block_24,
            R.id.img_ice_block_25,            R.id.img_ice_block_26,            R.id.img_ice_block_27,
            R.id.img_ice_block_28,            R.id.img_ice_block_29,            R.id.img_ice_block_30,
            R.id.img_ice_block_31,            R.id.img_ice_block_32,            R.id.img_ice_block_33,
            R.id.img_ice_block_34,            R.id.img_ice_block_35,            R.id.img_ice_block_36,
            R.id.img_ice_block_37
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_ice_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        userNum = intent.getIntExtra(RoomItem.USER_NUM, 0);
        roomNum = intent.getIntExtra(RoomItem.ROOM_NUM, 0);

        blocksImg = new ImageView[R_MAX * C_MAX];
        imgPlayer = new RelativeLayout[4];
        imgArrow = new ImageView[4];
        imgTurnWhat = new ImageView[4];
        textID = new TextView[4];
        imgPlayer[1] = findViewById(R.id.img_ice_player_1p);
        imgPlayer[2] = findViewById(R.id.img_ice_player_2p);
        imgPlayer[3] = findViewById(R.id.img_ice_player_3p);
        imgArrow[1] = findViewById(R.id.img_ice_turn_arrow_1p);
        imgArrow[2] = findViewById(R.id.img_ice_turn_arrow_2p);
        imgArrow[3] = findViewById(R.id.img_ice_turn_arrow_3p);
        imgTurnWhat[1] = findViewById(R.id.img_turn_what_1p);
        imgTurnWhat[2] = findViewById(R.id.img_turn_what_2p);
        imgTurnWhat[3] = findViewById(R.id.img_turn_what_3p);
        textID[1] = findViewById(R.id.txt_ice_player_1p_name);
        textID[2] = findViewById(R.id.txt_ice_player_2p_name);
        textID[3] = findViewById(R.id.txt_ice_player_3p_name);
        imgStartReadyGame = findViewById(R.id.img_ice_start_ready);
        imgPenguin = findViewById(R.id.img_ice_penguin);
        iceGameLayout = findViewById(R.id.layout_ice_game);
        isDestroyCauseByMaster = false;
        isClicked = false;
        isOver = false;

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference(RoomItem.GAMES).child(Integer.toString(roomNum));
        playerRef = gameRef.child(RoomItem.PLAYER);
        logRef = gameRef.child(RoomItem.LOG);
        boardRef = logRef.child(RoomItem.BOARD);

        if (user != null) {
            email = user.getEmail();
            String[] splits = email.split("@", 2);
            id = splits[0];
        }

        if(userNum == 0 || roomNum == 0){
            Log.w("ICE_GAME", RoomItem.USER_NUM + "== 0 || " + RoomItem.ROOM_NUM + "== 0");
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
                if(userNum != RoomItem.MASTER_USER) {
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
                if(isGameStart != null && isGameStart.equals("true")){
                    imgArrow[1].setVisibility(View.VISIBLE);
                    imgArrow[2].setVisibility(View.INVISIBLE);
                    imgArrow[3].setVisibility(View.INVISIBLE);
                    imgStartReadyGame.setVisibility(View.GONE);
                    imgPenguin.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                player = dataSnapshot.getValue(Player.class);
                if(player == null) return;
                for(int index = 1; index <=3; index++){
                    String id = null;
                    switch (index){
                        case 1 : id = player.p1;
                        break;
                        case 2 : id = player.p2;
                        break;
                        case 3 : id = player.p3;
                    }

                    if(id.equals(RoomItem.EMPTY_PLAYER)){            // 비어있는 자리일 때
                        imgPlayer[index].setVisibility(View.INVISIBLE);
                        imgArrow[index].setVisibility(View.INVISIBLE);
                    }
                    else{
                        imgPlayer[index].setVisibility(View.VISIBLE);
                        textID[index].setText(id);
                    }
                }
                if(isGameStart.equals("false")){
                    if(player.p2Ready.equals("true")){
                        imgArrow[2].setVisibility(View.VISIBLE);
                    }
                    else{
                        imgArrow[2].setVisibility(View.INVISIBLE);
                    }
                    if(player.p3Ready.equals("true")){
                        imgArrow[3].setVisibility(View.VISIBLE);
                    }
                    else{
                        imgArrow[3].setVisibility(View.INVISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        logRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                iceGameBoard = dataSnapshot.getValue(IceGameBoard.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dialogView = View.inflate(IceGameActivity.this, R.layout.dialog_roulette, null);
        dlg = new AlertDialog.Builder(IceGameActivity.this);

        roulettePanel = dialogView.findViewById(R.id.img_roulette_panel);
        rouletteStart = dialogView.findViewById(R.id.imgbtn_roulette_start);

        dlg.setView(dialogView);
        dialog = dlg.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        rouletteStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random random = new Random();
                int degree = random.nextInt(360) + 360 * 5;

                Map<String, Object> randomDegree = new HashMap<>();
                randomDegree.put("degree", Integer.toString(degree));
                logRef.updateChildren(randomDegree);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        logRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String turnString = dataSnapshot.getValue(String.class);
                if(turnString == null || turnString.equals("0")) return;
                int turn = Integer.parseInt(turnString);

                if(turn == userNum){
                    rouletteStart.setVisibility(View.VISIBLE);
                    isClicked = true;
                }
                else{
                    rouletteStart.setVisibility(View.INVISIBLE);
                }
                for(int i = 1; i <= 3; i++){
                    if(i == turn){
                        imgArrow[i].setVisibility(View.VISIBLE);
                    }
                    else
                        imgArrow[i].setVisibility(View.INVISIBLE);
                }


                SystemClock.sleep(1000);
                dialog.show();
                for(int i = 1; i <= 3; i++){
                    imgTurnWhat[i].setVisibility(View.INVISIBLE);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = 950;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                Window window = dialog.getWindow();
                window.setAttributes(lp);

                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logRef.child("degree").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // we calculate random angle for rotation of our wheel
                String degreeString = dataSnapshot.getValue(String.class);
                if(degreeString == null || degreeString.equals("0") || !dialog.isShowing()) return;
                degree = Integer.parseInt(degreeString);

                // rotation effect on the center of the wheel
                RotateAnimation rotateAnim = new RotateAnimation(0, degree,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                rotateAnim.setDuration(3600);
                rotateAnim.setFillAfter(true);
                rotateAnim.setInterpolator(new DecelerateInterpolator());
                rotateAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // we empty the result text view when the animation start
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // we display the correct sector pointed by the triangle at the end of the rotate animation
                        SystemClock.sleep(2000);
                        Map<String, Object> putDegree = new HashMap<>();
                        degree %= 360;
                        if(degree < 90){
                            putDegree.put("turn", nextTurn());
                        }
                        else if(degree < 180){
                            putDegree.put("roulette", PUSH_WHITE);
                            imgTurnWhat[Integer.parseInt(iceGameBoard.turn)].setImageResource(R.drawable.roulette_white);
                        }
                        else if(degree < 270){
                            putDegree.put("roulette", PUSH_ALL);
                            imgTurnWhat[Integer.parseInt(iceGameBoard.turn)].setImageResource(R.drawable.roulette_all);
                        }
                        else {
                            putDegree.put("roulette", PUSH_BLUE);
                            imgTurnWhat[Integer.parseInt(iceGameBoard.turn)].setImageResource(R.drawable.roulette_blue);
                        }
                        dialog.cancel();

                        imgTurnWhat[Integer.parseInt(iceGameBoard.turn)].setVisibility(View.VISIBLE);

                        logRef.updateChildren(putDegree);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                // we start the animation
                roulettePanel.startAnimation(rotateAnim);
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
                    blocksInt[key] = block;
                    switch (block){
                        case WHITE : blocksImg[key].setImageResource(R.drawable.icegame_white_ice);
                            break;
                        case BLUE : blocksImg[key].setImageResource(R.drawable.icegame_blue_ice);
                            break;
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    int block = dataSnapshot.getValue(Integer.class);
                    int key = Integer.parseInt(dataSnapshot.getKey());
                    blocksInt[key] = block;
                    if(block == DROP){
                        blocksImg[key].setImageResource(R.drawable.icegame_black_hole);
                        if(key == PENGUIN_BLOCK){
                            SystemClock.sleep(1000);
                            dropPenguin();
                        }
                    }
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

        if(userNum == RoomItem.MASTER_USER){    // 방장일 때
            Map<String, Object> map = new HashMap<>();

            map.put("p1", id);
            map.put("p2", RoomItem.EMPTY_PLAYER);
            map.put("p3", RoomItem.EMPTY_PLAYER);
            map.put("p2Ready", "false");
            map.put("p3Ready", "false");

            playerRef.updateChildren(map);
            imgStartReadyGame.setImageResource(R.drawable.start_game_btn);
            iceGameBoard = new IceGameBoard();
            iceGameBoard.initMaster();

            imgStartReadyGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!(player.p2.equals(RoomItem.EMPTY_PLAYER) && player.p3.equals(RoomItem.EMPTY_PLAYER))
                            &&
                            (
                                    (player.p2.equals(RoomItem.EMPTY_PLAYER) || player.p2Ready.equals("true"))
                                    &&
                                    (player.p3.equals(RoomItem.EMPTY_PLAYER) || player.p3Ready.equals("true"))
                            )){   // 다 레디 되어야 시작
                        Map<String, Object> start = new HashMap<>();
                        start.put("isGameStart", "true");
                        Map<String, Object> turn = new HashMap<>();
                        turn.put("turn", "1");
                        logRef.updateChildren(turn);
                        gameRef.updateChildren(start);
                        boardRef.updateChildren(boardToMap());
                    }
                }
            });
        }
        else { // 방장이 아닐 때
            Map<String, Object> map = new HashMap<>();
            switch (userNum){
                case 2 : map.put("p2", id);
                break;
                case 3 : map.put("p3", id);
                default: break;
            }
            playerRef.updateChildren(map);
            imgStartReadyGame.setImageResource(R.drawable.ready_game_btn);
            imgStartReadyGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String, Object> ready = new HashMap<>();
                    String playerString = "p" + userNum + "Ready";

                    if(userNum == 2){
                        if (player.p2Ready.equals("true")){
                            ready.put(playerString, "false");
                        }
                        else{
                            ready.put(playerString, "true");
                        }
                    }
                    else{
                        if (player.p3Ready.equals("true")){
                            ready.put(playerString, "false");
                        }
                        else{
                            ready.put(playerString, "true");
                        }
                    }
                    playerRef.updateChildren(ready);
                }
            });
        }
        referenceImg();
        if(userNum == RoomItem.MASTER_USER){
            logRef.updateChildren(iceGameBoard.logToMap());
        }
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

    public void referenceImg(){
        int pos = 1;

        for (int i = 0; i < R_MAX * C_MAX; i++) {
            final int _i = i;
            if (blocksInt[i] == 1 || blocksInt[i] == WHITE || blocksInt[i] == BLUE) {
                blocksImg[i] = findViewById(blocksId[pos++]);
                if(userNum == RoomItem.MASTER_USER) {
                    blocksInt[i] = generateRandomBlock();
                    if (blocksInt[i] == WHITE) {
                        blocksImg[i].setImageResource(R.drawable.icegame_white_ice);
                        iceGameBoard.cntWhite = (Integer.toString(Integer.parseInt(iceGameBoard.cntWhite) + 1));
                    } else {
                        blocksImg[i].setImageResource(R.drawable.icegame_blue_ice);
                        iceGameBoard.cntBlue = (Integer.toString(Integer.parseInt(iceGameBoard.cntBlue) + 1));
                    }
                }
                blocksImg[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.w("CLICKED_I", Integer.toString(_i));
                        Log.w("TURN", iceGameBoard.turn);
                        if(iceGameBoard.turn.equals(Integer.toString(userNum))){
                            if(isClicked) {
                                if ((blocksInt[_i] == WHITE && (iceGameBoard.roulette.equals(PUSH_WHITE) || iceGameBoard.roulette.equals(PUSH_ALL)))
                                        ||
                                        (blocksInt[_i] == BLUE && (iceGameBoard.roulette.equals(PUSH_BLUE) || iceGameBoard.roulette.equals(PUSH_ALL)))) {
                                    isClicked = false;
                                    dropBlock(_i);
                                    Map<String, Object> nextTurn = new HashMap<>();
                                    nextTurn.put("turn", nextTurn());
                                    logRef.updateChildren(nextTurn);
                                }
                            }
                        }
                    }
                });
            }
        }

    }

    private int generateRandomBlock(){
        Random random = new Random();
        return random.nextInt(2) + WHITE;       // 2 ~ 3 난수 생성
    }

    public void dropBlock(int i){
        Map<String, Object> cnt = new HashMap<>();
        if(blocksInt[i] == WHITE) {
            iceGameBoard.cntWhite = Integer.toString(Integer.parseInt(iceGameBoard.cntWhite) - 1);
            cnt.put("cntWhite", iceGameBoard.cntWhite);
        }
        else {
            iceGameBoard.cntBlue = Integer.toString(Integer.parseInt(iceGameBoard.cntBlue) - 1);
            cnt.put("cntBlue", iceGameBoard.cntBlue);
        }
        logRef.updateChildren(cnt);

        blocksInt[i] = DROP;
        Map<String, Object> drop = new HashMap<>();
        drop.put(Integer.toString(i), DROP);
        boardRef.updateChildren(drop);

        if(i == PENGUIN_BLOCK){
            dropPenguin();
            return;
        }

        if((i / C_MAX) % 2 == 0){   // 왼쪽으로 나와있을 떄
            checkBlock(i - C_MAX - 1);
            checkBlock(i + C_MAX  - 1);
        }
        else{
            checkBlock(i - C_MAX + 1);
            checkBlock(i + C_MAX + 1);
        }
        checkBlock(i - C_MAX);
        checkBlock(i + C_MAX);
        checkBlock(i - 1);
        checkBlock(i + 1);
    }

    public void checkBlock(int i){
        if(blocksInt[i] == WHITE || blocksInt[i] == BLUE){
            boolean isDrop = true;
            if((i / C_MAX) % 2 == 0){
                // 2개로 버티는 경우
                if(blocksInt[i - C_MAX] != DROP && blocksInt[i + C_MAX - 1] != DROP) {
                    isDrop = false;
                }
                else if(blocksInt[i - C_MAX - 1] != DROP && blocksInt[i + C_MAX] != DROP) {
                    isDrop = false;
                }
                else if(blocksInt[i - 1] != DROP && blocksInt[i + 1] != DROP) {
                    isDrop = false;
                }
                // 3개로 버티는 경우
                else if(blocksInt[i - C_MAX] != DROP && blocksInt[i - 1] != DROP && blocksInt[i + C_MAX] != DROP) {
                    isDrop = false;
                }
                else if(blocksInt[i - C_MAX - 1] != DROP && blocksInt[i + 1] != DROP && blocksInt[i + C_MAX - 1] != DROP) {
                    isDrop = false;
                }
            }
            else{
                // 2개로 버티는 경우
                if(blocksInt[i - C_MAX] != DROP && blocksInt[i + C_MAX + 1] != DROP) {
                    isDrop = false;
                }
                else if(blocksInt[i - C_MAX + 1] != DROP && blocksInt[i + C_MAX] != DROP) {
                    isDrop = false;
                }
                else if(blocksInt[i - 1] != DROP && blocksInt[i + 1] != DROP) {
                    isDrop = false;
                }
                // 3개로 버티는 경우
                else if(blocksInt[i - C_MAX] != DROP && blocksInt[i + 1] != DROP && blocksInt[i + C_MAX] != DROP) {
                    isDrop = false;
                }
                else if(blocksInt[i - C_MAX + 1] != DROP && blocksInt[i - 1] != DROP && blocksInt[i + C_MAX + 1] != DROP) {
                    isDrop = false;
                }
            }

            if(isDrop) dropBlock(i);
        }
    }

    public Map<String, Object> boardToMap(){
        Map<String, Object> map = new HashMap<>();
        for(int i = 0; i < C_MAX * R_MAX; i++){
            map.put(Integer.toString(i), blocksInt[i]);
        }
        return map;
    }

    public String nextTurn(){
        String turn;
        if(iceGameBoard.turn.equals("1")){
            if(player.p2.equals(RoomItem.EMPTY_PLAYER)){
                turn = "3";
            }
            else{
                turn = "2";
            }
        }
        else if(iceGameBoard.turn.equals("2")){
            if(player.p3.equals(RoomItem.EMPTY_PLAYER)){
                turn = "1";
            }
            else{
                turn = "3";
            }
        }
        else{
            turn = "1";
        }

        return turn;
    }

    public void dropPenguin(){
        blocksImg[PENGUIN_BLOCK].setImageResource(R.drawable.icegame_black_hole);
        SystemClock.sleep(300);
        imgPenguin.setImageResource(R.drawable.icegame_drop_penguin);

        SystemClock.sleep(1000);

        getResult();
    }

    public void getResult(){
        if(isOver) return;
        isOver = true;
        dialogView = View.inflate(IceGameActivity.this, R.layout.dialog_result_ice_game, null);
        dlg = new AlertDialog.Builder(IceGameActivity.this);

        final RelativeLayout relativeLayout = dialogView.findViewById(R.id.img_result_ice);
        final ImageView imgExit = dialogView.findViewById(R.id.img_exit_ice_game);

        dlg.setView(dialogView);
        dialog = dlg.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if(!iceGameBoard.turn.equals(Integer.toString(userNum))){
            relativeLayout.setBackgroundResource(R.drawable.win_ice);
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
        lp.width = 950;
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
