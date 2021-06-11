package com.example.trading.Models;

public class Post {
    private String postid,posttext,date,publisher,postimage;

    public Post(){

    }

    public Post(String postid, String posttext, String date, String publisher, String postimage) {
        this.postid = postid;
        this.posttext = posttext;
        this.date = date;
        this.publisher = publisher;
        this.postimage = postimage;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPosttext() {
        return posttext;
    }

    public void setPosttext(String posttext) {
        this.posttext = posttext;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }
}

