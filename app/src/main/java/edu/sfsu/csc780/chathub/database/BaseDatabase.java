package edu.sfsu.csc780.chathub.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by rajatar08 on 4/30/17.
 * Base Database class for DBFlow
 */

@Database(name = BaseDatabase.NAME, version = BaseDatabase.VER)
public class BaseDatabase {

    public static final String NAME = "messages";
    public static final int VER = 1;


    public String getDBName() {
        return NAME;
    }
}
