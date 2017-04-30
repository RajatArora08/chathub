package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.database.ChatMessageDB;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.ui.MainActivity;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cjkriese on 2/17/17.
 */

public class MessageUtil {
    private static final String LOG_TAG = MessageUtil.class.getSimpleName();
    public static final String MESSAGES_CHILD = "messages";

    private static DatabaseReference sFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private static MessageLoadListener sAdapterListener;
    private static FirebaseAuth sFirebaseAuth;
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    public static HashMap<String, ChatMessage> selectedMessages = new HashMap<>();
    private static boolean is_action_mode = false;
    private static Context mContext;

    public interface MessageLoadListener { public void onLoadComplete(); }

    public static void send(ChatMessage chatMessage) {
        sFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessage);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public ImageView messageImageView;
        public boolean is_selected = false;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView =
                    (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);

        }
    }

    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             MessageLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView) {
        sAdapterListener = listener;
        mContext = activity;
        final FirebaseRecyclerAdapter adapter =
                new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(
                        ChatMessage.class,
                        R.layout.item_message,
                        MessageViewHolder.class,
                        sFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

                    @Override
                    protected void populateViewHolder(final MessageViewHolder viewHolder,
                                                      ChatMessage chatMessage, final int position) {

                        sAdapterListener.onLoadComplete();
                        viewHolder.messageTextView.setText(chatMessage.getText());
                        viewHolder.messengerTextView.setText(chatMessage.getName());

                        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

                            @Override
                            public boolean onLongClick(View view) {

                                if (viewHolder.is_selected) {
                                    deselectMessage(view, getRef(position).getKey());
                                    viewHolder.is_selected = false;
                                }
                                else
                                {
                                    selectMessage(view, getRef(position).getKey(), getItem(position));
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                    is_action_mode = true;
                                    viewHolder.is_selected = true;
                                    viewHolder.itemView.setClickable(true);
                                    activity.startActionMode(mActionCallback);
                                }
                                return true;
                            }
                        });

                        displayPhotoURL(viewHolder.messengerImageView, chatMessage.getPhotoUrl(), activity);
                        displayImageMessage(chatMessage.getImageUrl(),
                                viewHolder.messageImageView, viewHolder.messageTextView,
                                activity);


                        /*
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (is_action_mode) {
                                    if (viewHolder.is_selected) {
                                        deselectMessage(view, getRef(position).getKey());
                                        viewHolder.is_selected = false;
                                    }
                                    else {
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                        selectMessage(view, getRef(position).getKey());
                                    }

                                }
                            }
                        });
                        */

                        /*
                        if (chatMessage.getPhotoUrl() == null) {
                            viewHolder.messengerImageView
                                    .setImageDrawable(ContextCompat
                                            .getDrawable(activity,
                                                    R.drawable.ic_account_circle_black_36dp));
                        } else {
                            SimpleTarget target = new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                    viewHolder.messengerImageView.setImageBitmap(bitmap);
                                }
                            };
                            Glide.with(activity)
                                    .load(chatMessage.getPhotoUrl())
                                    .asBitmap()
                                    .into(target);
                        }

                        if (chatMessage.getImageUrl() != null) {
                            //Set view visibilities for a image message
                            viewHolder.messageImageView.setVisibility(View.VISIBLE);
                            viewHolder.messageTextView.setVisibility(View.GONE);
                            // load image for message
                            try {
                                final StorageReference gsReference =
                                        sStorage.getReferenceFromUrl(chatMessage.getImageUrl());
                                gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(activity)
                                                .load(uri)
                                                .into(viewHolder.messageImageView);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.e(LOG_TAG, "Could not load image for message", exception);
                                    }
                                });
                            } catch (IllegalArgumentException e) {
                                viewHolder.messageTextView.setText("Error loading image");
                                Log.e(LOG_TAG, e.getMessage() + " : " + chatMessage.getImageUrl());
                            }
                        } else {
                            //Set view visibilities for a text message
                            viewHolder.messageImageView.setVisibility(View.GONE);
                            viewHolder.messageTextView.setVisibility(View.VISIBLE);
                        }
                        */

                    }
                };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = linearManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(lastVisiblePosition);
                }
            }
        });


        return adapter;
    }

    public static void selectMessage(View view, String key, ChatMessage chatMessage) {
        view.setBackgroundResource(R.color.selectedMessage);
        selectedMessages.put(key, chatMessage);
    }

    public static void deselectMessage(View view, String key) {
        view.setBackgroundResource(0);
        selectedMessages.remove(key);
    }


    public static void displayImageMessage(String imageUrl, final ImageView messageImageView,
                                           TextView messageTextView, final Context context) {
        if (imageUrl != null) {
            //Set view visibilities for a image message
            messageImageView.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);
            // load image for message
            try {
                final StorageReference gsReference =
                        sStorage.getReferenceFromUrl(imageUrl);
                gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context)
                                .load(uri)
                                .into(messageImageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(LOG_TAG, "Could not load image for message", exception);
                    }
                });
            } catch (IllegalArgumentException e) {
                messageTextView.setText("Error loading image");
                Log.e(LOG_TAG, e.getMessage() + " : " + imageUrl);
            }
        } else {
            //Set view visibilities for a text message
            messageImageView.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);
        }
    }

    public static void displayPhotoURL(final CircleImageView messengerImageView, String photoUrl, Context context) {
        if (photoUrl == null) {
            messengerImageView
                    .setImageDrawable(ContextCompat
                            .getDrawable(context,
                                    R.drawable.ic_account_circle_black_36dp));
        } else {
            SimpleTarget target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    messengerImageView.setImageBitmap(bitmap);
                }
            };
            Glide.with(context)
                    .load(photoUrl)
                    .asBitmap()
                    .into(target);
        }
    }


    public static StorageReference getImageStorageReference(FirebaseUser user, Uri uri) {
        //Create a blob storage reference with path : bucket/userId/timeMs/filename
        long nowMs = Calendar.getInstance().getTimeInMillis();

        return sStorage.getReference().child(user.getUid() + "/" + nowMs + "/" + uri
                .getLastPathSegment());
    }


    public static ActionMode.Callback mActionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Options");

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_action_bar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {

                case R.id.starMessage:
                    starMessages();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(LOG_TAG, "Selection Done. Exiting action mode");
            is_action_mode = false;
        }

        public void starMessages() {

            for (String key : selectedMessages.keySet()) {
                ChatMessageDB db = new ChatMessageDB();
                db.insertData(key, selectedMessages.get(key));
                db.save();
            }



            /*
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            Gson gson = new Gson();

            ArrayList<ChatMessage> data = new ArrayList<>();
            if(mPrefs.contains("Starred_Messages")) {
                String json = mPrefs.getString("Starred_Messages", "");
                data = gson.fromJson(json, ArrayList.class);

                for (ChatMessage message : selectedMessages) {
                    if (!data.contains(message))
                        data.add(message);
                }
            }
            else {
                data = selectedMessages;
            }

            String json = gson.toJson(data);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putString("Starred_Messages", json);
            prefsEditor.commit();

            Log.d(LOG_TAG, "Messages selected: " + json);
            */

        }

    };

}
