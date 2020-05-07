package br.com.petgramapp.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.FiltrosAdapterThumbnails;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.RecyclerItemClickListener;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.FotoPostada;
import br.com.petgramapp.model.Usuario;

public class PostagemActivity extends AppCompatActivity {

    //WIDGETS
    Uri uriImagemPostagem;
    String imagemUrlPostagem;
    TextInputEditText descricaoInputTextPostagem;
    ImageView imagemSelecionadaPostagem;
    Button botaoPostarFoto;
    private Bitmap imagem;
    private Bitmap imagemFiltro;
    private List<ThumbnailItem> listaFiltros;
    private RecyclerView recyclerViewFiltros;
    private FiltrosAdapterThumbnails adapterFiltros;

    // firebase
    StorageTask uploadTaskPostagem;
    StorageReference storageReferencePostagem;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    ProgressDialog dialog;

    //usuarios
    private String idUsuarioLogado;
    private Usuario usuarioLogado;

    //bloco de inicialização de filtros
    static
    {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onSaveInstanceState(Bundle oldInstanceState) {
        super.onSaveInstanceState(oldInstanceState);
        oldInstanceState.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postagem);
        carregarElementos();
        //CONFIGURAÇÕES INICIAIS
        firebaseRef = ConfiguracaoFirebase.getReferenciaDatabase();
        usuarioLogado = UsuarioFirebase.getUsuarioLogado();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuariosRef = ConfiguracaoFirebase.getReferenciaDatabase().child("usuarios");

        //FILTROS
        listaFiltros = new ArrayList<>();
        //recuperando imagem da tela de fragment
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            byte[] dadosFoto = bundle.getByteArray("fotoSelecionada");
            //permitir editar byte array
            if (dadosFoto != null) {
                imagem = BitmapFactory.decodeByteArray(dadosFoto,0,dadosFoto.length);
            } else {
                Toast.makeText(this, "Dados foto vazio ou muito grande.", Toast.LENGTH_SHORT).show();
            }
        }

        //recuperar dados da postagem
        recuperarDadosPostagem();

        imagemSelecionadaPostagem.setImageBitmap(imagem);
        imagemFiltro = imagem.copy(imagem.getConfig(),true);

        //configurando o RecyclerView

        adapterFiltros = new FiltrosAdapterThumbnails(listaFiltros,this);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerViewFiltros.setLayoutManager(layoutManager);
        recyclerViewFiltros.setAdapter(adapterFiltros);

        recyclerViewFiltros.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewFiltros, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ThumbnailItem item = listaFiltros.get(position);
                imagemFiltro = imagem.copy(imagem.getConfig(),true);
                Filter filter = item.filter;
                imagemSelecionadaPostagem.setImageBitmap(filter.processFilter(imagemFiltro));


            }

            @Override
            public void onLongItemClick() {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }
        ));

        recuperarFiltros();

        storageReferencePostagem = FirebaseStorage.getInstance().getReference().child("Posts");

        botaoPostarFotoAcionar();
        //CropImage.activity().setAspectRatio(1,1).start(PostagemActivity.this);
    }

    public void carregarElementos(){
        descricaoInputTextPostagem = findViewById(R.id.descricao_id_input_edittext_Postagem_id);
        imagemSelecionadaPostagem = findViewById(R.id.imagem_foto_selecionada_Postagem_activity);
        botaoPostarFoto = findViewById(R.id.botao_PostarFoto_Postagem_id);
        recyclerViewFiltros = findViewById(R.id.recycler_view_Postagem_id);

        //configurando toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_PostagemGaleria);
        toolbar.setTitle("Postar PetFoto");
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.branco));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_voltar_back);

    }

    private void recuperarDadosPostagem(){
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        //assim que função é acionada, setar valor como true
        abrirDialogCarregamento("Carregando dados, aguarde!");
        usuarioLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //recuperando valores de usuarios
                usuarioLogado = dataSnapshot.getValue(Usuario.class);
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void postarFoto(){

        //verificando se dados ja foram carregados antes de postar foto
        abrirDialogCarregamento("Postando foto, aguarde!");
        FotoPostada fotoPostada = new FotoPostada();

        fotoPostada.setIdUsuarioPostou(idUsuarioLogado);
        fotoPostada.setDescricaoImagemPostada(Objects.requireNonNull(descricaoInputTextPostagem.getText()).toString());

        //recuperar dados da imagem para salvar no firebaseStorage para depois salvar no firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagemFiltro.compress(Bitmap.CompressFormat.WEBP,50,baos);
        byte[] dadosImagemPostada = baos.toByteArray();

        //salvando no storage
        StorageReference storageReference = ConfiguracaoFirebase.getStorageReference();

        StorageReference imagemRef = storageReference.child("Imagens")
                .child("fotoPostada")
                .child(fotoPostada.getIdPostagem()+".jpeg");

        //passar um array de bytes no putbytes da imagem
        UploadTask uploadTask = imagemRef.putBytes(dadosImagemPostada);
        uploadTask.addOnFailureListener(e -> Toast.makeText(PostagemActivity.this, "Falha ao executar o comando para fazer upload da imagem.", Toast.LENGTH_SHORT).show()).
                addOnProgressListener(taskSnapshot -> {

                        }
                ).addOnSuccessListener(taskSnapshot -> {
            //recuperar local da foto
             taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                //recuperando local da foto postada
                fotoPostada.setImagemPostada(uri.toString());

                //recuperar quantidade de postagens
                int quantidadeFotosPostadas = usuarioLogado.getFotos() + 1;
                usuarioLogado.setFotos(quantidadeFotosPostadas);
                usuarioLogado.atualizarFotosPostadas();
/*
                DatabaseReference postagemRef = firebaseRef.child("Posts").child(usuarioLogado.getId());
                String postId = postagemRef.push().getKey();
                fotoPostada.setIdPostagem(postId);*/
                fotoPostada.setUsuario(usuarioLogado);

                //salvando a foto no banco de dados
                if (fotoPostada.salvarFotoPostada()){
                    //caso foto tenha sido postada com sucesso, atualizar numero de fotos postada
                    dialog.cancel();
                    Toast.makeText(getContext(), "Foto postada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();

                } else{
                    Toast.makeText(getContext(), "Erro ao postar foto. Verifique sua internet.", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    private void recuperarFiltros(){
        //limpando itens
        ThumbnailsManager.clearThumbs();
        listaFiltros.clear();

        //configurando filtro normal
        ThumbnailItem item = new ThumbnailItem();
        item.image = imagem;
        item.filterName = "Padrão";
        ThumbnailsManager.addThumb(item);

        //listando todos os filtros
        List<Filter> filtros = FilterPack.getFilterPack(getContext());
        for (Filter filtro:filtros){

            ThumbnailItem itemFiltro = new ThumbnailItem();
            itemFiltro.image = imagem;
            itemFiltro.filter = filtro;
            itemFiltro.filterName = filtro.getName();

            //setando configuração dos filtros gerais
            ThumbnailsManager.addThumb(itemFiltro);
        }

        //ThumbNailsManager processThumbs processa todas as miniaturas de filtros para layout que será criado
        listaFiltros.addAll(ThumbnailsManager.processThumbs(getContext()));
        adapterFiltros.notifyDataSetChanged();
    }


    public Context getContext(){
        return PostagemActivity.this;
    }

    public void botaoPostarFotoAcionar(){
        //botaoPostarFoto.setOnClickListener(v -> uploadFotoPostagem());
        botaoPostarFoto.setOnClickListener(v -> postarFoto());
    }


    private String getFileExtension(Uri uriExtensao){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uriExtensao));
    }

    private void abrirDialogCarregamento(String descricao){
       dialog = new ProgressDialog(getContext());
        dialog.setTitle("Aguarde.");
        dialog.setMessage(descricao);
        dialog.setIcon(R.drawable.ic_pets_black_24dp);
        dialog.setCancelable(false);
        dialog.show();

    }

 /*   @Override
    protected void onSaveInstanceState(Bundle oldInstanceState) {
        super.onSaveInstanceState(oldInstanceState);
        oldInstanceState.clear();
    }*/



    private void uploadFotoPostagem(){
     abrirDialogCarregamento("Sua petFoto está sendo postada! Aguarde...");

        if (uriImagemPostagem != null){
            StorageReference referenceFile =
                    storageReferencePostagem.child(System.currentTimeMillis()+"."+getFileExtension(uriImagemPostagem));

            uploadTaskPostagem = referenceFile.putFile(uriImagemPostagem);

            uploadTaskPostagem.continueWithTask((Continuation) task -> {

                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return referenceFile.getDownloadUrl();

            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if (task.isSuccessful()) {
                    Uri downloadUriImagem = task.getResult();
                    imagemUrlPostagem = downloadUriImagem.toString();

                    DatabaseReference postagemRef = firebaseRef.child("Posts");
                    String postId = postagemRef.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();

                    hashMap.put("idPostagem", postId);
                    hashMap.put("imagemPostada", imagemUrlPostagem);
                    hashMap.put("descricaoImagemPostada", descricaoInputTextPostagem.getText().toString());
                    hashMap.put("idUsuarioPostou", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    postagemRef.child("idPostagem").setValue(hashMap);

                    dialog.dismiss();

                    startActivity(new Intent(getContext(), StartActivity.class));
                    finish();
                }else{
                    Toast.makeText(getContext(), "Algo deu errado. Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }

            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show());
        }else{
            Toast.makeText(getContext(), "Nenhuma PetImagem selecionada", Toast.LENGTH_SHORT).show();
        }
    }

    //APOS CLICAR PARA SELECIONAR A FOTO

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            uriImagemPostagem = result.getUri();
            imagemSelecionadaPostagem.setImageURI(uriImagemPostagem);

        }else {
            Toast.makeText(this, "Algo deu errado. Verifique sua conexão com a Internet!", Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtros_postar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //definindo itens que foram selecionados

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_salvar_postagem_menu_postar:
                postarFoto();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}












