package com.ablanco.pokemonstats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private Set<String> searchedPokemonName;
    static final String TAG = "myTag";
    static final String pokeKey = "inputpokemonname";
    static final String isRest = "rest";
    private static final int DEFAULT_MAVALEUR = 0;
    private static final String URL_BASE = "https://www.pokepedia.fr/";

    // Listes des permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Vérifie si nous avons les droits d'écriture
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // Aie, il faut les demander àl'utilisateur
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MainActivity curr = this;
        findViewById(R.id.button).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(curr, DisplayPokemon.class);
                intent.putExtra(isRest,true);
                String txt = ((EditText)findViewById(R.id.editText)).getText().toString();
                intent.putExtra(pokeKey, txt);
                startActivity(intent);
            }
        });
        findViewById(R.id.buttonScrap).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(curr, DisplayPokemon.class);
                intent.putExtra(isRest,false);
                String txt = ((EditText)findViewById(R.id.editText)).getText().toString();
                searchedPokemonName.add(txt);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(curr);
                sharedPref.edit().putStringSet("historyPokemonName", searchedPokemonName).apply();
                intent.putExtra(pokeKey, txt);
                startActivity(intent);
            }
        });
        findViewById(R.id.website).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData((Uri.parse((URL_BASE + ((EditText)findViewById(R.id.editText)).getText().toString()))));
                startActivity(intent);
            }
        });
        findViewById(R.id.button2).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curr.finish();
            }
        });
        //display_historic();
        reloadHistoric();
        verifyStoragePermissions(this);
        write_historic_in_file();
    }

    public void reloadHistoric() {
        // Récuperation de l'objet unique qui s'occupe de la sauvegarde
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Récuperation de l'ancienne valeur ou d'une valeur par défaut
        searchedPokemonName = sharedPref.getStringSet("historyPokemonName", new TreeSet<String>());
    }

    public void display_historic() {
        if (searchedPokemonName == null)
            reloadHistoric();
        Log.d(TAG,"Historique ("+ (new Date())+ ") size= "+searchedPokemonName.size()+": ");
        for (String item : searchedPokemonName) {
            Log.d(TAG,"\t- " + item);
        }
    }


    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mavaleur", 324);
        Log.println(Log.DEBUG,TAG,"save...");
    }

    /*
        Here's some steps to test out onRestoreInstanceState():
           1) Press home screen
           2) Kill the app through adb
           3) Launch your app again
     */
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null){
            Log.println(Log.ERROR,TAG,"Cannot retrieve value !!!");
            return;
        }
        Log.println(Log.DEBUG,TAG,"trying to retrieve value...");
        int mavaleur;
        if(savedInstanceState.containsKey("mavaleur"))
            mavaleur = savedInstanceState.getInt(("mavaleur"));
        else
            mavaleur = DEFAULT_MAVALEUR;

        Log.println(Log.DEBUG,TAG,"mavaleur = " + mavaleur);

    }

    public void write_historic_in_file() {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fileout = new File(folder, "pokestat_historic.txt");
        try (FileOutputStream fos = new FileOutputStream(fileout)) {
            PrintStream ps = new PrintStream(fos);
            ps.println("Start of my historic");
            for(String s : searchedPokemonName)
                ps.println("- " + s);
            ps.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG,"File not found",e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Error I/O",e);
        }
    }


}
