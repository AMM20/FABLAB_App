package martinez.alex.lopez.laura.upc.fablabapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ReservationListActivity extends AppCompatActivity {

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        RecyclerView reservationListView = findViewById(R.id.ReservationListView);

        reservationListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        reservationListView.setAdapter(adapter);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView clientHourView;
        private TextView clientView;

        private ImageButton detailsButton;

        public ViewHolder(View itemView) {
            super(itemView);

            clientHourView = findViewById(R.id.ClientHourView);
            clientView = findViewById(R.id.ClientView);
            detailsButton = findViewById(R.id.DetailsButton);

            detailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Intent intent = new Intent(this,ReservationDetailsActivity.class);

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

            //holder.clientHourView.setText();
            //holder.clientView.setText();

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
