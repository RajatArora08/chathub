package edu.sfsu.csc780.chathub;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.database.ChatMessageDB;
import edu.sfsu.csc780.chathub.model.ChatMessage;

public class MessageUtil {
    private static final String LOG_TAG = MessageUtil.class.getSimpleName();
    public static final String MESSAGES_CHILD = "messages";

    private static DatabaseReference sFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private static MessageLoadListener sAdapterListener;
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    public static HashMap<String, ChatMessage> selectedMessages = new HashMap<>();
    private static boolean is_action_mode = false;
    private static Context mContext;
    private static FirebaseRecyclerAdapter mAdapter;

    public interface MessageLoadListener { public void onLoadComplete(); }

    public static void send(ChatMessage chatMessage) {
        chatMessage.setTimeStamp(DateUtil.getUTCTime());
        sFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessage);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public ImageView messageImageView;
        public TextView messsageTimeView;
        public LinearLayout linearLayout;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView =
                    (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messsageTimeView = (TextView) itemView.findViewById(R.id.messsageTime);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.singleMessageLL);
            linearLayout.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if(is_action_mode)
                processSelection(v, getAdapterPosition());
        }
    }

    public static void processSelection(View v, int pos) {
        String key = mAdapter.getRef(pos).getKey();

        if(!(boolean)v.getTag()) {
            v.setBackgroundResource(R.color.selectedMessage);
            selectedMessages.put(key, (ChatMessage) mAdapter.getItem(pos));
            v.setTag(true);
        }
        else
        {
            selectedMessages.remove(key);
            v.setBackgroundResource(0);
            v.setTag(false);
        }

    }

    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             MessageLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView) {
        sAdapterListener = listener;
        mContext = activity;
        mAdapter =
                new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(
                        ChatMessage.class,
                        R.layout.item_message,
                        MessageViewHolder.class,
                        sFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

                    @Override
                    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view;
                        if (viewType == 1)
                            view = LayoutInflater.from(mContext).inflate(R.layout.item_message_me, parent, false);
                        else
                            view = LayoutInflater.from(mContext).inflate(R.layout.item_message, parent, false);
                        return new MessageViewHolder(view);
                    }

                    @Override
                    public int getItemViewType(int position) {
                        if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName()
                                .equals(getItem(position).getName()))
                            return 1;
                        else
                            return 2;
                    }

                    @Override
                    protected void populateViewHolder(final MessageViewHolder viewHolder,
                                                      ChatMessage chatMessage, final int position) {

                        sAdapterListener.onLoadComplete();
                        viewHolder.messageTextView.setText(chatMessage.getText());
                        viewHolder.messengerTextView.setText(chatMessage.getName());
                        if (chatMessage.getTimeStamp() != null)
                            viewHolder.messsageTimeView.setText(DateUtil.toLocalTime(chatMessage.getTimeStamp()));

                        if (viewHolder.linearLayout.getTag() == null)
                            viewHolder.linearLayout.setTag(false);
                        if (!(boolean)viewHolder.linearLayout.getTag())
                            viewHolder.linearLayout.setBackgroundResource(0);
                        viewHolder.linearLayout.setClickable(true);

                        viewHolder.linearLayout.setOnLongClickListener(new View.OnLongClickListener(){

                            @Override
                            public boolean onLongClick(View view) {

                                if(!is_action_mode) {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                    activity.startActionMode(mActionCallback);
                                    is_action_mode = true;
                                }

                                return true;
                            }
                        });

                        displayPhotoURL(viewHolder.messengerImageView, chatMessage.getPhotoUrl(), activity);
                        displayImageMessage(chatMessage.getImageUrl(),
                                viewHolder.messageImageView, viewHolder.messageTextView,
                                activity);
                    }
                };

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mAdapter.getItemCount();
                int lastVisiblePosition = linearManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                }
                recyclerView.scrollToPosition(messageCount-1);
            }
        });

        return mAdapter;
    }


    public static void displayImageMessage(String imageUrl, final ImageView messageImageView,
                                           TextView messageTextView, final Context context) {
        if (imageUrl != null) {
            messageImageView.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);
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
        long nowMs = Calendar.getInstance().getTimeInMillis();

        return sStorage.getReference().child(user.getUid() + "/" + nowMs + "/" + uri
                .getLastPathSegment());
    }


    public static ActionMode.Callback mActionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Click to select");

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
                    mode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(LOG_TAG, "Selection Done. Exiting action mode");
            is_action_mode = false;
            mAdapter.notifyDataSetChanged();
        }

        public void starMessages() {

            for (String key : selectedMessages.keySet()) {
                ChatMessageDB db = new ChatMessageDB();
                db.insertData(key, selectedMessages.get(key));
                db.save();
            }

            selectedMessages.clear();

        }

    };

}
