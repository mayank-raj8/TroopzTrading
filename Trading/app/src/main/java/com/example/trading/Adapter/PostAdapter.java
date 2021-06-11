package com.example.trading.Adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trading.CommentsActivity;
import com.example.trading.Models.Post;
import com.example.trading.Models.User;
import com.example.trading.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mcontext;
    public List<Post> mPostList;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context mcontext, List<Post> mPostList) {
        this.mcontext = mcontext;
        this.mPostList = mPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.post_retreive_layout,parent,false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPostList.get(position);
        if(post.getPostimage() == null){
            holder.postImage.setVisibility(View.GONE);
        }
        holder.postImage.setVisibility(View.VISIBLE);
        Glide.with(mcontext).load(post.getPostimage()).into(holder.postImage);
        holder.expandable_text.setText(post.getPosttext());
        holder.postOnTextView.setText(post.getDate());

        publisherInformation(holder.postBy,post.getPublisher());

        isLike(post.getPostid(),holder.like);
        isDislike(post.getPostid(),holder.dislike);
        getlikes(holder.likes,post.getPostid());
        getDislikes(holder.dislikes,post.getPostid());

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like") && holder.dislike.getTag().equals("dislike")){
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                }
                else if (holder.like.getTag().equals("like") && holder.dislike.getTag().equals("disliked")){
                    FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);

                }else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.dislike.getTag().equals("dislike") && holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                }else if (holder.dislike.getTag().equals("dislike") && holder.like.getTag().equals("liked")){
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext, CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisher",post.getPublisher());
                mcontext.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext,CommentsActivity.class);
                intent.putExtra("postid",post.getPostid());
                intent.putExtra("publisher",post.getPublisher());
                mcontext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView more,postImage,like,dislike,comment,save;
        public TextView postOnTextView,likes,dislikes,comments,postBy;
        public ExpandableTextView expandable_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            likes = itemView.findViewById(R.id.likes);
            dislikes = itemView.findViewById(R.id.dislikes);
            comments = itemView.findViewById(R.id.comments);
            more = itemView.findViewById(R.id.more);
            postImage = itemView.findViewById(R.id.postImage);
            like = itemView.findViewById(R.id.like);
            dislike = itemView.findViewById(R.id.dislike);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            postOnTextView = itemView.findViewById(R.id.postOnTextView);
            expandable_text = itemView.findViewById(R.id.expand_text_view);
            postBy = itemView.findViewById(R.id.post_by_Textview);
        }
    }

    private void publisherInformation(final TextView postBy, String userid)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                postBy.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLike(String postid, ImageView imageView)
    {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isDislike(String postid, ImageView imageView)
    {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("dislikes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_disliked);
                    imageView.setTag("disliked");
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_dislike);
                    imageView.setTag("dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getlikes(TextView likes, String postid)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfLikes = snapshot.getChildrenCount();
                int NOL = (int) numberOfLikes;
                if(NOL > 1)
                {
                    likes.setText(snapshot.getChildrenCount()+ "likes");
                }
                else if(NOL == 0)
                {
                    likes.setText("0 likes");
                }
                else
                {
                    likes.setText(snapshot.getChildrenCount()+ "like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDislikes(TextView dislikes, String postid)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("dislikes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfDislikes = snapshot.getChildrenCount();
                int NOD = (int) numberOfDislikes;
                if(NOD > 1)
                {
                    dislikes.setText(snapshot.getChildrenCount()+ "dislikes");
                }
                else if(NOD == 0)
                {
                    dislikes.setText("0 dislikes");
                }
                else
                {
                    dislikes.setText(snapshot.getChildrenCount()+ "dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mcontext, error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
