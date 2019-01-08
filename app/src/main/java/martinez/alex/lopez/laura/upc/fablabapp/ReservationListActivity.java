package martinez.alex.lopez.laura.upc.fablabapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

    private Adapter adapter;

    private TextView chosenDate;

    private int year, month, day;
    private Date date;
    private final Calendar calendar = Calendar.getInstance();

    private List<Reserva> reservationList;
    private Reserva reserva;
    private Client client;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference resRef;
    private String docID;
    private String reservationID;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        RecyclerView reservationListView = findViewById(R.id.ReservationListView);
        chosenDate = findViewById(R.id.ChosenDate);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        String sactualdate = Integer.toString(day) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(year);
        chosenDate.setText(sactualdate);
        try {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(sactualdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        reservationList = new ArrayList<>();

        reservationListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        reservationListView.setAdapter(adapter);

    }

    public void onClickSearchReservations(View view) {
        setDocID();
        CercaReserves();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClickChooseDate(View view) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String sdate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                chosenDate.setText(sdate);
                try {
                    date = new SimpleDateFormat("dd/MM/yyyy").parse(sdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setDocID();
                //CercaReserves();
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void setDocID() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String sdate = dateFormat.format(date);
        String[] dateArray = sdate.split("/");
        docID = dateArray[2] + dateArray[1] + dateArray[0];
    }

    private void CercaReserves() {

        db.collection("reservas").document(docID).collection("turnos")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        reservationList =  new ArrayList<>();
                        for (DocumentSnapshot doc : documentSnapshots) {
                            OmpleReserva(doc);
                            reservationList.add(reserva);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("dbError",e.toString());
                    }
                });
    }

    private void OmpleReserva(DocumentSnapshot doc) {
        reserva = new Reserva();
        client = new Client();

        client.setName(doc.getString("name"));
        client.setLastName(doc.getString("lastName"));
        client.setEmail(doc.getString("email"));
        client.setPhone(doc.getLong("phone").intValue());
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

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView clientHourView;
        private TextView clientView;

        private ImageView itemBackgroundView;

        public ViewHolder(View itemView) {
            super(itemView);

            clientHourView = itemView.findViewById(R.id.ClientHourView);
            clientView = itemView.findViewById(R.id.ClientView);
            itemBackgroundView = itemView.findViewById(R.id.ItemBackgroundView);

            itemBackgroundView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int pos = getAdapterPosition();
                    Intent intent = new Intent(ReservationListActivity.this, ReservationDetailsActivity.class);
                    intent.putExtra("reserva", reservationList.get(pos));
                    intent.putExtra("docID",docID);
                    intent.putExtra("reservationID",reservationID);
                    startActivityForResult(intent,0);
                }
            });
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View reservationListView = getLayoutInflater().inflate(R.layout.view_reservation_list,parent,false);
            return new ViewHolder(reservationListView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Reserva res = reservationList.get(position);

            String reservedTurn = getReservedTurn(res);

            holder.clientHourView.setText(reservedTurn);
            holder.clientView.setText(res.getClient().getName());

        }

        @Override
        public int getItemCount() {
            return reservationList.size();
        }
    }

    @NonNull
    private String getReservedTurn(Reserva res) {
        String turnStart = res.getReservedHours().get(0);
        String turnEnd;
        String[] hour = res.getReservedHours().get(res.getReservedHours().size()-1).split(":");
        int endHour = Integer.parseInt(hour[0]);
        int endMin = Integer.parseInt(hour[1]);

        if (endMin + 15 == 60) {
            endHour++;
            turnEnd = String.valueOf(endHour) + ":00";
        } else {
            endMin = endMin +15;
            turnEnd = String.valueOf(endHour) + ":" + String.valueOf(endMin);
        }
        return turnStart + " - " + turnEnd;
    }
}
