package com.ablanco.pokemonstats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static android.widget.Toast.*;

public class DisplayPokemon extends AppCompatActivity {

    private static final String URL_API = "https://pokeapi.co/api/v2/pokemon/";
    private static final String URL_WEBSITE = "https://www.pokepedia.fr/";

    private TextView name;
    private TextView speed;
    private TextView type;
    private TextView base_xp;
    private TextView hp;
    private TextView def;
    private TextView def2;
    private TextView att;
    private TextView att2;
    private TextView height;
    private TextView weight;
    private ImageView imageView;
    private RatingBar ratingBar;
    private PokemonInfos infos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_pokemon);

        StrictMode.ThreadPolicy policy = null;
        policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ======== INSTANTIATE COMPONENTS ========== //
        name = findViewById(R.id.name);
        speed = findViewById(R.id.speed);
        base_xp = findViewById(R.id.base_xp);
        hp = findViewById(R.id.hp);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        type = findViewById(R.id.type);
        def = findViewById(R.id.def);
        def2 = findViewById(R.id.def2);
        att = findViewById(R.id.attack);
        att2 = findViewById(R.id.attack2);
        imageView = findViewById(R.id.imageView);
        ratingBar = findViewById(R.id.ratingBar);

        // ======== RETRIEVING DATA ========== //
        Bundle bundle = getIntent().getExtras();
        if (bundle == null
        || !bundle.containsKey(MainActivity.pokeKey)
        || !bundle.containsKey(MainActivity.isRest)){
            Log.println(Log.ERROR,MainActivity.TAG,"Cannot retrieve name !!!");
            toast(getResources().getString(R.string.bad_name),this);
            super.onBackPressed();
        }
        String name = bundle.getString((MainActivity.pokeKey));
        boolean isRest = bundle.getBoolean((MainActivity.isRest));

        Log.println(Log.DEBUG,MainActivity.TAG,"got name : " + name);

        // ======== MAKE REQUEST ========== //
        if(isRest)
            makeRequest(name);
        else
            scrap(name);
    }

    private void makeRequest(final String s){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = URL_API + s.toLowerCase();

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (null != response) {
                            try {
                                Log.println(Log.DEBUG,MainActivity.TAG,"got res : " + response.toString());
                                JSONArray stats = response.getJSONArray("stats");
                                PokemonInfos infos = new PokemonInfos(
                                        response.getString("name"),
                                        response.getJSONArray("types").getJSONObject(0).getJSONObject("type").getString("name"),
                                        response.getInt("height"),
                                        response.getInt("base_experience"),
                                        response.getInt("weight"),
                                        Integer.valueOf(stats.getJSONObject(0).getString("base_stat")),
                                        Integer.valueOf(stats.getJSONObject(2).getString("base_stat")),
                                        Integer.valueOf(stats.getJSONObject(4).getString("base_stat")),
                                        Integer.valueOf(stats.getJSONObject(1).getString("base_stat")),
                                        Integer.valueOf(stats.getJSONObject(3).getString("base_stat")),
                                        Integer.valueOf(stats.getJSONObject(5).getString("base_stat"))
                                );

                                loadPokemon(infos);
                                // === picture === //
                                Log.println(Log.DEBUG,MainActivity.TAG,"got url : " + response.getJSONObject("sprites").getString("front_default"));
                                InputStream is = (InputStream) new URL(response.getJSONObject("sprites").getString("front_default")).getContent();
                                Drawable d = Drawable.createFromStream(is, "src name");
                                imageView.setImageDrawable(d);

                            } catch (JSONException e) {
                                Log.println(Log.ERROR,MainActivity.TAG,"while parsing json : " + e.getMessage());
                            } catch (MalformedURLException e) {
                                Log.println(Log.ERROR,MainActivity.TAG,"bad url for picture : " + e.getMessage());
                            } catch (IOException e) {
                                Log.println(Log.ERROR,MainActivity.TAG,"problem loading picture : " + e.getMessage());
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                notValid();
            }
        });
        queue.add(request);
    }


    private void notValid(){
        Log.println(Log.ERROR,MainActivity.TAG,"Not a valid name !!!");
        toast(getResources().getString(R.string.valid_poke),this);
        finish();
    }

    private void scrap(final String s){
        final String url = URL_WEBSITE + s.toLowerCase();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(url).get();
                    final Element tab1 = document.select(".tableaustandard.ficheinfo tbody").first();
                    final Element tab2 = document.getElementById("Statistiques").parent().nextElementSibling().nextElementSibling().selectFirst(".tableaustandard.électrik tbody");
                    name.post(new Runnable() {
                        @Override
                        public void run() {
                            loadPokemon(new PokemonInfos(
                                    s,
                                    tab1.child(6).select("td a").attr("title").split(" ")[0],
                                    (int) (Float.parseFloat(tab1.child(8).select("td").text().split("m")[0].replace(',','.')) * 10),
                                    Integer.valueOf(tab1.child(14).select("td").text().split(" ")[0]),
                                    (int) Float.parseFloat(tab1.child(9).select("td").text().split("kg")[0].replace(',','.')) * 10,
                                    Integer.valueOf(tab2.child(3).child(1).text()),
                                    Integer.valueOf(tab2.child(5).child(1).text()),
                                    Integer.valueOf(tab2.child(7).child(1).text()),
                                    Integer.valueOf(tab2.child(4).child(1).text()),
                                    Integer.valueOf(tab2.child(6).child(1).text()),
                                    Integer.valueOf(tab2.child(8).child(1).text())
                            ));
                        }
                    });
                } catch (Exception e){
                    Log.d(MainActivity.TAG,e.getMessage());
                    name.post(new Runnable() {
                        @Override
                        public void run() {
                            notValid();
                        }
                    });
                }
            }
        });

    }


    public static void toast(String msg, Context context) {
        makeText(context, msg, LENGTH_LONG).show();
    }

    public void loadPokemon(PokemonInfos infos){
        if (infos == null)
            return;
        toast(getResources().getString(R.string.success),this);
        this.infos = infos;
        name.setText(infos.getName().toUpperCase());
        base_xp.setText(infos.getBaseXP() + "");
        height.setText(infos.getHeight() + "");
        weight.setText(infos.getWeight() + "");
        type.setText(infos.getType());
        hp.setText(infos.getHP() + "");
        att.setText(infos.getAttack() + "");
        def.setText(infos.getDefence() + "");
        att2.setText(infos.getSpeAttack() + "");
        def2.setText(infos.getSpeDefence() + "");
        speed.setText(infos.getSpeed() + "");

        // rating bar animation
        float current = ratingBar.getRating();
        ObjectAnimator anim = ObjectAnimator.ofFloat(ratingBar, "rating", current, ratingBar.getNumStars() * infos.getRating());
        anim.setDuration(3000);
        anim.start();


        MainActivity.verifyStoragePermissions(this);
        write_new_poke_in_file(infos);
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Intent i = new Intent();
        i.putExtra("pokeObj", this.infos);
        outState.putParcelable("pokeObj",i);
        Log.println(Log.DEBUG,MainActivity.TAG,"save...");
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null){
            Log.println(Log.ERROR,MainActivity.TAG,"Cannot retrieve value !!!");
            return;
        }
        Log.println(Log.DEBUG,MainActivity.TAG,"trying to retrieve value...");
        PokemonInfos infos;
        if(savedInstanceState.containsKey("mavaleur"))
            infos = (PokemonInfos) ((Intent)(savedInstanceState.getParcelable("pokeObj"))).getSerializableExtra("pokeObj");
        else
            infos = null;

        this.infos = infos;

    }

    public void write_new_poke_in_file(PokemonInfos infos) {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File fileout = new File(folder, infos.getName().trim() + ".html");
        try (OutputStreamWriter writer =
                     new OutputStreamWriter(new FileOutputStream(fileout), StandardCharsets.UTF_8)) {
            writer.write("<html><head><title>" +infos.getName() + "</title></head><body>" );
            writer.write("<p>Name : " + infos.getName()) ;
            writer.write("</p><p>Type : " + infos.getType());
            writer.write("</p><p>Size : " + infos.getHeight());
            writer.write("</p><p>Weight : " + infos.getWeight());
            writer.write("</p><p>Rating : " + infos.getRating());
            writer.write("</p></body></html>" );
            writer.close();
        } catch (FileNotFoundException e) {
            Log.e(MainActivity.TAG,"File not found",e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MainActivity.TAG,"Error I/O",e);
        }
    }

}
