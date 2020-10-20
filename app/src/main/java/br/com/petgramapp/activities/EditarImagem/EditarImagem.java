package br.com.petgramapp.activities.EditarImagem;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.imageprocessors.Filter;

import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.fragments.EditarImage.FiltersListFragment;
import br.com.petgramapp.interfaces.FiltersListFragmentListener;
import br.com.petgramapp.novos_adapter.ViewPagerAdapter;
import br.com.petgramapp.utils.BitmapUtils;

public class EditarImagem extends AppCompatActivity implements FiltersListFragmentListener {

    public static final int PERMISSION_PICK_IMAGE = 1000;
    public static String pictureName = "flash.jpg";

    //load native image filters
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    ImageView imagePreview;
    TabLayout tabLayout;
    ViewPager viewPager;
    CoordinatorLayout coordinatorLayout;
    Bitmap originalBitmap;
    Bitmap filteredBitmap;
    Bitmap finalBitmap;
    FiltersListFragment filtersListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);

        Toolbar toolbar = findViewById(R.id.toolbarEditImage);
        toolbar.setTitle("Petgram Filters");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        loadElementos();
        //carregar imagens
        loadImages();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void loadImages() {

        //recuperando imagem da tela de fragment
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            byte[] dadosFoto = bundle.getByteArray("fotoSelecionada");
            //permitir editar byte array
            if (dadosFoto != null) {
                originalBitmap = BitmapFactory.decodeByteArray(dadosFoto,0,dadosFoto.length);
            } else {
                Toast.makeText(this, "Dados foto vazio ou muito grande.", Toast.LENGTH_SHORT).show();
            }
        }

        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(originalBitmap);
    }

    //configuração do view pager
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);


        adapter.addFragment(filtersListFragment, "FILTERS");


        viewPager.setAdapter(adapter);
    }

    private void loadElementos() {
        imagePreview = findViewById(R.id.image_preview);
        tabLayout = findViewById(R.id.tabLayout_ContentMain);
        viewPager = findViewById(R.id.nonSwipebleViewPager);
        coordinatorLayout = findViewById(R.id.activity_principal_imagens);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(filter.processFilter(filteredBitmap));
        finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filtros_postar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.ic_salvar_postagem_menu_postar:
                openImageFromGalery();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            try {
                                final String path = BitmapUtils.insertImage(
                                        getContentResolver(),
                                        finalBitmap,
                                        System.currentTimeMillis() + ".jpeg",
                                        null
                                );

                                if (!TextUtils.isEmpty(path)) {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                            "Imagem salva na galeria",
                                            Snackbar.LENGTH_SHORT).setAction("Open", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            openImage(path);
                                        }
                                    });
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                            "Imagem salva na galeria",
                                            Snackbar.LENGTH_SHORT);
                                    snackbar.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(EditarImagem.this, "Permissão negada!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                });
    }

    private void openImage(String path) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path),"image/*");
        startActivity(intent);

    }

    private void openImageFromGalery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, PERMISSION_PICK_IMAGE);
                        } else {
                            Toast.makeText(EditarImagem.this, "Permissão negada!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PERMISSION_PICK_IMAGE) {
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

            //limpar memoria bitmap
            originalBitmap.recycle();
            filteredBitmap.recycle();
            finalBitmap.recycle();

            originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            filteredBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(originalBitmap);
            bitmap.recycle();

            //renderizar imagem thumbnail selecionada
           // filtersListFragment.displayThumbail(originalBitmap);

            //FIX CRASH FILTERLIST
            filtersListFragment = FiltersListFragment.getInstance(originalBitmap);
            filtersListFragment.setListener(this);

        }
    }
}









