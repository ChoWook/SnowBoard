package com.example.firebasetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoomListActivity extends AppCompatActivity {

    RecyclerView roomRecyclerView;
    RoomItemAdapter roomItemAdapter;
    String email, id;
    SwipeRefreshLayout swipeRefreshLayout;

    DatabaseReference gameRef;
    FirebaseDatabase database;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_room_list);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        if (user != null) {
            email = user.getEmail();
            String[] splits = email.split("@", 2);
            id = splits[0];
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        roomRecyclerView = findViewById(R.id.list_room);

        roomRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        roomItemAdapter = new RoomItemAdapter();
        roomRecyclerView.setAdapter(roomItemAdapter);


        gameRef = database.getReference(RoomItem.GAMES);

        gameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RoomItem roomItem = dataSnapshot.getValue(RoomItem.class);
                if(roomItem == null) return;
                roomItem.roomNumber = dataSnapshot.getKey();
                roomItemAdapter.addItem(roomItem);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RoomItem roomItem = dataSnapshot.getValue(RoomItem.class);

                if(roomItem.isGameStart.equals("true")){    // 게임 시작했으면 리스트에서 제거
                    onChildRemoved(dataSnapshot);
                }

                roomItem.roomNumber = dataSnapshot.getKey();
                for(int i = 0; i < roomItemAdapter.getItemCount(); i++){
                    if (roomItemAdapter.getRoomList().get(i).roomNumber.equals(roomItem.roomNumber)){
                        roomItemAdapter.getRoomList().set(i, roomItem);
                        roomItemAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                RoomItem roomItem = dataSnapshot.getValue(RoomItem.class);

                roomItem.roomNumber = dataSnapshot.getKey();
                for(int i = 0; i < roomItemAdapter.getItemCount(); i++){
                    if (roomItemAdapter.getRoomList().get(i).roomNumber.equals(roomItem.roomNumber)){
                        roomItemAdapter.getRoomList().remove(i);
                        roomItemAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
