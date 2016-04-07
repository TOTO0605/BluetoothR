package com.example.toto.bluetoothc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Konstansok.allapot:
                    String[] teszt=msg.getData().getStringArray(Integer.toString(Konstansok.allapot));
                    allapotValt(teszt[0], teszt[1]);

                    break;
                case Konstansok.bezar:{
                    bezar();
                    Toast.makeText(getBaseContext(), R.string.megszakadt, Toast.LENGTH_SHORT).show();
                }
                    break;
                case Konstansok.kihez:
                    kihez(msg.getData().getString(Integer.toString(Konstansok.kihez)));
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"HOPPÁ", Toast.LENGTH_LONG).show();

            }
        }
    };

    private Button bont;
    private Button eszkozok;
    private Button kapcsol;
    private Button beallitasok;
    private Button bezar;
    private Button elore;
    private Button hatra;
    private Button balra;
    private Button jobbra;
    private Button sorozat;
    private RavitelG ravitelG; //érintés érézkelés
    private String mozogE;
    private String mozogH;
    private String mozogB;
    private String mozogJ;
    private String mozdulatsor="";
    private boolean sincron;

    private TextView allapot;
    private TextView eszkoz;

    private ServerThread server =null; //szinkron kapcsolat
    private AsyncServerThread async = null;// aszinkron kapcsolat
    private KuldesThread kuldes = null; // adatküldés
    private BluetoothAdapter bluetoothAdapter = null;
    private String eszkozNev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //beállítások betöltése
        try {
            beallitas();
            Log.d("Beállítások_betöltése1", "Betöltve");
            Toast.makeText(getBaseContext(), R.string.betoltve, Toast.LENGTH_SHORT).show();

        } catch (Exception e1) {
            Log.e("Beállítások_betöltése1","Hiba:" +e1.getMessage());
            try {
                //Most történt elsőnek indítás
                File sajat=new File(Environment.getExternalStorageDirectory(),"/BluetoothV/beallitasok.txt");
                BufferedWriter out = new BufferedWriter(new FileWriter(sajat.getAbsolutePath(), true));

                out.write("10;10;90;90;AS");
                out.close();


                beallitas();
                Toast.makeText(getBaseContext(), R.string.alapbeallitas, Toast.LENGTH_SHORT).show();
                Log.d("Beállítások_betöltése1", "Befejezve");

            } catch (Exception e2) {

                Toast.makeText(getBaseContext(),R.string.alapbeallitasHiba , Toast.LENGTH_SHORT).show();
                Toast.makeText(getBaseContext(), R.string.leall, Toast.LENGTH_SHORT).show();
                Log.e("Beállítások_betöltése2","Hiba: " +e2.getMessage());
                finish();

            }
        }
        try {
            mozdulatsorBe();
            Toast.makeText(getBaseContext(), R.string.mozdulatsorBe, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d("Mozdulatsor_beolvasása","Hiba: " +e.getMessage());
        }

        this.allapot = (TextView) findViewById(R.id.allapotsor);
        this.eszkoz = (TextView) findViewById(R.id.eszkoz);
        this.eszkozok = (Button) findViewById(R.id.eszkozok);
        this.beallitasok = (Button) findViewById(R.id.beallit);
        this.bezar = (Button) findViewById(R.id.bezar);
        this.elore = (Button) findViewById(R.id.elore);
        this.hatra = (Button) findViewById(R.id.hatra);
        this.jobbra = (Button) findViewById(R.id.jobbra);
        this.balra = (Button) findViewById(R.id.balra);
        this.sorozat = (Button) findViewById(R.id.sorozat);
        this.kapcsol = (Button) findViewById(R.id.kapcsolat);
        this.bont = (Button) findViewById(R.id.bont);

        ravitelG = new RavitelG();
        this.beallitasok.setOnTouchListener(ravitelG);
        this.bezar.setOnTouchListener(ravitelG);
        this.kapcsol.setOnTouchListener(ravitelG);
        this.bont.setOnTouchListener(ravitelG);
        this.eszkozok.setOnTouchListener(ravitelG);
        this.elore.setOnTouchListener(ravitelG);
        this.hatra.setOnTouchListener(ravitelG);
        this.balra.setOnTouchListener(ravitelG);
        this.jobbra.setOnTouchListener(ravitelG);
        this.sorozat.setOnTouchListener(ravitelG);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        // kapcsolat bontása
        this.bont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!allapot.getText().equals(getResources().getString(R.string.nincs))) {

                    bezar();

                }
            }
        });
        //kapcsolódás
        this.kapcsol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sincron) {

                    if (!(allapot.getText().equals(getResources().getString(R.string.fojamatban)) || allapot.getText().equals(getResources().getString(R.string.aktiv)))) {
                        bluetoothAdapter.cancelDiscovery();
                        server = new ServerThread();
                        server.start();
                    } else {
                        Toast.makeText(getBaseContext(), R.string.marVan, Toast.LENGTH_SHORT).show();

                    }
                } else {

                    Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
                    if (pairedDevice.size() > 0) {

                        if (!(allapot.getText().equals(getResources().getString(R.string.fojamatban)) || allapot.getText().equals(getResources().getString(R.string.aktiv)))) {

                            Intent inent = new Intent(MainActivity.this, AsyncActivity.class);
                            startActivityForResult(inent, Konstansok.asyncKod);

                        } else {
                            Toast.makeText(getBaseContext(), R.string.marVan, Toast.LENGTH_SHORT).show();

                        }
                    } else {

                        Toast.makeText(getBaseContext(), R.string.nincsP, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // kapcsoaltok ablak megnyitása
        this.eszkozok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!(allapot.getText().equals(getResources().getString(R.string.fojamatban)) || allapot.getText().equals(getResources().getString(R.string.aktiv)))) {

                    Intent inent = new Intent(MainActivity.this, ActivityConn.class);
                    // calling an activity using <intent-filter> action name
                    //  Intent inent = new Intent("com.hmkcode.android.ANOTHER_ACTIVITY");
                    startActivity(inent);
                } else {
                    Toast.makeText(getBaseContext(), R.string.marVan, Toast.LENGTH_SHORT).show();

                }

            }

        });
        //beállítások ablak megnyitása
        this.beallitasok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inent = new Intent(MainActivity.this, SettActivity.class);
                startActivityForResult(inent, Konstansok.setKod);

            }
        });
        //bezárás
        this.bezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        this.elore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(kuldes!=null) {

                    kuldes.kuld(mozogE.getBytes());
                }
                else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.nincs), Toast.LENGTH_SHORT).show();

                }

            }
        });
        this.hatra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(kuldes!=null) {

                    kuldes.kuld(mozogH.getBytes());

                }
                else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.nincs), Toast.LENGTH_SHORT).show();

                }

            }
        });
        this.balra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(kuldes!=null) {

                    kuldes.kuld(mozogB.getBytes());

                }
                else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.nincs), Toast.LENGTH_SHORT).show();

                }

            }
        });
        this.jobbra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(kuldes!=null) {

                    kuldes.kuld(mozogJ.getBytes());

                }
                else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.nincs), Toast.LENGTH_SHORT).show();
                }

            }
        });

        this.sorozat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(kuldes!=null){

                    if(!mozdulatsor.equals("")){

                        String[] mozdulatok=mozdulatsor.split(";");
                        for(int i=0;i<mozdulatok.length;i++){

                            kuldes.kuld(mozdulatok[i].getBytes());
                            try {
                                kuldes.sleep(500);//késleltetés 0,5 mp-cel
                            } catch (InterruptedException e) {
                                Log.e("Várakozás","Hiba: " +e.getMessage());
                            }
                        }
                    }
                    else {
                        Toast.makeText(getBaseContext(), R.string.nincsM, Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.nincs), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

    }

    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //bluetooth ellenörzése
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,Konstansok.bluetoothKod);
            // Otherwise, setup the chat session
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Konstansok.setKod){ //beállítások frissítése
            if(resultCode == Konstansok.beallitasF){
                Boolean a;
                a = data.getBooleanExtra("frissit",false);
                if(a){
                    try {
                        beallitas();
                        Toast.makeText(getBaseContext(), R.string.frissitB, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), R.string.frissitBhiba, Toast.LENGTH_SHORT).show();
                        Log.e("Beállítások_frissítése","Hiba: " +e.getMessage());
                    }

                }
            }
            if(resultCode == Konstansok.mozdulatSorF){ ///mozdulatsor frissítése
                Boolean a;
                a = data.getBooleanExtra("frissit",false);
                if(a){
                    try {
                        mozdulatsorBe();
                        Toast.makeText(getBaseContext(), R.string.frissitM, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), R.string.frissitMhiba, Toast.LENGTH_SHORT).show();
                        Log.e("Mozdulatsor_frissítése","Hiba: " +e.getMessage());
                    }

                }
            }
        }
        else if(requestCode == Konstansok.bluetoothKod && !(resultCode == RESULT_OK)){ // bluetooth ellenörzése

            Toast.makeText(getApplicationContext(), R.string.bluetoothNincs, Toast.LENGTH_LONG).show();
            finish();

        }
        else if(requestCode == Konstansok.asyncKod && resultCode == RESULT_OK){ //aszinkron kapcsolódás

            bluetoothAdapter.cancelDiscovery();
            eszkozNev=data.getStringExtra("eszkozNev");
            async = new AsyncServerThread(bluetoothAdapter.getRemoteDevice(eszkozNev));
            async.start();

        }
    }

    public void onDestroy() { //ablak bázáródás
        super.onDestroy();
      bezar();
    }

    private class AsyncServerThread extends Thread{ //aszinkron szál

        private final BluetoothSocket mmSocket;

        public AsyncServerThread(BluetoothDevice device){
            BluetoothSocket tmp = null;


            try {

                Log.d("Async1", "Socket inicializálása");
                tmp = device.createRfcommSocketToServiceRecord(Konstansok.SERVER_UUID);

            } catch (IOException e) {
                Log.e("Async1",e.getMessage());
            }
            mmSocket = tmp;
        }

        public void run() {

            bluetoothAdapter.cancelDiscovery();
            try {

                Log.d("Async2", "Kapcsolódás...");

                Message msg = mHandler.obtainMessage(Konstansok.allapot);
                Bundle bundle = new Bundle();
                String[] a={getResources().getString(R.string.fojamatban),"Async2 connecting"};
                bundle.putStringArray(Integer.toString(Konstansok.allapot),a);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                mmSocket.connect();
                Log.d("Async2", "KAPCSOLATBAN");

                kuldes=new KuldesThread(mmSocket);
                kuldes.start();

                msg = mHandler.obtainMessage(Konstansok.allapot);
                bundle = new Bundle();
                a[0]=getResources().getString(R.string.aktiv);
                a[1]="KAPCSOLATBAN";
                bundle.putStringArray(Integer.toString(Konstansok.allapot), a);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                msg=mHandler.obtainMessage(Konstansok.kihez);
                bundle=new Bundle();
                bundle.putString(Integer.toString(Konstansok.kihez), mmSocket.getRemoteDevice().getName());
                msg.setData(bundle);
                mHandler.sendMessage(msg);

            } catch (IOException e) {
                Log.e("Async2",e.getMessage());
                bezar();
            }

        }

        public void bezar() {

            try {
                Log.d("Async3", "Socket bezárása");
                Message msg = mHandler.obtainMessage(Konstansok.allapot);
                Bundle bundle = new Bundle();
                String[] a={getResources().getString(R.string.nincs),"Async3 Async bezárása"};
                bundle.putStringArray(Integer.toString(Konstansok.allapot), a);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                msg=mHandler.obtainMessage(Konstansok.kihez);
                bundle=new Bundle();
                bundle.putString(Integer.toString(Konstansok.kihez), "-");
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                mmSocket.close();

            } catch (IOException e) {
                Log.e("Async3", "Socket bezárás hiba: " + e.getMessage());
            }
        }

    }

    private class ServerThread extends Thread{ //szinkron szál

        private final BluetoothServerSocket mmServerSocket;


        public ServerThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                Log.d("Server1", "Server inicializálása");
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Konstansok.SERVER_NAME,
                        Konstansok.SERVER_UUID);
            } catch (IOException e) {
                Log.e("Server1", "Server inicializálás_hiba: "+e.getMessage());

            }
            mmServerSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket=null;

                try {

                    Log.d("Server2", "Server indítása ");
                    Message msg = mHandler.obtainMessage(Konstansok.allapot);
                    Bundle bundle = new Bundle();
                    String[] a={getResources().getString(R.string.fojamatban),"Server2 indítása"};
                    bundle.putStringArray(Integer.toString(Konstansok.allapot), a);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    socket = mmServerSocket.accept((int)(1000*Konstansok.time_out));

                    Log.d("Server2", "Accept vége");
                    kuldes = new KuldesThread(socket);
                    kuldes.start();

                    Log.d("Server2", "KAPCSOLATBAN");
                    msg = mHandler.obtainMessage(Konstansok.allapot);
                    bundle = new Bundle();
                    a[0]=getResources().getString(R.string.aktiv);
                    a[1]="Server2 KAPCSOALTBAN";
                    bundle.putStringArray(Integer.toString(Konstansok.allapot), a);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                    msg=mHandler.obtainMessage(Konstansok.kihez);
                    bundle=new Bundle();
                    bundle.putString(Integer.toString(Konstansok.kihez),socket.getRemoteDevice().getName());
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                } catch (IOException e) {

                    Log.e("Server2", "Hiba: " + e.getMessage());
                    bezar();
                }

        }

        public void bezar() {

            try {
                Log.d("Server3", "Server bezárása ");

                Message msg = mHandler.obtainMessage(Konstansok.allapot);
                Bundle bundle = new Bundle();
                String[] a={getResources().getString(R.string.nincs),"Server3 server bezárása"};
                bundle.putStringArray(Integer.toString(Konstansok.allapot), a);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                msg=mHandler.obtainMessage(Konstansok.kihez);
                bundle=new Bundle();
                bundle.putString(Integer.toString(Konstansok.kihez), "-");
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                mmServerSocket.close();

            } catch (IOException e) {
                Log.e("Server3", "Server bezárás_hiba: "+e.getMessage());

            }
        }

    }

    private class KuldesThread extends Thread{  // küldés szál

        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private final InputStream mmInStream;

        public KuldesThread(BluetoothSocket socket){

            mmSocket=socket;
            OutputStream mmOut = null;
            InputStream mmIn =null;

            try {
                Log.d("Küldés1", "in/output inicializálása");
                mmOut= mmSocket.getOutputStream();
                mmIn = mmSocket.getInputStream();

            } catch (IOException e) {
                Log.e("Küldés1", "in/output inicializálás_hiba: " + e.getMessage());
                bezar();
            }
            mmOutStream=mmOut;
            mmInStream=mmIn;

        }

        public void run() {
            Log.d("Küldés2", "Küldés indítása");
            while(true){

                byte[] buffer = new byte[1024];
                try {

                    Log.d("Küldés2", "Input olvasása ");
                    int proba = mmInStream.read(buffer);

                } catch (IOException e) {
                    Log.e("Küldés2", "Input hiba: "+e.getMessage());

                        Message msg = mHandler.obtainMessage(Konstansok.bezar);
                        Bundle bundle = new Bundle();
                        bundle.putString("hiba", getResources().getString(R.string.nincs));
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        break;

                }

            }
        }

        public void kuld(byte[] buffer){
            try {
                Log.d("Küldés3", "Küldés");
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e("Küldés3", "Küldés hiba: "+e.getMessage());

                    Message msg = mHandler.obtainMessage(Konstansok.bezar);
                    Bundle bundle = new Bundle();
                    bundle.putString("hiba", e.getMessage());
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

            }
        }

        public void bezar() {

            try {
                Log.d("Küldés4", "Küldés bezárása");
                mmSocket.close();

            } catch (IOException e) {

                Log.e("Küldés4", "Küldés bezárás_hiba" + e.getMessage());

            }
        }

    }
    // gombok érintése
    private class RavitelG implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (v.getId()) {

                case R.id.eszkozok:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        eszkozok.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        eszkozok.setBackgroundResource(R.drawable.conn);
                    }
                    break;
                case R.id.kapcsolat:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        kapcsol.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        kapcsol.setBackgroundResource(R.drawable.conn);
                    }
                    break;
                case R.id.bont:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        bont.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        bont.setBackgroundResource(R.drawable.conn);
                    }
                    break;
                case R.id.beallit:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        beallitasok.setBackgroundResource(R.drawable.set2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        beallitasok.setBackgroundResource(R.drawable.set);
                    }
                    break;
                case R.id.bezar:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        bezar.setBackgroundResource(R.drawable.bezar2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        bezar.setBackgroundResource(R.drawable.bezar);
                    }
                    break;
                case R.id.elore:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        elore.setBackgroundResource(R.drawable.nyile2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        elore.setBackgroundResource(R.drawable.nyile);
                    }
                    break;
                case R.id.hatra:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        hatra.setBackgroundResource(R.drawable.nyilh2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        hatra.setBackgroundResource(R.drawable.nyilh);
                    }
                    break;
                case R.id.balra:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        balra.setBackgroundResource(R.drawable.nyilb2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        balra.setBackgroundResource(R.drawable.nyilb);
                    }
                    break;
                case R.id.jobbra:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        jobbra.setBackgroundResource(R.drawable.nyilj2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        jobbra.setBackgroundResource(R.drawable.nyilj);
                    }
                    break;
                case R.id.sorozat:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        sorozat.setBackgroundResource(R.drawable.sorozat2);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        sorozat.setBackgroundResource(R.drawable.sorozat);
                    }
                    break;
            }
            return false;
        }
    }
    // beállítások betöltése
    private void beallitas() throws IOException{
        Log.d("Beállítások_betöltése","Kezdés...");
        File sajat=new File(Environment.getExternalStorageDirectory(),"/BluetoothV/beallitasok.txt");
        FileInputStream fIn = new FileInputStream(sajat);
        BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
        String aDataRow;
        String aBuffer="";

        while ((aDataRow = myReader.readLine()) != null)
        {
            aBuffer += aDataRow ;
        }
        String[] adatok = aBuffer.split(";");
        mozogE = 'E'+adatok[0];
        mozogH = 'H'+adatok[1];
        mozogJ = 'J'+adatok[2];
        mozogB = 'B'+adatok[3];
        if(adatok[4].equals("AS")){
            sincron=false;
        }
        else sincron=true;
        Log.d("Beállítások_betöltése","Befejezve");
    }
    // mozdulatsor betöltése
    private void mozdulatsorBe()throws IOException{
        Log.d("Mozdulatsor_beolvasása","Kezdés...");
        File sajatM=new File(Environment.getExternalStorageDirectory(),"/BluetoothV/mozdulatSor.txt");
        if(sajatM.canRead()){
            FileInputStream fIn = null;
            boolean rossz=false;

            fIn = new FileInputStream(sajatM);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow;
            String aBuffer="";

            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow ;
            }

            for(int i=0;i<aBuffer.length();i++){

                if( !( (aBuffer.charAt(i) <= '9' && aBuffer.charAt(i) >= '0') || aBuffer.charAt(i) == 'E'
                        || aBuffer.charAt(i) == 'H' || aBuffer.charAt(i) == 'J' || aBuffer.charAt(i) == 'B'
                        || aBuffer.charAt(i) == ';' ) ){

                    Toast.makeText(getBaseContext(), R.string.tiltottK +aBuffer.charAt(i), Toast.LENGTH_SHORT).show();
                    rossz=true;
                    break;
                }
            }

            if(!rossz) {
                mozdulatsor=aBuffer;
                Log.d("Mozdulatsor_beolvasása","Beolvasva");
            }


        }
        else {Toast.makeText(getBaseContext(), R.string.nincsM, Toast.LENGTH_SHORT).show();
            Log.d("Mozdulatsor_beolvasása","Nincs mozdulatsor");}

    }
    // szálak bezárása
    private void bezar(){

        if (kuldes != null) {
            kuldes.bezar();
            kuldes=null;
        }
        if (server != null) {
            server.bezar();
            server=null;
        }
        if (async != null) {
            async.bezar();
            async=null;
        }

    }
    // kapcsoalt állapotának beálítása
    private void allapotValt(String allapot,String info){

        this.allapot.setText(allapot);
        Log.d("Állapotváltás",info);
        if(allapot.equals(getResources().getString(R.string.fojamatban))){
            this.allapot.setTextColor(Color.parseColor("#cfae09"));
        }
        else if(allapot.equals(getResources().getString(R.string.nincs))){
            this.allapot.setTextColor(Color.parseColor("#cf0f09"));

        }
        else if(allapot.equals(getResources().getString(R.string.aktiv))){
            this.allapot.setTextColor(Color.parseColor("#42d10e"));

        }

    }
    // kapcsolódott eszköz nevének beállítása
    private void kihez(String kihez){

        eszkoz.setText(kihez);

    }

}
