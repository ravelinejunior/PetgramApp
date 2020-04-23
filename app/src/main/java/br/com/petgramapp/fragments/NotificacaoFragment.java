package br.com.petgramapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.petgramapp.R;
import br.com.petgramapp.adapter.AdapterNotificacao;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;
import br.com.petgramapp.model.Notificacao;


public class NotificacaoFragment extends Fragment {

    private RecyclerView recyclerViewNotificacao;
    private AdapterNotificacao adapterNotificacao;
    private List<Notificacao> notificacaoList;

    public NotificacaoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_notificacao, container, false);
        carregarElementos(view);
        recyclerViewNotificacao.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewNotificacao.setLayoutManager(linearLayoutManager);

        notificacaoList = new ArrayList<>();
        adapterNotificacao = new AdapterNotificacao(getContext(),notificacaoList);
        recyclerViewNotificacao.setAdapter(adapterNotificacao);

        readNotificacao();

        return view;
    }

    private void readNotificacao() {
        FirebaseUser firebaseUser = UsuarioFirebase.getUsuarioAtual();
        DatabaseReference notificacaoRef = ConfiguracaoFirebase.getReferenciaDatabase().child("Notificacao")
                .child(firebaseUser.getUid());
        notificacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificacaoList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Notificacao notificacao = ds.getValue(Notificacao.class);
                    notificacaoList.add(notificacao);
                }
                Collections.reverse(notificacaoList);
                adapterNotificacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void carregarElementos(View view){
        recyclerViewNotificacao = view.findViewById(R.id.recyclerView_NotificacaoFragment);
    }
}
