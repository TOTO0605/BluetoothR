package com.example.toto.bluetoothc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class AsyncActivity extends AppCompatActivity {

    ListView parositottListaTar;
    ArrayList<BluetoothDevice> arrayListParositottLista;
    ArrayAdapter<String> adapter;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> parositottLista;
    ListItemClickParositott listItemClickParositott;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        parositottListaTar = (ListView) findViewById(R.id.parositott);
        parositottLista = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(AsyncActivity.this, android.R.layout.simple_list_item_single_choice, parositottLista);
        parositottListaTar.setAdapter(adapter);
        listItemClickParositott = new ListItemClickParositott();
        parositottListaTar.setOnItemClickListener(listItemClickParositott);
        arrayListParositottLista = new ArrayList<BluetoothDevice>();

    }

    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        keresParositott();

    }

    private void keresParositott(){

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

    private class ListItemClickParositott implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fojamatban), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("eszkozNev",arrayListParositottLista.get(position).getAddress());
            setResult(RESULT_OK, intent);
            finish();

        }
    }

}
