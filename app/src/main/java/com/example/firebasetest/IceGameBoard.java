package com.example.firebasetest;

import java.util.HashMap;
import java.util.Map;

public class IceGameBoard {

    public String cntWhite;
    public String cntBlue;
    public String turn;
    public String roulette;                    // PUSH_WHITE = "white"
    public String degree;

    IceGameBoard(){}

    public void initMaster(){
        cntWhite = "0";
        cntBlue = "0";
        turn = "0";
        roulette = IceGameActivity.PUSH_NONE;
        degree = "0";
    }

    public Map<String, Object> logToMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("cntWhite", cntWhite);
        map.put("cntBlue", cntBlue);
        map.put("turn", turn);
        map.put("roulette", roulette);
        map.put("degree", degree);
        return map;
    }
}
