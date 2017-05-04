package edu.sfsu.csc780.chathub.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.MessageUtil;
import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.database.ChatMessageDB;
import edu.sfsu.csc780.chathub.model.ChatMessage;

/**
 * Created by rajatar08 on 4/26/17.
 *
 * This class is the Activity for Starred Messages View that
 * displays all the messages marked as starred
 */

public class StarMessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.starred_messages);
        setTitle("Starred Messages");

        List<ChatMessageDB> messageList = SQLite.select().
                from(ChatMessageDB.class).queryList();

        ListAdapter adapter = new StarMessageAdapter(getApplicationContext(), messageList);
        ListView listView = (ListView) findViewById(R.id.starred_messages_view);
        listView.setAdapter(adapter);

    }

    class StarMessageAdapter extends ArrayAdapter<ChatMessageDB> {

        private Context mContext;

        public StarMessageAdapter(Context context, List<ChatMessageDB> list) {
            super(context, R.layout.item_message, list);
            mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_message, parent, false);

            ChatMessageDB message = getItem(position);
            TextView messengerTextView = (TextView) view.findViewById(R.id.messengerTextView);
            TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
            CircleImageView messengerImageView =
                    (CircleImageView) view.findViewById(R.id.messengerImageView);
            ImageView messageImageView = (ImageView) view.findViewById(R.id.messageImageView);
            TextView messsageTimeView = (TextView) view.findViewById(R.id.messsageTime);

            messengerTextView.setText(message.getName());
            messageTextView.setText(message.getText());
            messsageTimeView.setText(message.getDate());

            MessageUtil.displayPhotoURL(messengerImageView, message.getPhotoUrl(), mContext);
            MessageUtil.displayImageMessage(message.getImageUrl(),
                    messageImageView, messageTextView,
                    mContext);

            return view;
        }
    }
}
