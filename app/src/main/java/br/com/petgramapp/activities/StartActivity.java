package br.com.petgramapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import br.com.petgramapp.R;
import br.com.petgramapp.fragments.HomeFragment;
import br.com.petgramapp.fragments.NotificacaoFragment;
import br.com.petgramapp.fragments.PerfilFragment;
import br.com.petgramapp.fragments.PesquisarFragment;
import br.com.petgramapp.fragments.PostarFragment;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.Permissao;

public class StartActivity extends AppCompatActivity {
    //firebase
    private FirebaseAuth firebaseAuth;

    //lista de permissões
    private final String[] listaPermissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    //navigations
    public BottomNavigationView bottomNavigationViewStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        carregarElementos();
        //validando as permissoes
        Permissao.validarPermissoes(listaPermissoesNecessarias,this,1);

        //configurações iniciais
        configurarBottomNavigation();
        FragmentManager fragmentManager= getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            String autorComentario = bundle.getString("idAutorComentario");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
            editor.putString("idUsuario",autorComentario);
            editor.apply();

            fragmentTransaction.replace(R.id.fragment_container_principal_StartAct,new PerfilFragment()).commit();

        }else{
            fragmentTransaction.replace(R.id.fragment_container_principal_StartAct,new HomeFragment()).commit();
        }




    }

    @Override
    public void onBackPressed() {
      Intent intent = new Intent(this,StartActivity.class);
      startActivity(intent);
    }


    public void configurarBottomNavigation(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavigation_StartAct);
        //configuração
        bottomNavigationViewEx.enableAnimation(true);
        bottomNavigationViewEx.enableItemShiftingMode(true);
        bottomNavigationViewEx.enableShiftingMode(true);
        bottomNavigationViewEx.setTextVisibility(true);
        bottomNavigationViewEx.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.branco)));

        //criando eventos de clique no navigationbottom
        habilitarEventosNavigation(bottomNavigationViewEx);

        //configurar menu inicial quando tela for carrega ou houver algum impacto na rede
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);


    }

    private void habilitarEventosNavigation(BottomNavigationViewEx viewEx){
        viewEx.setOnNavigationItemSelectedListener(item -> {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            switch (item.getItemId()){

                case R.id.home_navigation_itemMenu:
                    fragmentTransaction.replace(R.id.fragment_container_principal_StartAct , new HomeFragment()).commit();
                    return true;

                case R.id.perfil_navigation_itemMenu:
                    SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                    editor.putString("idUsuario",firebaseAuth.getCurrentUser().getUid());
                    editor.apply();
                    fragmentTransaction.replace(R.id.fragment_container_principal_StartAct , new PerfilFragment()).commit();
                    return true;

                case R.id.postar_navigation_itemMenu:
                   // startActivity(new Intent(getContext(),PostagemActivity.class));
                    fragmentTransaction.replace(R.id.fragment_container_principal_StartAct , new PostarFragment()).commit();
                    return true;

                case R.id.notificacao_navigation_itemMenu:
                    fragmentTransaction.replace(R.id.fragment_container_principal_StartAct , new NotificacaoFragment()).commit();
                    return true;

                case R.id.pesquisar_navigation_itemMenu:
                    fragmentTransaction.replace(R.id.fragment_container_principal_StartAct , new PesquisarFragment()).commit();
                    return true;
            }

            return false;
        });
    }

    public void carregarElementos(){
        bottomNavigationViewStart = findViewById(R.id.bottomNavigation_StartAct);

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao();
   }

    private void deslogarUsuario(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.deseja_sair_app);
        builder.setIcon(R.drawable.ic_pets_black_24dp);
        builder.setMessage(R.string.deseja_sair_app_message);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirmar), (dialog, which) -> {
            try {
                firebaseAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Erro." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            Toast.makeText(getContext(), "Muito bem. Continue se divertindo com os pets do mundo todo!", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private Context getContext() {
        return StartActivity.this;
    }

    /*//criando menu na tela
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sair_usuarios,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //recuperando valores dos itens
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.item_sair_MenuSair) {
            deslogarUsuario();
        }
        return super.onOptionsItemSelected(item);

    }*/

}



























