/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Locale;

import edu.sfsu.csc780.chathub.BackgroundUtils;
import edu.sfsu.csc780.chathub.ImageUtil;
import edu.sfsu.csc780.chathub.LocationUtils;
import edu.sfsu.csc780.chathub.MapLoader;
import edu.sfsu.csc780.chathub.MessageUtil;
import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.ui.SignInActivity;

import static edu.sfsu.csc780.chathub.ImageUtil.savePhotoImage;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, MessageUtil.MessageLoadListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int MSG_LENGTH_LIMIT = 50;
    public static final String ANONYMOUS = "anonymous";
    private static final int REQUEST_PICK_IMAGE = 1;
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageButton mLocationButton;
    private ImageButton mPaintButton;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseRecyclerAdapter<ChatMessage, MessageUtil.MessageViewHolder>
            mFirebaseAdapter;
    private ImageButton mImageButton;
    private ImageButton mMicButton;
    int dayNightMode;
    public static final int RESULT_SPEECH = 200;
    private static final int REQUEST_PICK_WALLPAPER = 300;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dayNightMode = AppCompatDelegate.getDefaultNightMode();
        if (dayNightMode == AppCompatDelegate.MODE_NIGHT_AUTO) {
            setTheme(R.style.AppTheme);
        }
        else if(dayNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            setTheme(R.style.AppTheme);
        }
        else {
            setTheme(R.style.AppThemeNight);
        }

        setContentView(R.layout.activity_main);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String wallpaperUri = mSharedPreferences.getString("wallpaperPath", null);

        if (wallpaperUri != null) {
            Bitmap bitmap = ImageUtil.getBitmapForUri(this, Uri.parse(wallpaperUri));
            changeWallpaper(bitmap);
        }

        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        //Initialize Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mUser.getDisplayName();
            if (mUser.getPhotoUrl() != null) {
                mPhotoUrl = mUser.getPhotoUrl().toString();
            }
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mFirebaseAdapter = MessageUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mMessageRecyclerView);

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MSG_LENGTH_LIMIT)});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send messages on click.
                mMessageRecyclerView.scrollToPosition(0);
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl);
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });

        mImageButton = (ImageButton) findViewById(R.id.shareImageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(REQUEST_PICK_IMAGE);
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadmap();
            }
        });

        mMicButton = (ImageButton) findViewById(R.id.micbutton);
        mMicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechProcess();
            }
        });

        mPaintButton = (ImageButton) findViewById(R.id.paintButton);
        mPaintButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PaintActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocationUtils.startLocationUpdates(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.change_mode_menu:
                switchMode();
                return true;
            case R.id.background_change_menu:
                showWallpaperMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showWallpaperMenu () {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.background_change_menu));
        popupMenu.setOnMenuItemClickListener(this);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.wallpaper_menu, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.resetWallpaper:
                mRelativeLayout.setBackgroundResource(0);
                mSharedPreferences.edit().remove("wallpaperPath").commit();
                return true;
            case R.id.chooseWallpaper:
                pickImage(REQUEST_PICK_WALLPAPER);
                return true;
            default:
                return false;

        }

 }

    private void switchMode() {

        if (dayNightMode == AppCompatDelegate.MODE_NIGHT_AUTO ||
                dayNightMode == AppCompatDelegate.MODE_NIGHT_NO ) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadComplete() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        boolean isGranted = (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if (isGranted && requestCode == LocationUtils.REQUEST_CODE) {
            LocationUtils.startLocationUpdates(this);
        }
    }

    private void speechProcess() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your message");

        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Intent problem", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage(int requestCode) {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");

        startActivityForResult(intent, requestCode);

    }

    private void loadmap() {

        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        mLocationButton.setEnabled(false);


        Loader<Bitmap> loader = getSupportLoaderManager().initLoader(0, null,
            new LoaderManager.LoaderCallbacks<Bitmap>() {

                boolean flag_image_sent = false;

                @Override
                public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {

                    return new MapLoader(MainActivity.this);
                }

                @Override
                public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap result) {

                    if (!flag_image_sent) {

                        if (result == null) return;
                        Bitmap resizedBitmap = ImageUtil.scaleImage(result);

                        Uri uri = null;

                        if (result != resizedBitmap) {
                            uri = savePhotoImage(MainActivity.this, resizedBitmap);
                        } else {
                            uri = savePhotoImage(MainActivity.this, result);
                        }
                        createImageMessage(uri);

                        flag_image_sent = true;

                        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                        mLocationButton.setEnabled(true);
                    }
                }

                @Override
                public void onLoaderReset(final Loader<Bitmap> loader) {
                }

            });


        loader.forceLoad();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request=" + requestCode + ", result=" + resultCode);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // Process selected image here
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // Resize if too big for messaging
                Bitmap bitmap = ImageUtil.getBitmapForUri(this, uri);
                Bitmap resizedBitmap = ImageUtil.scaleImage(bitmap);
                if (bitmap != resizedBitmap) {
                    uri = savePhotoImage(this, resizedBitmap);
                }

                createImageMessage(uri);
            }  else {
                Log.e(TAG, "Cannot get image for uploading");
            }
        }

        if (requestCode == RESULT_SPEECH && resultCode == Activity.RESULT_OK) {

            ArrayList<String> out = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            ChatMessage chatMessage = new
                    ChatMessage(out.get(0),
                    mUsername,
                    mPhotoUrl);

            MessageUtil.send(chatMessage);

        }

        if (requestCode == REQUEST_PICK_WALLPAPER && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // Resize if too big for messaging
                Bitmap bitmap = ImageUtil.getBitmapForUri(this, uri);
                BackgroundUtils.saveWallpaper(this, uri);

                changeWallpaper(bitmap);
            }

        }
    }

    private void changeWallpaper(Bitmap bitmap) {

        Drawable drawable = new BitmapDrawable(getResources(),bitmap);
        mRelativeLayout.setBackground(drawable);

    }

    private void createImageMessage(Uri uri) {
        if (uri == null) Log.e(TAG, "Could not create image message with null uri");

        final StorageReference imageReference = MessageUtil.getImageStorageReference(mUser, uri);
        UploadTask uploadTask = imageReference.putFile(uri);
        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload image message");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl, imageReference.toString());
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });
    }


}
