package com.example.firebasetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Hashtable;

public class HomeMenuActivity extends AppCompatActivity {

    // TODO 시연할때는 이메일이랑 비밀번호 직접 입력해야 함

    ImageButton BtnMakeRoom, BtnEnterRoom, BtnTutorial;
    int gameType;
    int index;
    String email, id;
    DatabaseReference infoIndexRef, gameRef;
    FirebaseDatabase database;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home_menu);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BtnMakeRoom = findViewById(R.id.btn_make_room);
        BtnEnterRoom = findViewById(R.id.btn_enter_room);
        BtnTutorial = findViewById(R.id.btn_tutorial);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        if (user != null) {
            email = user.getEmail();
            String[] splits = email.split("@", 2);
            id = splits[0];
        }

        BtnMakeRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    View dialogView = View.inflate(HomeMenuActivity.this, R.layout.dialog_make_room, null);
                    AlertDialog.Builder dlg = new AlertDialog.Builder(HomeMenuActivity.this);
                    gameType = 0;

                    final EditText editRoomTitle = dialogView.findViewById(R.id.edttxt_room_title);
                    final EditText editRoomPass = dialogView.findViewById(R.id.edit_room_pwd);
                    final ImageView imgIce = dialogView.findViewById(R.id.imgbtn_icegame);
                    final ImageView imgDavinci = dialogView.findViewById(R.id.imgbtn_davinci);
                    ImageView imgGenerate = dialogView.findViewById(R.id.imgbtn_password_enter);
                    dlg.setView(dialogView);

                    imgIce.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imgIce.setImageResource(R.drawable.generate_icegame);
                            imgDavinci.setImageResource(R.drawable.generate_davinci_dark);
                            gameType = RoomItem.ICE_GAME;
                        }
                    });

                    imgDavinci.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imgIce.setImageResource(R.drawable.generate_icegame_dark);
                            imgDavinci.setImageResource(R.drawable.generate_davinci);
                            gameType = RoomItem.DAVINCI_GAME;
                        }
                    });

                    final Dialog dialog = dlg.create();

                    infoIndexRef = database.getReference("Info").child("NewRoomIndex");
                    infoIndexRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try{
                                index = dataSnapshot.getValue(Integer.class);
                                Log.w("INDEX", Integer.toString(index));
                            }
                            catch (Exception e){
                                Log.w("ON_DATA_CHANGED", e.getMessage());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    imgGenerate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String title = editRoomTitle.getText().toString();
                            if(!(title.isEmpty() || title.equals(""))) {
                                if (gameType != 0) {
                                    Intent intent;

                                    gameRef = database.getReference(RoomItem.GAMES).child(Integer.toString(index));
                                    Hashtable<String, String> newRoom = new Hashtable<>();
                                    try {
                                        newRoom.put("gameType", Integer.toString(gameType));
                                        newRoom.put("numberOfPlayer", "1");
                                        newRoom.put("title", title);
                                        newRoom.put("roomMasterId", id);
                                        newRoom.put("isGameStart", "false");

                                        String pass = editRoomPass.getText().toString();
                                        if (pass.isEmpty() || pass.equals("")) {
                                            newRoom.put("isPassword", "false");
                                            newRoom.put("password", "");
                                        } else {
                                            newRoom.put("isPassword", "true");
                                            newRoom.put("password", pass);
                                        }
                                    } catch (Exception e) {
                                        Log.w("PUT", e.getMessage());
                                    }

                                    switch (gameType) {
                                        case RoomItem.ICE_GAME: // 아이스게임을 만들 때
                                            intent = new Intent(getApplicationContext(), IceGameActivity.class);
                                            intent.putExtra(RoomItem.USER_NUM, RoomItem.MASTER_USER);
                                            intent.putExtra(RoomItem.ROOM_NUM, index);
                                            gameRef.setValue(newRoom);
                                            startActivity(intent);
                                            dialog.cancel();
                                            break;
                                        case RoomItem.DAVINCI_GAME:
                                            intent = new Intent(getApplicationContext(), DavinciGameActivity.class);
                                            intent.putExtra(RoomItem.USER_NUM, RoomItem.MASTER_USER);
                                            intent.putExtra(RoomItem.ROOM_NUM, index);
                                            gameRef.setValue(newRoom);
                                            startActivity(intent);
                                            dialog.cancel();
                                            break;
                                        default:
                                            Toast.makeText(getApplicationContext(), "게임을 선택하세요", Toast.LENGTH_SHORT).show();
                                            break;
                                    }

                                    if(++index == 100){
                                        index = 1;
                                    }
                                    infoIndexRef.setValue(index);
                                } else{
                                    Toast.makeText(getApplicationContext(), "게임을 선택하세요", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(getApplicationContext(), "방 제목을 입력하세요", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
                catch (Exception e){
                    Log.w("MAKE_ROOM", e.getMessage());
                }
            }
        });
        BtnEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeMenuActivity.this, RoomListActivity.class);
                startActivity(intent);
            }
        });
        BtnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeMenuActivity.this, TutorialActivity.class);
                startActivity(intent);
            }
        });
    }
}
