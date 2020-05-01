package br.com.petgramapp.fragments;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.PostagemActivity;
import br.com.petgramapp.activities.PostagemCamera;
import br.com.petgramapp.helper.Permissao;

public class PostarFragment extends Fragment {

    //request code para a ação de onclick de fotos
    private final static int CODIGO_ABRIR_GALERIA = 200;
    private final static int CODIGO_ABRIR_CAMERA = 100;
    private Button botaoGaleria;
    private Button botaoCamera;

    //lista de permissões
    private final String[] listaPermissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public PostarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_postar, container, false);
        botaoCamera = view.findViewById(R.id.botao_camera_Postar);
        botaoGaleria = view.findViewById(R.id.botao_galeria_Postar);

        //validando as permissoes
        Permissao.validarPermissoes(listaPermissoesNecessarias,getActivity(),1);

        botaoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getContext(),PostagemCamera.class);
                startActivity(i);
            }
        });


        botaoGaleria.setOnClickListener(v -> {

            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (i.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager())!= null){
                startActivityForResult(i,CODIGO_ABRIR_GALERIA);
            }

        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap imagem = null;

        //valida o tipo de seleção de imagem
        try {

            if (requestCode == CODIGO_ABRIR_GALERIA) {
                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), localImagemSelecionada);

                //validar imagem selecionada
                if (imagem != null){

                    //converter imagem em byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.WEBP,80,baos);
                    byte[] dadosFoto = baos.toByteArray();

                    //enviar imagem para tela de filtros
                    Intent i = new Intent(getActivity(), PostagemActivity.class);
                    i.putExtra("fotoSelecionada",dadosFoto);
                    startActivity(i);

                }
            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
