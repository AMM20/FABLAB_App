package martinez.alex.lopez.laura.upc.fablabapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReservationListActivity extends AppCompatActivity {

    // Declaració de la variable Adapter utilitzada en el Recycler View.
    private Adapter adapter;

    // Declaració de les variables lligades al layout de l'activitat.
    private TextView chosenDate;

    // Declaració de les variables utilitzades per a mostrar la data actual i l'escollida al DatePickerDialog.
    private int year, month, day;
    private Date date;
    private final Calendar calendar = Calendar.getInstance();

    // Declaració d'objectes del model.
    private List<Reserva> reservationList;
    private Reserva reserva;
    private Client client;

    // Declaració d'una instància de FirebaseFirestore.
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaració de la variable lligada a la data de  reserva seleccionada.
    private String docID;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        // S'obtenen les referències als objectes del layout.
        RecyclerView reservationListView = findViewById(R.id.ReservationListView);
        chosenDate = findViewById(R.id.ChosenDate);

        // Es guarda la data actual dins de les variables year, month i day.
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // Per tal d'actualitzar el valor de chosenDate que es mostra per pantalla, cal crear un String utilitzant les variables year, month i day, de tipus int.
        String sactualdate = Integer.toString(day) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(year);
        chosenDate.setText(sactualdate);
        try {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(sactualdate); // Dins de la variable date es guarda la data actual en format Date, utilitzant el patró dd/MM/yyyy.
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Creació d'una llista de reserves buida.
        reservationList = new ArrayList<>();

        // Crida inicial als mètodes setDocID() i CercaReserves() per a actulitzar la pantall amb la data actual.
        setDocID();
        CercaReserves();

        // Configuració del RecyclerView amb un LayoutManager i un Adapter.
        reservationListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        reservationListView.setAdapter(adapter);

    }

    // Aquest mètode es crida quan es prem el botó CalendarButton.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClickChooseDate(View view) {

        // Creació d'un quadre de diàleg amb un calendari en el qual es pot seleccionar la data desitjada.
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Un cop s'escull la data, s'actualitza el text del TextView chosenDate.
                String sdate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                chosenDate.setText(sdate);
                try {
                    date = new SimpleDateFormat("dd/MM/yyyy").parse(sdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // Es torna a cridar als mètodes setDocID() i CercaReserves() cada vegada que es selecciona una data.
                setDocID();
                CercaReserves();
            }
        }, year, month, day);
        datePickerDialog.show();

    }

    // Aquest métode retorna l'ID del document en format YYYYMMDD, per tal de facilitar el seu filtratge i ordenació a la base de dades.
    private void setDocID() {

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String sdate = dateFormat.format(date);
        String[] dateArray = sdate.split("/");
        docID = dateArray[2] + dateArray[1] + dateArray[0];

    }

    // Aquest mètode recupera totes les reserves corresponents a una data determinada, marcada pel docID.
    private void CercaReserves() {

        db.collection("reservas").document(docID).collection("turnos")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        reservationList =  new ArrayList<>(); // Es torna a crear una llista de reserves buida.
                        for (DocumentSnapshot doc : documentSnapshots) { // Es recuperen tots els documents d'una data determinada
                            OmpleReserva(doc); // Crida al mètode OmpleReserva()
                            reservationList.add(reserva); // S'afegeix la reserva actual a la llista de reserves.
                        }
                        adapter.notifyDataSetChanged(); // Es notifica a l'adaptador de que s'han actualitzat les dades de reservationList.
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(ReservationListActivity.this);
                        builder.setMessage(R.string.error_reading_database_message);

                        builder.setPositiveButton(R.string.try_again, null);

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        Log.d("dbError",e.toString());

                    }
                });

    }

    // Aquest mètode recupera els camps d'un document corresponent a una reserva d'una data determinada i els carrega dins d'un objecte de la classe Reserva del model.
    private void OmpleReserva(DocumentSnapshot doc) {

        reserva = new Reserva();
        client = new Client();

        client.setName(doc.getString("name"));
        client.setLastName(doc.getString("lastName"));
        client.setEmail(doc.getString("email"));
        client.setPhone(doc.getLong("phone").intValue()); // Ja que la base de dades ens retorna un objecte de tipus Long, cal convertir-lo a un String per a guardar-lo dins de l'objecte client.
        client.setNotes(doc.getString("notes"));

        reserva.setClient(client);
        reserva.setDate(date);
        reserva.setReservedHours((List<String>) doc.get("reservedHours"));
        reserva.setProjectUse(doc.getString("projectUse"));
        reserva.setServiceType(doc.getString("serviceType"));
        reserva.setMaterial(doc.getString("material"));
        reserva.setThickness(doc.getString("thickness"));
        reserva.setTotalCost(doc.getString("totalCost"));
        reserva.setReservationID(doc.getString("reservationID"));

    }

    // Aquest mètode es crida quan una altra activitat fa una crida a ReservationListActivity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 0: // Cas: Es realitza una crida desde ReservationDetailsActivity.
                if (resultCode == RESULT_OK) // Si s'ha cridat a travès del mètode corresponent a l'eliminació d'una reserva.
                {
                    // S'actualtiza la llista de reserves.
                    setDocID();
                    CercaReserves();
                }
                break;
            default: // En cas contrari (s'ha clicat el botó Back del telèfon, per exemple) no es realitza cap acció.
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // El ViewHolder manté referències a les parts de l'itemView que canvien quan el reciclem.
    class ViewHolder extends RecyclerView.ViewHolder {

        // Declaració de les variables lligades al layout de l'itemView.
        private TextView clientHourView, clientView;
        private ImageView itemBackgroundView;

        public ViewHolder(View itemView) { // Es rep l'itemView al constructor

            super(itemView);

            // Referències als objectes que hi ha dins de l'itemView.
            clientHourView = itemView.findViewById(R.id.ClientHourView);
            clientView = itemView.findViewById(R.id.ClientView);
            itemBackgroundView = itemView.findViewById(R.id.ItemBackgroundView);

            // S'afegeix un OnClickListener que crida a l'activitat ReservationDetailsActivity i li passa les dades de la reserva seleccionada.
            itemBackgroundView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int pos = getAdapterPosition();
                    Intent intent = new Intent(ReservationListActivity.this, ReservationDetailsActivity.class);
                    intent.putExtra("reserva", reservationList.get(pos));
                    intent.putExtra("docID",docID);
                    startActivityForResult(intent,0);
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            // Creació d'un item de la pantalla a partir del layout view_reservation_list.
            View reservationListView = getLayoutInflater().inflate(R.layout.view_reservation_list,parent,false);
            // Es crea i es retorna el ViewHolder associat.
            return new ViewHolder(reservationListView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            // Dins d'un objecte de la classe Reserva del nostre model, s'obté el valor de la reserva en la posició que es passa al mètode.
            Reserva res = reservationList.get(position);
            // També s'obté l'hora d'inici i fi de la reserva a través del mètode getReservedTurn.
            String reservedTurn = getReservedTurn(res);
            // S'introdueix el text pertinent a cada TextView de l'itemView.
            holder.clientHourView.setText(reservedTurn);
            holder.clientView.setText(res.getClient().getName() + " " + res.getClient().getLastName());

        }

        @Override
        public int getItemCount() {
            return reservationList.size();
        }
    }


    // Aquest mètode agafa l'Array de Stings del camp ReservedHours de l'objecte Reserva i retorna l'hora d'inici i fi en un únic String.
    @NonNull
    private String getReservedTurn(Reserva res) {
        String turnStart = res.getReservedHours().get(0); // L'hora d'inici es correspon amb el primer element de l'Array de Strings.
        String turnEnd;
        String[] hour = res.getReservedHours().get(res.getReservedHours().size()-1).split(":"); // L'últim element de l'Array es correspon amb l'hora d'inici de l'últim torn reservat.
        int endHour = Integer.parseInt(hour[0]);
        int endMin = Integer.parseInt(hour[1]);
        // Per tal d'obtenir l'hora d'acabament de la reserva, nomès cal sumar-li un quart d'hora a endMin. Es distingeix entre dos casos.
        if (endMin + 15 == 60) {
            endHour++;
            turnEnd = String.valueOf(endHour) + ":00";
        } else {
            endMin = endMin +15;
            turnEnd = String.valueOf(endHour) + ":" + String.valueOf(endMin);
        }
        // Es retorna un únic String
        return turnStart + " - " + turnEnd;
    }
}
