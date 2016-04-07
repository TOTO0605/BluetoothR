package com.example.toto.bluetoothc;

import java.util.UUID;

/**
 * Created by TOTO on 2016.03.10..
 */
public interface Konstansok {

    public static int setKod=1; //beállítások küldése
    public static int beallitasF=1; //beállítások frissítése
    public static int mozdulatSorF=2;//mozdulatsor frissítése
    public static int allapot=1; //állapotfrissítés
    public static int bezar=2; // brzárás
    public static int kihez=3; // eszköz nevének frissítése
    public static int bluetoothKod=4; // bluetooth ellenörzése
    public static int asyncKod = 5;// aszinkron kapcsolat
    public static double time_out=8.0; //ser ver várakozása mpben

    public static final UUID SERVER_UUID = UUID
            .fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String SERVER_NAME = "BluetootServer";

}
