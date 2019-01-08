package martinez.alex.lopez.laura.upc.fablabapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ReservationDetailsActivity extends AppCompatActivity {

    private TextView clientView;
    private TextView phoneView;
    private TextView emailView;
    private TextView reservationDateView;
    private TextView reservationHourView;
    private TextView projectUseView;
    private TextView serviceTypeView;
    private TextView materialView;
    private TextView thicknessView;
    private TextView totalCostView;
    private TextView notesView;

    private Reserva reserva;
    private String docID;
    private String reservationID;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_details);

        Intent intent = getIntent();
        reserva = (Reserva) intent.getSerializableExtra("reserva");
        docID = intent.getStringExtra("docID");
        reservationID = intent.getStringExtra("reservationID");

        clientView = findViewById(R.id.ClientView);
        phoneView = findViewById(R.id.PhoneView);
        emailView = findViewById(R.id.EmailView);
        reservationDateView = findViewById(R.id.ReservationDateView);
        reservationHourView = findViewById(R.id.ReservationHourView);
        projectUseView = findViewById(R.id.ProjectUseView);
        serviceTypeView = findViewById(R.id.ServiceTypeView);
        materialView = findViewById(R.id.MaterialView);
        thicknessView = findViewById(R.id.ThicknessView);
        totalCostView = findViewById(R.id.TotalCostView);
        notesView = findViewById(R.id.NotesView);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);

        OmpleDetallsReserva();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reservation_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_reservation:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.Delete_Reservation_Message)
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("reservas").document(docID).collection("turnos").document(reservationID)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ReservationDetailsActivity.this);
                                                builder2.setMessage(R.string.Deleted_Reservation_Message)
                                                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                finish();
                                                            }
                                                        });
                                                AlertDialog dialog2 = builder2.create();
                                                dialog2.show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("dbError",e.toString());
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void OmpleDetallsReserva () {

        clientView.setText(reserva.getClient().getName() + " " + reserva.getClient().getLastName());
        phoneView.setText(reserva.getClient().getPhone());
        emailView.setText(reserva.getClient().getEmail());
        notesView.setText(reserva.getClient().getNotes());

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String reservationDate = dateFormat.format(reserva.getDate());
        reservationDateView.setText(reservationDate);
        reservationHourView.setText(getReservedTurn(reserva));
        projectUseView.setText(reserva.getProjectUse());
        serviceTypeView.setText(reserva.getServiceType());
        materialView.setText(reserva.getMaterial());
        thicknessView.setText(reserva.getThickness());
        totalCostView.setText(reserva.getTotalCost());

    }

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
