package br.com.petgramapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.Stories;
import br.com.petgramapp.model.Usuario;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoriesActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private int count = 0;
    private long pressTime = 0L;
    private long limite = 500L;
    private ImageView imagemStories;
    private ImageView fotoStoriesPerfil;
    private TextView nomeUsuarioStories;
    private StoriesProgressView storiesProgressView;

    List<String> imagens;
    List<String> storiesId;
    String idUsuario;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()){

                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limite < now - pressTime;

            }

            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        storiesProgressView = findViewById(R.id.storiesProgress_StoriesActivity);
        imagemStories = findViewById(R.id.imagem_stories_StoriesActivity);
        fotoStoriesPerfil = findViewById(R.id.fotoStoriesPerfil_StoriesActivity);
        nomeUsuarioStories = findViewById(R.id.nomeUsuario_StoriesActivity);

        idUsuario = getIntent().getStringExtra("idUsuario");
        getStories(idUsuario);
        getUsuarioInfo(idUsuario);

        View reverse = findViewById(R.id.reverse_StoriesActivity);
        reverse.setOnClickListener(v -> storiesProgressView.reverse());

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip_StoriesActivity);
        reverse.setOnClickListener(v -> storiesProgressView.skip());

        skip.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onNext() {
        Picasso.get().load(imagens.get(++count)).into(imagemStories);
    }

    @Override
    public void onPrev() {
        if ((count - 1) < 0) return;
        Picasso.get().load(imagens.get(--count)).into(imagemStories);
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String idUsuario){
        imagens = new ArrayList<>();
        storiesId = new ArrayList<>();
        DatabaseReference storiesRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Stories")
                .child(idUsuario);
        storiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imagens.clear();
                storiesId.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Stories stories = ds.getValue(Stories.class);
                    long timeCurrent = System.currentTimeMillis();
                    if (timeCurrent > stories.getDataInicio() && timeCurrent < stories.getDataFim()){
                        imagens.add(stories.getUrlStoriesFoto());
                        storiesId.add(stories.getIdStories());

                    }
                }
                storiesProgressView.setStoriesCount(imagens.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoriesActivity.this);
                storiesProgressView.startStories(count);
                Picasso.get().load(imagens.get(count)).into(imagemStories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsuarioInfo(String idUsuarioInfo){
        DatabaseReference usuarioRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios")
                .child(idUsuarioInfo);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                Picasso.get().load(usuario.getUriCaminhoFotoPetUsuario()).into(fotoStoriesPerfil);
                nomeUsuarioStories.setText(usuario.getNomePetUsuario());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
