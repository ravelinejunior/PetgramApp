package br.com.petgramapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.fragments.PesquisarFragment;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Stories;
import br.com.petgramapp.model.Usuario;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoriesActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private int count = 0;
    private long pressTime = 0L;
    private long limite = 500L;
    private ImageView imagemStories;
    private ImageView fotoStoriesPerfil;
    private ImageView visualicoesStories;
    private ImageView deletarStories;
    private TextView nomeUsuarioStories;
    private TextView visualizacoesNumeroStories;
    private StoriesProgressView storiesProgressView;
    private LinearLayout storiesVisualizadosLayout;
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
        idUsuario = getIntent().getStringExtra("idUsuario");

        storiesProgressView = findViewById(R.id.storiesProgress_StoriesActivity);
        imagemStories = findViewById(R.id.imagem_stories_StoriesActivity);
        fotoStoriesPerfil = findViewById(R.id.fotoStoriesPerfil_StoriesActivity);
        nomeUsuarioStories = findViewById(R.id.nomeUsuario_StoriesActivity);
        storiesVisualizadosLayout = findViewById(R.id.stories_visualizados_StoriesLayout);
        deletarStories = findViewById(R.id.deletar_StoriesActivity);
        visualizacoesNumeroStories = findViewById(R.id.numeroVisualizacoes_StoriesActivity);
        storiesVisualizadosLayout.setVisibility(View.GONE);
        deletarStories.setVisibility(View.GONE);

        if (idUsuario.equals(UsuarioFirebase.getIdentificadorUsuario())){
            storiesVisualizadosLayout.setVisibility(View.VISIBLE);
            deletarStories.setVisibility(View.VISIBLE);
        }


        getStories(idUsuario);
        getUsuarioInfo(idUsuario);

        View reverse = findViewById(R.id.reverse_StoriesActivity);
        reverse.setOnClickListener(v -> storiesProgressView.reverse());

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip_StoriesActivity);
        reverse.setOnClickListener(v -> storiesProgressView.skip());

        skip.setOnTouchListener(onTouchListener);

        storiesVisualizadosLayout.setOnClickListener(v -> {

            Intent intent = new Intent(StoriesActivity.this, SeguidoresActivity.class);
            intent.putExtra("idUsuario",idUsuario);
            intent.putExtra("idStories",storiesId.get(count));
            intent.putExtra("titulo","Views");
            startActivity(intent);

        });

        deletarStories.setOnClickListener(v -> {

            DatabaseReference storiesRef = ConfiguracaoFirebase.getReferenciaDatabase()
                    .child("Stories").child(idUsuario).child(storiesId.get(count));

          storiesRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {

                  if (task.isSuccessful()){
                      Toast.makeText(StoriesActivity.this, "Deletado.", Toast.LENGTH_SHORT).show();
                      finish();
                  }

              }
          });

        });
    }

    @Override
    public void onNext() {
        Picasso.get().load(imagens.get(++count)).into(imagemStories);

        addVisualizacao(storiesId.get(count));
        visualicoesNumero(storiesId.get(count));
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

                addVisualizacao(storiesId.get(count));
                visualicoesNumero(storiesId.get(count));
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

    private void addVisualizacao(String idStories){
        ConfiguracaoFirebase.getReferenciaDatabase().
                child("Stories").
                    child(idUsuario).
                        child(idStories)
                            .child("Views")
                                .child(UsuarioFirebase.getIdentificadorUsuario()).setValue(true);

    }

    private void visualicoesNumero(String idStories){
        DatabaseReference visualizacoesRef = ConfiguracaoFirebase.getReferenciaDatabase().
                child("Stories").
                    child(idUsuario).
                        child(idStories)
                            .child("Views");

        visualizacoesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                visualizacoesNumeroStories.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}














