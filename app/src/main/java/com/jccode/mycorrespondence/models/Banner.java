package com.jccode.mycorrespondence.models;

public class Banner {

    private String image;
    private String id;



    public String getImage() {
        return image;
    }

    public String getId() {
        return id;
    }

    public Banner(String image, String id) {
        this.image = image;
        this.id = id;
    }
}
