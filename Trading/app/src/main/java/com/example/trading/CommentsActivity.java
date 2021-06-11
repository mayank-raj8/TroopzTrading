package com.example.trading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trading.Models.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textView;
    private EditText editText;

    private ProgressDialog loader;

    String postid;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");

        recyclerView = findViewById(R.id.recycler_view);
        textView = findViewById(R.id.commenting_post_textview);
        editText = findViewById(R.id.adding_comment);
        loader = new ProgressDialog(this);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editText.getText().toString();
                if(TextUtils.isEmpty(commentText)){
                    editText.setError("please type something");
                }
                else
                {
                    addcomment();
                }
            }
        });
        commentList = new ArrayList<>();
        commentAdapter =new CommentAdapter(CommentsActivity.this,commentList,postid);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(commentAdapter);
        

        readComments();
    }

    private void addcomment(){
        loader.setMessage("Adding a comment");
        loader.setCanceledOnTouchOutside(false);
        loader.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        String commentid = reference.push().getKey();
        String date = DateFormat.getDateInstance().format(new Date());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment",editText.getText().toString());
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("commentid",commentid);
        hashMap.put("postid",postid);
        hashMap.put("date",date);

        reference.child(commentid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CommentsActivity.this,"comment added successfully",Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
                else
                {
                    Toast.makeText(CommentsActivity.this,"Error adding comment" + task.getException(),Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }
               editText.setText("");
            }
        });

    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}