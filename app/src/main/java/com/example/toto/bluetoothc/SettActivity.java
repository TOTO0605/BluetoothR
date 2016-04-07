package com.example.toto.bluetoothc;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SettActivity extends AppCompatActivity {

    private Button mentes;// beállítások mentése
    private Button mentes2;//mozdulatsor mentése
    private Button hozzaAd;// mozdulatsor hozzáadása
    private Switch szinkron;
    private EditText elore;
    private EditText hatra;
    private EditText balra;
    private EditText jobbra;
    private TextView hol;
    private EditText mozdulatok;
    private RavitelG ravitelG;// érintés

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sett);

        ravitelG = new RavitelG();
        this.mentes = (Button) findViewById(R.id.mentes);
        this.mentes2 = (Button) findViewById(R.id.mentes2);
        this.hozzaAd = (Button) findViewById(R.id.hozzaAd);
        this.szinkron = (Switch) findViewById(R.id.sincron);
        this.elore = (EditText) findViewById(R.id.eloreBe);
        this.hatra= (EditText) findViewById(R.id.hatraBe);
        this.balra = (EditText) findViewById(R.id.balraBe);
        this.jobbra = (EditText) findViewById(R.id.jobbraBe);
        this.hol = (TextView) findViewById(R.id.hol);
        this.mozdulatok = (EditText) findViewById(R.id.mozdulatok);
        this.hol.setText(Environment.getExternalStorageDirectory() + "/BluetoothV/mozdulatSor.txt");
        beallitasok();
        mozdulatsorBe();
        this.mentes.setOnTouchListener(ravitelG);
        this.hozzaAd.setOnTouchListener(ravitelG);
        this.mentes2.setOnTouchListener(ravitelG);
        this.mentes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (elore.getText().toString().equals("") || hatra.getText().toString().equals("")
                        || balra.getText().toString().equals("") || jobbra.getText().toString().equals("")) {

                    Toast.makeText(getBaseContext(),R.string.nincsKitoltve , Toast.LENGTH_SHORT).show();

                } else {

                    try {
                        Log.d("Beállítások_mentése", "Kezdés...");
                        String beallitasok = elore.getText().toString() + ";" + hatra.getText().toString() + ";"
                                + jobbra.getText().toString() + ";" + balra.getText().toString() + ";";
                        if (szinkron.isChecked()) {
                            beallitasok += "S";
                        } else beallitasok += "AS";

                        File sajat = new File(Environment.getExternalStorageDirectory(), "/BluetoothV/beallitasok.txt");
                        BufferedWriter out = new BufferedWriter(new FileWriter(sajat.getAbsolutePath(), false));

                        out.write(beallitasok);
                        out.close();
                        Log.d("Beállítások_mentése", "Kész");
                        Toast.makeText(getBaseContext(), R.string.beallitasM, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("frissit", true);
                        setResult(Konstansok.beallitasF, intent);
                        finish();

                    } catch (Exception e1) {
                        Log.e("Beállítások_mentése","Hiba: " +e1.getMessage());
                        Toast.makeText(getBaseContext(), R.string.beallitasMhiba, Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        this.mentes2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Mozdulatsor_mentése", "Kezdés...");
                String mozdulatsor=mozdulatok.getText().toString();
                boolean rossz=false;
                for(int i=0;i<mozdulatsor.length();i++){

                    if( !( (mozdulatsor.charAt(i) <= '9' && mozdulatsor.charAt(i) >= '0') || mozdulatsor.charAt(i) == 'E'
                            || mozdulatsor.charAt(i) == 'H' || mozdulatsor.charAt(i) == 'J' || mozdulatsor.charAt(i) == 'B'
                            || mozdulatsor.charAt(i) == ';' ) ){

                        Toast.makeText(getBaseContext(), R.string.tiltottK +mozdulatsor.charAt(i), Toast.LENGTH_SHORT).show();
                        rossz=true;
                        Log.d("Mozdulatsor_mentése", "Hibás output: "+mozdulatsor.charAt(i));
                        break;
                    }
                }
                if(!rossz){
                    File sajat = new File(Environment.getExternalStorageDirectory(), "/BluetoothV/mozdulatSor.txt");
                    BufferedWriter out = null;
                    try {
                        out = new BufferedWriter(new FileWriter(sajat.getAbsolutePath(), false));
                        out.write(mozdulatsor);
                        out.close();
                        Toast.makeText(getBaseContext(), R.string.mozdulatsorM, Toast.LENGTH_SHORT).show();
                        Log.d("Mozdulatsor_mentése", "Kész");
                        Intent intent = new Intent();
                        intent.putExtra("frissit", true);
                        setResult(Konstansok.mozdulatSorF, intent);
                        finish();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), R.string.mozdulatsorMhiba, Toast.LENGTH_SHORT).show();
                        Log.e("Mozdulatsor_mentés", "Hiba: " + e.getMessage());
                    }

                }

            }
        });

        hozzaAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adat = elore.getText().toString();
                if(!adat.equals("")){
                    mozdulatok.append('E'+adat+';');
                }
                adat = hatra.getText().toString();
                if(!adat.equals("")){
                    mozdulatok.append('H'+adat+';');
                }
                adat = balra.getText().toString();
                if(!adat.equals("")){
                    mozdulatok.append('B'+adat+';');
                }
                adat = jobbra.getText().toString();
                if(!adat.equals("")){
                    mozdulatok.append('J'+adat+';');
                }
            }
        });
    }

    private class RavitelG implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (v.getId()) {

                case R.id.mentes:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mentes.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mentes.setBackgroundResource(R.drawable.conn);
                    }
                    break;
                case R.id.mentes2:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mentes2.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mentes2.setBackgroundResource(R.drawable.conn);
                    }
                    break;
                case R.id.hozzaAd:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        hozzaAd.setBackgroundResource(R.drawable.input);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        hozzaAd.setBackgroundResource(R.drawable.conn);
                    }
                    break;
            }
            return false;
        }

    }

    private void beallitasok(){
        Log.d("Beállítások_betöltése(2)", "Kezdés...");
        try {
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
            elore.setText(adatok[0]);
            hatra.setText(adatok[1]);
            jobbra.setText(adatok[2]);
            balra.setText(adatok[3]);
            if(adatok[4].equals("AS")){
                this.szinkron.setChecked(false);
            }
            else this.szinkron.setChecked(true);
            Log.d("Beállítások_betöltése(2)", "Befejezve");
        }
        catch (Exception e) {
            Log.e("Beállítások_betöltése(2)","Hiba: " +e.getMessage());

            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void mozdulatsorBe(){
        Log.d("Mozdulatsor_beolvasása(2)","Kezdés...");
        File sajatM=new File(Environment.getExternalStorageDirectory(),"/BluetoothV/mozdulatSor.txt");
        if(sajatM.canRead()){
            FileInputStream fIn = null;
            boolean rossz=false;
            try {
                fIn = new FileInputStream(sajatM);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String aDataRow;
                String aBuffer="";

                while ((aDataRow = myReader.readLine()) != null)
                {
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
                    mozdulatok.setText(aBuffer);
                    Log.d("Mozdulatsor_beolvasása(2)", "Befejezve");
                }
            } catch (Exception e) {
                Log.e("Mozdulatsor_beolvasása(2)","Hiba: " +e.getMessage());
            }

        }
        else Toast.makeText(getBaseContext(), R.string.nincsM, Toast.LENGTH_SHORT).show();

    }

}
