package com.example.saftest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;

import com.example.saftest.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "testTag";
    private TextView textView;
//    private Uri uri;

    protected final SafActivityResult<Intent, ActivityResult> dirLauncher = SafActivityResult.registerActivityForResult(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        textView = findViewById(R.id.textView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Uri uri = MFiles.loadMFolderUri(this.getBaseContext());
            if (uri != null) {
                String text = getFiles(MFiles.getmFolderUri());
                textView.setText(text);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fabClick(View view) {
        openDirectory(null);
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void openDirectory(Uri uriToLoad) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);//, SafActivityResult.class);
        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
        }
        dirLauncher.launch(intent,
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        doSomeOperations(data);
                    }
                });
    }

    @SuppressLint("WrongConstant")
    void doSomeOperations(Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

            MFiles.saveMFolderUri(this, uri);

            Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
            String text = getFiles(uri);
            textView.setText(text);
        }
    }

    private String getFiles(Uri uri) {
        StringBuilder sfiles = new StringBuilder();
        DocumentFile parentFolder = DocumentFile.fromTreeUri(this, uri);
        if (parentFolder != null) {
            DocumentFile[] dfiles = parentFolder.listFiles();
            sfiles = new StringBuilder("\n" + parentFolder.getName() + " content:");
            for (DocumentFile dfile : dfiles) {
                sfiles.append("\n").append(dfile.getName());
                try {
                    copyFile(dfile, parentFolder);
                } catch (Exception ex) {
                    Log.d(TAG, "getFiles: " + ex.getMessage());
                }

            }
        }
        return sfiles.toString();
    }

    boolean copyFile(DocumentFile documentFile, DocumentFile parentFolder) throws IOException {
        boolean ret = false;
        if (documentFile != null && documentFile.exists() && documentFile.isFile()) {
            String name = documentFile.getName();
            String mimeType = documentFile.getType();
            long length = documentFile.length();
            DocumentFile destinationFile = parentFolder.createFile(mimeType, name + "_copy");
            if (destinationFile != null) {
                ContentResolver resolver = getContentResolver();
                OutputStream outputStream = resolver.openOutputStream(destinationFile.getUri());
                InputStream inputStream = resolver.openInputStream(documentFile.getUri());
                int len;
                final byte[] b = new byte[1024];
                while ((len = inputStream.read(b)) > 0) {
                    outputStream.write(b, 0, len);
                }
                outputStream.flush();
                outputStream.close();
                ret = true; //writeStream(inputStream, outputStream);
            }
        }
        return ret;
    }
}