package com.example.saftest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.view.View;

import com.example.saftest.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //    private AppBarConfiguration appBarConfiguration;
    private TextView textView;
    private Uri uri;

    //    ActivityResultLauncher<Intent> safLauncher;
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
            String text = textView.getText().toString();
            text += getFiles(uri);
            textView.setText(text);

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

    void doSomeOperations(Intent data) {
        if (data != null) {
            uri = data.getData();
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

//            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity?.baseContext)
//            with (sharedPref.edit()) {
//                putString("savePathURI", uri.toString())
//                commit()

            Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();
            String text = textView.getText().toString();
            text += getFiles(uri);
            textView.setText(text);
        }
    }

    String getFiles(Uri uri) {
        StringBuilder sfiles = new StringBuilder();
        DocumentFile parentFolder = DocumentFile.fromTreeUri(this, uri);
        if (parentFolder != null) {
            DocumentFile[] dfiles = parentFolder.listFiles();
            sfiles = new StringBuilder("\n" + parentFolder.getName() + " content:");
            for (DocumentFile dfile : dfiles) {
                sfiles.append("\n").append(dfile.getName());
            }
        }
        return sfiles.toString();
    }

}