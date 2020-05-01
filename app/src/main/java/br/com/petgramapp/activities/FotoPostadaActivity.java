package br.com.petgramapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterFotoPostada;
import br.com.petgramapp.adapter.AdapterFotoPostadaComActivity;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.FotoPostada;

public class FotoPostadaActivity extends AppCompatActivity {

    public AdapterFotoPostadaComActivity adapterFotoPostada;
    private List<FotoPostada> fotoPostadaList;
    private RecyclerView recyclerViewFotoPostagemFragment;
    private String idPostagem;

    //FIREBASE
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_postada);

        databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();

        SharedPreferences preferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        idPostagem = preferences.getString("idPostagem","default");

        recyclerViewFotoPostagemFragment = findViewById(R.id.recyclerView_fotoPostagemFragment);
        recyclerViewFotoPostagemFragment.setHasFixedSize(true);
        //inverter ordem das postagem (a mais atual)
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerViewFotoPostagemFragment.setLayoutManager(linearLayout);

        fotoPostadaList = new ArrayList<>();
        adapterFotoPostada = new AdapterFotoPostadaComActivity(fotoPostadaList, this);

        recyclerViewFotoPostagemFragment.setAdapter(adapterFotoPostada);
        receberPostagens();
    }

    private void receberPostagens() {
        DatabaseReference postReference = databaseReference.child("Posts").child(idPostagem);
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fotoPostadaList.clear();
                FotoPostada fotoPostada = dataSnapshot.getValue(FotoPostada.class);
                fotoPostadaList.add(fotoPostada);
                adapterFotoPostada.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean navigateUpToFromChild(Activity child, Intent upIntent) {
        return super.navigateUpToFromChild(child, upIntent);
    }

    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }

    @Override
    public boolean navigateUpTo(Intent upIntent) {
        return super.navigateUpTo(upIntent);
    }
}
