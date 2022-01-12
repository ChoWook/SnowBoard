package com.example.firebasetest;

import java.util.HashMap;
import java.util.Map;

public class DavinciGameBoard {

    public String turn;
    public String cntBlack;
    public String cntWhite;
    public String cnt1P;
    public String cnt2P;
    public String cnt3P;
    public String cntPlayer;

    DavinciGameBoard(){}

    public void initMaster(){
        turn = "0";
        cntWhite = "13";
        cntBlack = "13";
        cnt1P = "0";
        cnt2P = "0";
        cnt3P = "0";
        cntPlayer = "1";
    }

    public Map<String, Object> logToMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("turn", turn);
        map.put("cntBlack", cntBlack);
        map.put("cntWhite", cntWhite);
        map.put("cnt1P", cnt1P);
        map.put("cnt2P", cnt2P);
        map.put("cnt3P", cnt3P);
        map.put("cntPlayer", cntPlayer);

        return map;
    }
}
