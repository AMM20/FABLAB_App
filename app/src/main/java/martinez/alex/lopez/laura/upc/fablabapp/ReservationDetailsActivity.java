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

    // Declaració de les variables lligades al layout de l'activitat.
    private TextView clientView, phoneView, emailView, reservationDateView, reservationHourView, projectUseView, serviceTypeView, materialView, thicknessView, totalCostView, notesView;

    // Declaració de les variables lligades a la reserva seleccionada.
    private Reserva reserva;
    private String docID, reservationID;

    // Declaració d'una instància de FirebaseFirestore.
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_details);

        // Es recuperen les dades que s'han transferit des de l'activitat ReservationListActivity a partir de l'intent.
        Intent intent = getIntent();
        reserva = (Reserva) intent.getSerializableExtra("reserva");
        docID = intent.getStringExtra("docID");
        reservationID = reserva.getReservationID();

        // S'obtenen les referències als objectes del layout.
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

        // Es crida al métode OmpleDetallsReserva()
        OmpleDetallsReserva();

    }

    // Creació d'un menú situat a la barra d'accions de l'activitat. Aquest menú segueix l'estructura del layout menu_reservation_details.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reservation_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_reservation: // Quan es selecciona l'icona d'esborrar una reserva, apareix un quadre de diàleg.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.Delete_Reservation_Message)
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() { // Cas: Es confirma l'eliminació de la reserva.
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("reservas").document(docID).collection("turnos").document(reservationID) // S'accedeix al document de la base de dades que es correspon a la reserva escollida.
                                        .delete() // Eliminació del document corresponent a la reserva.
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) { // Un cop eliminada la reserva, es mostra un quadre de diàleg per a informar de la correcta eliminació del document.
                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ReservationDetailsActivity.this);
                                                builder2.setMessage(R.string.Deleted_Reservation_Message)
                                                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) { //Al clicar al botó OK, es crea un nou intent i se li assigna un RESULT_OK.
                                                                Intent data = new Intent();
                                                                setResult(RESULT_OK,data);
                                                                finish(); // Es tanca l'activitat i es crida a l'activitat anterior (ReservationListActivity).
                                                            }
                                                        });
                                                AlertDialog dialog2 = builder2.create();
                                                dialog2.show(); // Es mostra el segon quadre de diàleg.
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() { // Em el cas de que falli l'eliminació del document, es mostra un error al Log.
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("dbError",e.toString());
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(R.string.CANCEL, null);
                AlertDialog dialog = builder.create();
                dialog.show(); // Es mostra el primer quadre de diàleg.

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // El métode OmpleDetallsReserva() recupera les dades de la reserva seleccionada i les mostra en els camps corresponents del layout de l'activitat.

    private void OmpleDetallsReserva () {

        clientView.setText(reserva.getClient().getName() + " " + reserva.getClient().getLastName());
        phoneView.setText(String.valueOf(reserva.getClient().getPhone())); // Ja que el camp Phone es tracta d'un Integer, cal convertir-lo en un String.
        emailView.setText(reserva.getClient().getEmail());
        notesView.setText(reserva.getClient().getNotes());
        // Degut a que la data de la reserva és del tipus Date, cal convertir-la a un String per a poder mostrar-la per pantalla.
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String reservationDate = dateFormat.format(reserva.getDate());
        reservationDateView.setText(reservationDate);
        reservationHourView.setText(getReservedTurn(reserva)); // Es critda al mètode getReservedTurn, el qual ens retorna l'hora d'inici i final de la reserva.
        projectUseView.setText(reserva.getProjectUse());
        serviceTypeView.setText(reserva.getServiceType());
        materialView.setText(reserva.getMaterial());
        thicknessView.setText(reserva.getThickness());
        totalCostView.setText(reserva.getTotalCost());

    }

    // Aquest mètode agafa l'Array de Stings del camp ReservedHours de l'objecte Reserva i retorna l'hora d'inici i fi en un únic String.
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
