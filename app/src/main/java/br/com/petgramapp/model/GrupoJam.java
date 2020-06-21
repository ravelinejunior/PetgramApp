package br.com.petgramapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.List;

import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.helper.UsuarioFirebase;

public class GrupoJam implements Parcelable, Serializable {
    public static final Creator<GrupoJam> CREATOR = new Creator<GrupoJam>() {
        @Override
        public GrupoJam createFromParcel(Parcel in) {
            return new GrupoJam(in);
        }

        @Override
        public GrupoJam[] newArray(int size) {
            return new GrupoJam[size];
        }
    };
    private String idGrupo;
    private String nomeGrupo;
    private String fotoGrupo;
    private String idAdminGrupo;
    private List<Usuario> membrosGrupo;

    public GrupoJam() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference gruposRef = databaseReference.child("Grupo");
        String idFirebaseGrupos = gruposRef.push().getKey();
        setIdGrupo(idFirebaseGrupos);

    }

    public GrupoJam(String idGrupo, String nomeGrupo, String fotoGrupo, String idAdminGrupo, List<Usuario> membrosGrupo) {
        this.idGrupo = idGrupo;
        this.nomeGrupo = nomeGrupo;
        this.fotoGrupo = fotoGrupo;
        this.idAdminGrupo = idAdminGrupo;
        this.membrosGrupo = membrosGrupo;
    }

    protected GrupoJam(Parcel in) {
        idGrupo = in.readString();
        nomeGrupo = in.readString();
        fotoGrupo = in.readString();
        idAdminGrupo = in.readString();
        membrosGrupo = in.createTypedArrayList(Usuario.CREATOR);
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNomeGrupo() {
        return nomeGrupo;
    }

    public void setNomeGrupo(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public String getFotoGrupo() {
        return fotoGrupo;
    }

    public void setFotoGrupo(String fotoGrupo) {
        this.fotoGrupo = fotoGrupo;
    }

    public List<Usuario> getMembrosGrupo() {
        return membrosGrupo;
    }

    public void setMembrosGrupo(List<Usuario> membrosGrupo) {
        this.membrosGrupo = membrosGrupo;
    }

    public String getIdAdminGrupo() {
        return idAdminGrupo;
    }

    public void setIdAdminGrupo(String idAdminGrupo) {
        this.idAdminGrupo = idAdminGrupo;
    }


    //METODO PARA SALVAR O GRUPO
    public void salvarGrupoFirebase() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getReferenciaDatabase();
        DatabaseReference gruposRef = databaseReference.child("Grupo");
        gruposRef.child(getIdGrupo())
                .setValue(this);
    }

    public void salvarGrupoFirestore(){
        FirebaseFirestore firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        firebaseFirestore.collection("Grupo")
                .document(getIdGrupo()).set(this);

        //para salvar a conversa para membros do grupo
        for (Usuario usuario: getMembrosGrupo()){

            String idRemetente = usuario.getId();
            String idDestinatario = getIdGrupo();

            Conversas conversas = new Conversas();
            conversas.setIdRemetente(idRemetente);
            conversas.setIdDestinatario(idDestinatario);
            conversas.setUltimaMensagem("Mensagem do grupo "+getNomeGrupo());
            conversas.setIsGroup("true");
            conversas.setGrupoJam(this);

            conversas.salvarConversa();
            conversas.salvarConversaOutroUsuario(UsuarioFirebase.getUsuarioLogado());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idGrupo);
        dest.writeString(nomeGrupo);
        dest.writeString(fotoGrupo);
        dest.writeString(idAdminGrupo);
        dest.writeTypedList(membrosGrupo);
    }
}
