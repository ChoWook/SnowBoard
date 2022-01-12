package com.example.firebasetest;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoomItemAdapter extends RecyclerView.Adapter<RoomItemAdapter.ViewHolder>{

    FirebaseDatabase database;
    FirebaseUser user;
    String email, id;
    boolean isCorrect;
    private ArrayList<RoomItem> roomList;
    Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageLog, imageLock;
        TextView textTitle, textRoomMasterId, textNumOfPeople;
        ImageButton btnEnterRoom;
        DatabaseReference playerRef, gameRef;
        int emptyPlayer;

        ViewHolder(View itemView) {
            super(itemView);

            imageLog = itemView.findViewById(R.id.img_waiting_logo);
            imageLock = itemView.findViewById(R.id.img_room_lock);
            textTitle = itemView.findViewById(R.id.txt_room_title);
            textRoomMasterId = itemView.findViewById(R.id.txt_room_master);
            textNumOfPeople = itemView.findViewById(R.id.txt_people_num);
            btnEnterRoom = itemView.findViewById(R.id.imgbtn_room_enter);
        }
    }

    public RoomItemAdapter(){
        roomList  = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        if (user != null) {
            email = user.getEmail();
            String[] splits = email.split("@", 2);
            id = splits[0];
        }
    }

    @NonNull
    @Override
    public RoomItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_room, parent, false);
        RoomItemAdapter.ViewHolder viewHolder = new RoomItemAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final RoomItem roomItem = roomList.get(position);

        holder.textTitle.setText(roomItem.roomNumber + ". " + roomItem.title);
        holder.textRoomMasterId.setText(roomItem.roomMasterId);
        holder.textNumOfPeople.setText(roomItem.numberOfPlayer);

        switch (Integer.parseInt(roomList.get(position).gameType)) {
            case RoomItem.ICE_GAME:
                holder.imageLog.setImageResource(R.drawable.waiting_icegame);
                break;
            case RoomItem.DAVINCI_GAME:
                holder.imageLog.setImageResource(R.drawable.waiting_davinci_game);// 다빈치 로고
                break;
        }

        if (roomItem.isPassword.equals("true")) {
            holder.imageLock.setImageResource(R.drawable.waiting_lock);
        } else {
            holder.imageLock.setImageResource(R.drawable.waiting_unlock);
        }
        holder.playerRef = database.getReference(RoomItem.GAMES).child(roomList.get(holder.getAdapterPosition()).roomNumber).child(RoomItem.PLAYER);

        holder.playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Player player = dataSnapshot.getValue(Player.class);
                if (player == null) return;
                if (player.p2.equals(RoomItem.EMPTY_PLAYER)) {
                    holder.emptyPlayer = 2;
                } else {
                    holder.emptyPlayer = 3;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.btnEnterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCorrect = true;
                if (roomItem.numberOfPlayer.equals("3")) {   // 방이 꽉차있으면
                    return;
                }
                if (roomItem.isPassword.equals("true")) {   // 비밀번호 있을 때
                    isCorrect = false;
                    View dialogView = View.inflate(context, R.layout.dialog_password_input, null);
                    final AlertDialog.Builder dlg = new AlertDialog.Builder(context);

                    final EditText editRoomPassword = dialogView.findViewById(R.id.edit_input_room_pwd);
                    final ImageView imgBtnPasswordEnter = dialogView.findViewById(R.id.imgbtn_password_enter);

                    dlg.setView(dialogView);
                    final Dialog dialog = dlg.create();

                    imgBtnPasswordEnter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.w("PASSWORD", editRoomPassword.getText().toString() + " // " + roomItem.password);
                            if(editRoomPassword.getText().toString().equals(roomItem.password)){
                                isCorrect = true;
                                dialog.cancel();
                                enterRoom(roomItem, holder);
                            }
                            else{
                                Toast.makeText(context, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
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
                if(isCorrect){
                    enterRoom(roomItem, holder);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public void addItem(RoomItem item){
        if(item.isGameStart.equals("false")){
            roomList.add(item);
            notifyDataSetChanged();
        }
    }

    public ArrayList<RoomItem> getRoomList(){
        return roomList;
    }

    public void enterRoom(RoomItem roomItem, ViewHolder holder){
        Intent intent;
        switch (Integer.parseInt(roomItem.gameType)) {
            case RoomItem.ICE_GAME:
                intent = new Intent(context, IceGameActivity.class);
                break;
            case RoomItem.DAVINCI_GAME:
                intent = new Intent(context, DavinciGameActivity.class);
                break;
            default:
                intent = null;
        }

        intent.putExtra(RoomItem.USER_NUM, holder.emptyPlayer);
        intent.putExtra(RoomItem.ROOM_NUM, Integer.parseInt(roomItem.roomNumber));
        context.startActivity(intent);

        Map<String, Object> plusNumberOfPlayer = new HashMap<>();
        String value = Integer.toString(Integer.parseInt(roomItem.numberOfPlayer) + 1);
        plusNumberOfPlayer.put("numberOfPlayer", value);
        holder.gameRef = database.getReference().child(RoomItem.GAMES).child(roomItem.roomNumber);
        Log.w("ROOM_NUMBER", roomItem.roomNumber);
        holder.gameRef.updateChildren(plusNumberOfPlayer);
        notifyDataSetChanged();
    }
}
