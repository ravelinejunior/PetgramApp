package br.com.petgramapp.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;

import br.com.petgramapp.BuildConfig;
import br.com.petgramapp.R;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.MensagemJam;

public class AdapterMensagensJam extends RecyclerView.Adapter<AdapterMensagensJam.MyViewHolder> {

    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;
    RecyclerView recyclerView;
    private Context context;
    private List<MensagemJam> mensagemJamList;



    public AdapterMensagensJam(Context context, List<MensagemJam> mensagemJamList, RecyclerView recyclerView) {
        this.context = context;
        this.mensagemJamList = mensagemJamList;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (viewType == TIPO_REMETENTE) {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.adapter_mensagemjam_remetente, parent, false);
        } else if (viewType == TIPO_DESTINATARIO) {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.adapter_mensagemjam_destinatario, parent, false);
        }

        return new MyViewHolder(view);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MensagemJam mensagemJam = mensagemJamList.get(position);
        Uri uriImagemDownload = Uri.parse(mensagemJam.getImagemEnviada());
        ImageView imageView = holder.imagemTalks;
        String nomeImagem = mensagemJam.getImagemEnviada();
        Integer PERMISSION_WRITE_EXTERNAL = 1000;
        try {
            holder.setIsRecyclable(false);
            if (mensagemJam.getImagemEnviada() != null
                    && mensagemJam.getMensagem().equalsIgnoreCase("imagem.jpeg")) {

                Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
                Glide.with(context).load(uriImagem).into(holder.imagemTalks);
                holder.nomeUsuarioTalks.setText("Enviada por " + mensagemJam.getNomeUsuarioEnviou());
                holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
                //holder.mensagemTalks.setText(mensagemJam.getMensagem());
                holder.mensagemTalks.setVisibility(View.GONE);

            } else if (mensagemJam.getImagemEnviada().equals("")) {
                //Uri uriImagem = Uri.parse(mensagemJam.getImagemEnviada());
                //Glide.with(context).load(R.drawable.ic_pets_white_24dp).into(holder.imagemTalks);
                holder.nomeUsuarioTalks.setText("Enviada por " + mensagemJam.getNomeUsuarioEnviou());
                holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
                holder.mensagemTalks.setText(mensagemJam.getMensagem());
                holder.imagemTalks.setVisibility(View.GONE);
            } else {
                holder.nomeUsuarioTalks.setText("Enviada por " + mensagemJam.getNomeUsuarioEnviou());
                holder.dataEnvioRecebido.setText(mensagemJam.getDataEnvio());
                holder.mensagemTalks.setText(mensagemJam.getMensagem());
                holder.imagemTalks.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao carregar mensagens.", Toast.LENGTH_SHORT).show();
        }


        holder.imagemTalks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Baixar ou compartilhar");
                builder.setMessage("Selecione a opção para fazer download ou compartilhar a imagem.");
                builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_GRANTED) {
                                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                startDownloading(uriImagemDownload);
                            } else {
                                startDownloading(uriImagemDownload);
                            }
                        } else {
                            startDownloading(uriImagemDownload);

                        }
                    }
                });

                builder.setNegativeButton("Compartilhar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shareContent(imageView,nomeImagem);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }



    //metodo para compartilhar imagem
    private void shareContent(ImageView imageView,String imagemNome){
        Bitmap bitmap = getBitmapFromView(imageView);
        try{
            File file = new File(context.getExternalCacheDir(),"petGram.jpeg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            file.setReadable(true,true);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT,imagemNome);
            intent.setType("imagem/jpeg");
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(context),BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            context.startActivity(Intent.createChooser(intent,"Compartilhar via"));
        }catch (Exception e){
            Toast.makeText(context, "Erro ao compartilhar imagem.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //recuperar o bitmap antes de compartilhar
    private Bitmap getBitmapFromView(View view){
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable imageDrawable = view.getBackground();
        if (imageDrawable != null){
            imageDrawable.draw(canvas);
        }else{
            canvas.drawColor(Color.WHITE);
        }

        view.draw(canvas);
        return bitmap;
    }

    //metodo para baixar imagem
    private void startDownloading(Uri uriImagem) {
        String url = uriImagem.toString().trim();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("Download");
        request.setDescription("Baixando ...");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,System.currentTimeMillis()+".jpeg");

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

    }

    @Override
    public int getItemViewType(int position) {
        MensagemJam mensagemJam = mensagemJamList.get(position);
        String idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        if (idUsuarioLogado.equals(mensagemJam.getId())) {
            return TIPO_REMETENTE;
        }

        return TIPO_DESTINATARIO;
    }

    @Override
    public int getItemCount() {
        return mensagemJamList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mensagemTalks;
        public TextView nomeUsuarioTalks;
        public TextView dataEnvioRecebido;
        public ImageView imagemTalks;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeUsuarioTalks = itemView.findViewById(R.id.nome_id_usuario_AdapterMensagem);
            mensagemTalks = itemView.findViewById(R.id.mensagemDigitada_AdapterMensagem);
            dataEnvioRecebido = itemView.findViewById(R.id.horaMensagem_id_Mensagem);
            imagemTalks = itemView.findViewById(R.id.imagem_MensagemTalks);

        }
    }

}
