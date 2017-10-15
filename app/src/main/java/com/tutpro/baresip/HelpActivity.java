package com.tutpro.baresip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    private TextView helpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        helpView = (TextView)findViewById(R.id.helpText);
        helpView.setEnabled(false);
        helpView.setText(getString(R.string.helpText));
    }

}

