package com.jccode.mycorrespondence.models;

public class ChildModel {

    String Attachment;
    String id;

    public ChildModel(String attachment, String id) {
        Attachment = attachment;
        this.id = id;
    }

    public String getAttachment() {
        return Attachment;
    }

    public String getId() {
        return id;
    }
}
