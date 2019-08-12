package com.example.dailyshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyshoppinglist.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private FloatingActionButton fab;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private RecyclerView recyclerView;

    private TextView total;

    //global var

    private  String type, amount, note, post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.home_toolbar);
        fab = findViewById(R.id.floatingActionButton);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daily Shopping List");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);

        recyclerView = findViewById(R.id.recycler_home);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //Total value

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalAmount = 0;

                for (DataSnapshot snap:dataSnapshot.getChildren() ){
                    Data data = snap.getValue(Data.class);
                    totalAmount += data.getAmount();
                    String stTotalAmount = String.valueOf(totalAmount);
                    total = findViewById(R.id.total_amount);
                    total.setText(stTotalAmount+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setMessage("Processing");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();
            }
        });
    }

    void customDialog(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View view = inflater.inflate(R.layout.input_data, null);
        final AlertDialog dialog = myDialog.create();
        dialog.setView(view);

        final EditText type = view.findViewById(R.id.edt_type);
        final EditText amount = view.findViewById(R.id.edt_amount);
        final EditText note = view.findViewById(R.id.edt_note);
        Button btn_save = view.findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                if(TextUtils.isEmpty(mType)||TextUtils.isEmpty(mAmount)||TextUtils.isEmpty(mNote)){
                    type.setError("Required field!");
                    note.setError("Required field!");
                    amount.setError("Required field!");
                    return;
                }

                int mAmtInt = Integer.parseInt(mAmount);
                String id = mDatabase.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(mType, mAmtInt, mNote, date, id);

                progressDialog.show();
                dialog.dismiss();
                mDatabase.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Data Added", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }else{
                            Toast.makeText(HomeActivity.this, "Data Not Added!!!", Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    }
                });
            }
        });

        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Data,MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(Data.class, R.layout.item_data, MyViewHolder.class, mDatabase) {


            @Override
            protected void populateViewHolder(final MyViewHolder myViewHolder, final Data data, final int i) {//'final' int modification
                myViewHolder.setAmount(data.getAmount());
                myViewHolder.setDate(data.getDate());
                myViewHolder.setType(data.getType());
                myViewHolder.setNote(data.getNote());

                myViewHolder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        type = data.getType();
                        amount = String.valueOf(data.getAmount());
                        note = data.getNote();
                        post_key = getRef(i).getKey();
                        updateData();
                    }
                });


            }
        };
        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View myView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }


        public void setType(String type){
            TextView mType = myView.findViewById(R.id.type);
            mType.setText(type);
        }
        public void setNote(String note){
            TextView mNote = myView.findViewById(R.id.note);
            mNote.setText(note);
        }
        public void setAmount(int amount){
            TextView mAmount = myView.findViewById(R.id.amount);
            String amt = String.valueOf(amount);
            mAmount.setText(amt);
        }
        public void setDate(String date){
            TextView mDate = myView.findViewById(R.id.date);
            mDate.setText(date);
        }
    }

    public void updateData(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        View v = inflater.inflate(R.layout.update_field, null);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.setView(v);

        final EditText updt_type = v.findViewById(R.id.updt_type);
        final EditText updt_amount = v.findViewById(R.id.updt_amount);
        final EditText updt_note = v.findViewById(R.id.updt_note);
        Button updt_btn = v.findViewById(R.id.btn_update);
        Button del_btn = v.findViewById(R.id.btn_delete);

        updt_amount.setText(amount);
        updt_amount.setSelection(amount.length());

        updt_type.setText(type);
        updt_type.setSelection(type.length());

        updt_note.setText(note);
        updt_note.setSelection(note.length());

        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setMessage("Processing...");

        updt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                type = updt_type.getText().toString().trim();
                note = updt_note.getText().toString().trim();
                amount = updt_amount.getText().toString().trim();
                int mAmount = Integer.parseInt(amount);
                String date = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(type, mAmount, note, date, post_key);
                mDatabase.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.hide();
                        }else{
                            progressDialog.hide();
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                mDatabase.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressDialog.hide();
                        }else{
                            progressDialog.hide();
                        }

                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
