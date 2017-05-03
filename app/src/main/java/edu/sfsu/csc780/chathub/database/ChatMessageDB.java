package edu.sfsu.csc780.chathub.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;

import edu.sfsu.csc780.chathub.DateUtil;
import edu.sfsu.csc780.chathub.model.ChatMessage;

/**
 * Created by rajatar08 on 4/30/17.
 */

@Table(database = BaseDatabase.class)
public class ChatMessageDB extends BaseModel {

    @Column
    @PrimaryKey
    private String uid;

    @Column
    private String text;

    @Column
    private String name;

    @Column
    private String photoUrl;

    @Column
    private String imageUrl;

    @Column
    private String date;

    public void insertData(String uid, ChatMessage message) {
        this.uid = uid;
        this.text = message.getText();
        this.name = message.getName();
        this.photoUrl = message.getPhotoUrl();
        this.imageUrl = message.getImageUrl();
        this.date = DateUtil.toLocalTime(message.getTimeStamp());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
