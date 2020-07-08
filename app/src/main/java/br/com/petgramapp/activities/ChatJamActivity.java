package br.com.petgramapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;

import br.com.petgramapp.R;
import br.com.petgramapp.fragments.ContatosFragmentJam;
import br.com.petgramapp.fragments.ConversasFragmentJam;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;

public class ChatJamActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private MaterialSearchView conversasSearchView;
    private MaterialSearchView contatosSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_jam);
        carregarElementos();
        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //configuração de abas
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Conversas", ConversasFragmentJam.class)
                        .add("Contatos", ContatosFragmentJam.class)
                        .create()

        );

        ViewPager viewPager = findViewById(R.id.viewPagerTab);
        viewPager.setAdapter(adapter);

        SmartTabLayout smartTabLayout = findViewById(R.id.smartTabLayout);
        smartTabLayout.setViewPager(viewPager);

        //CONFIGURAÇÃO DE VOZ DO SEARCH VIEW
        conversasSearchView.setVoiceSearch(true);
        conversasSearchView.setVoiceIcon(getResources().getDrawable(R.drawable.ic_action_voice_search));

        //configuração do search view para conversas
        conversasSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //verificar se está pesquisando conversa ou contato
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        ConversasFragmentJam fragmentJam = (ConversasFragmentJam) adapter.getPage(0);
                        if (query != null && !query.isEmpty()) {
                            fragmentJam.pesquisarConversas(query.toLowerCase());
                        }
                        break;

                    case 1:
                        ContatosFragmentJam contatosFragmentJam = (ContatosFragmentJam) adapter.getPage(1);

                        if (query != null && !query.isEmpty()) {
                            contatosFragmentJam.getContatosJam(query.toLowerCase());
                        }
                        break;
                }


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Log.d("queryText",newText.toString());

                //verificar se está pesquisando conversa ou contato
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        ConversasFragmentJam fragmentJam = (ConversasFragmentJam) adapter.getPage(0);
                        if (newText != null && !newText.isEmpty()) {
                            fragmentJam.pesquisarConversas(newText.toLowerCase());
                        }
                        break;

                    case 1:
                        ContatosFragmentJam contatosFragmentJam = (ContatosFragmentJam) adapter.getPage(1);

                        if (newText != null && !newText.isEmpty()) {
                            contatosFragmentJam.getContatosJam(newText.toLowerCase());
                        }
                        break;
                }


                return true;
            }
        });

        conversasSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                //verificar qual metodo de pesquisa está ativado
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        ConversasFragmentJam fragmentJam = (ConversasFragmentJam) adapter.getPage(0);
                        fragmentJam.reloadConversas();
                        break;
                    case 1:
                        ContatosFragmentJam contatosFragmentJam = (ContatosFragmentJam) adapter.getPage(1);
                        contatosFragmentJam.reloadContatosJam();
                        break;
                }

            }
        });

     /*   //CONFIGURAÇÃO DO SEARCH VIEW PARA USUARIOS
        contatosSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ContatosFragmentJam contatosFragmentJam = (ContatosFragmentJam) adapter.getPage(1);

                if (query != null && !query.isEmpty()) {
                    contatosFragmentJam.getContatosJam(query.toLowerCase());
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ContatosFragmentJam contatosFragmentJam = (ContatosFragmentJam) adapter.getPage(1);

                if (newText != null && !newText.isEmpty()) {
                    contatosFragmentJam.getContatosJam(newText.toLowerCase());
                }

                return true;
            }
        });

        contatosSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ContatosFragmentJam contatosFragmentJam = (ContatosFragmentJam) adapter.getPage(1);
                contatosFragmentJam.reloadContatosJam();
            }
        });*/

        updateToken();
    }

    private void updateToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();
        Log.i("TokenUser", token);

        if (idUsuario != null) {
            firebaseFirestore.collection("Usuarios")
                    .document(idUsuario)
                    .update("token", token);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_pesquisa_jam, menu);

        //CONFIGURAÇÃO DO MENU PESQUISA
        MenuItem item = menu.findItem(R.id.action_search_MenuJam);
        conversasSearchView.setMenuItem(item);
        conversasSearchView.setVoiceIcon(getDrawable(R.drawable.ic_action_voice_search));

     /*   MenuItem item2 = menu.findItem(R.id.action_searchContatos_MenuJam);
        contatosSearchView.setMenuItem(item2);
        contatosSearchView.setVoiceIcon(getDrawable(R.drawable.ic_action_voice_search));*/


        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sair_MenuJam:
                deslogarUsuario();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void carregarElementos() {

        toolbar = findViewById(R.id.toobar_ChatJam_Principal);
        toolbar.setTitle("Pet Talks");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        contatosSearchView = findViewById(R.id.search_viewContatosPrincipal);
        conversasSearchView = findViewById(R.id.search_viewPrincipal);


    }

    public void deslogarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.deseja_sair_app);
        builder.setIcon(R.drawable.ic_pets_black_24dp);
        builder.setMessage(R.string.deseja_sair_app_message);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirmar), (dialog, which) -> {
            try {
                firebaseAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Erro." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            Toast.makeText(this, "Muito bem. Continue se divertindo com os pets do mundo todo!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    conversasSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (conversasSearchView.isSearchOpen()) {
            conversasSearchView.closeSearch();
        } else {
         Intent intent = new Intent(this,StartActivity.class);
         startActivity(intent);
        }
    }
}
