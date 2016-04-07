package com.example.toto.bluetoothc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class ActivityConn extends AppCompatActivity {

    private ListView parositottListaTar;
    private ListView lathatoListaTar;
    private ArrayList<String> parositottLista;

    private ArrayAdapter<String> adapter,latottAdapter;// párosított és látott eszközök neve
    private BluetoothDevice bdDevice;

    private ArrayList<BluetoothDevice> arrayListParositottLista;// párosított eszközök listája

    private ArrayList<BluetoothDevice> arrayListLathatoLista;// látható eszközök listája
    private BluetoothAdapter bluetoothAdapter = null;

    private Button keres,lathato;// keresés kezdése és láthatóság beállítása
    private ListItemClick listItemClick;// látható eszközök kattintásának figyelése
    private ListItemClickParositott listItemClickParositott;// párostott eszközök kattintásának figyelése
    private RavitelG ravitelG;// érintés érézkelés

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conn);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        lathatoListaTar = (ListView) findViewById(R.id.lathatoBlue);
        parositottListaTar = (ListView) findViewById(R.id.parosiBlue);
        keres = (Button) findViewById(R.id.blueKeres);
        lathato = (Button) findViewById(R.id.lathatosag);
        parositottLista = new ArrayList<String>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayListParositottLista = new ArrayList<BluetoothDevice>();

        ravitelG = new RavitelG();
        keres.setOnTouchListener(ravitelG);
        lathato.setOnTouchListener(ravitelG);

        arrayListLathatoLista = new ArrayList<BluetoothDevice>();
        adapter= new ArrayAdapter<String>(ActivityConn.this, android.R.layout.simple_list_item_single_choice, parositottLista);
        parositottListaTar.setAdapter(adapter);
        listItemClick = new ListItemClick();
        listItemClickParositott = new ListItemClickParositott();

        latottAdapter = new ArrayAdapter<String>(ActivityConn.this, android.R.layout.simple_list_item_checked);
        lathatoListaTar.setAdapter(latottAdapter);

        lathatoListaTar.setOnItemClickListener(listItemClick);
        parositottListaTar.setOnItemClickListener(listItemClickParositott);
        // keresés
        this.keres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keresesKezdese();
            }
        });
        // láthatóság beállítás
        this.lathato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lathatosagBeallit();
            }
        });
    }

    private static final int DISCOVER_DURATION = 300;


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // bluetooth ellenörzése
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,1);

        }
        // párosított eszközök kiírása
        keresParositott();

    }

    public void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // bluetooth ellenörzése
        if(!(resultCode==Activity.RESULT_OK)){
            Toast.makeText(getApplicationContext(), R.string.bluetoothNincs, Toast.LENGTH_LONG).show();
            finish();
        }
    }
    // párosítások keresése
    private void keresParositott() {
        adapter.clear();
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

        if(pairedDevice.size()>0)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                adapter.add(device.getName()+"\n"+device.getAddress());
                arrayListParositottLista.add(device);
            }

        }

        Toast.makeText(getApplicationContext(), R.string.parosiBe, Toast.LENGTH_SHORT).show();
    }
    // pársítás
    private class ListItemClick implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            bdDevice = arrayListLathatoLista.get(position);

            Log.i("Párosítandó_eszköz",bdDevice.toString());

            Boolean isBonded = false;

                isBonded = parositas(bdDevice);

            if(isBonded){
                adapter.notifyDataSetChanged();
            }
            Log.d("Párosítás", String.valueOf(isBonded));

        }
    }
    // párosítás eltávolítása
    private class ListItemClickParositott implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {


            bdDevice = arrayListParositottLista.get(position);

                Log.d("PárosításMegsz", bdDevice.toString());
                Boolean removeBonding = parositasMegszuntetes(bdDevice);
                if(removeBonding)
                {
                    parositottLista.remove(position);
                    adapter.notifyDataSetChanged();
                    Log.d("PárosításMegsz", String.valueOf(removeBonding));
                }


        }
    }

    public boolean parositasMegszuntetes(BluetoothDevice btDevice)
    {
        Class btClass = null;
        try {
            Log.d("Párosításmegsz", "Megszüntetés...");

            btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            removeBondMethod.invoke(btDevice);
            return true;
        } catch (Exception e) {
            Log.e("Párosításmegsz", "Hiba: " + e.getMessage());
            return false;
        }

    }


    public boolean parositas(BluetoothDevice btDevice)
    {
        Class class1 = null;
        try {
            Log.d("Párosítás", "kezdés..");

            class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");
            boolean paros= (boolean)createBondMethod.invoke(btDevice);

            return paros;
        } catch (Exception e) {
            Log.e("Párosítás", "Hiba: " + e.getMessage());

            return false;
        }

    }
    // érintés figyelés
    private class RavitelG implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (v.getId()) {

                case R.id.blueKeres:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        keres.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        keres.setBackgroundResource(R.drawable.conn);
                    }
                    break;
                case R.id.lathatosag:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        lathato.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        lathato.setBackgroundResource(R.drawable.conn);
                    }
                    break;

            }
            return false;
        }
    }
// keresés
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
           if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
               Toast.makeText(context, R.string.kezdes, Toast.LENGTH_SHORT).show();
           }
           else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
               Toast.makeText(context, R.string.befejezes, Toast.LENGTH_SHORT).show();

           }
           else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Toast.makeText(context, R.string.talal, Toast.LENGTH_SHORT).show();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(arrayListLathatoLista.size()<1) // ha a lista üres
                {
                    latottAdapter.add(device.getName()+"\n"+device.getAddress());
                    arrayListLathatoLista.add(device);
                    latottAdapter.notifyDataSetChanged();
                }
                else
                {// ha nem üres me kell nézni nem szerepel e már benne
                    boolean nincs = true;
                    for(int i = 0; i<arrayListLathatoLista.size();i++)
                    {
                        if(device.getAddress().equals(arrayListLathatoLista.get(i).getAddress()))
                        {
                            nincs = false;
                        }
                    }
                    if(nincs)
                    {// ha nem szerepel hozzáadjuk a listához
                        latottAdapter.add(device.getName()+"\n"+device.getAddress());
                        arrayListLathatoLista.add(device);
                        latottAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };
    private void keresesKezdese() {
        Log.i("Keresés", "Keresés kezdése...");

        arrayListLathatoLista.clear();
        int a=lathatoListaTar.getCount();

        if(a!=0){
            latottAdapter.clear();
            lathatoListaTar.setAdapter(latottAdapter);
            }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ActivityConn.this.registerReceiver(myReceiver, intentFilter);

        bluetoothAdapter.startDiscovery();
    }

    private void lathatosagBeallit() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivity(discoverableIntent);
        Log.i("Láthatóság", "Beállítva "+DISCOVER_DURATION+" mp-re");
    }

}
