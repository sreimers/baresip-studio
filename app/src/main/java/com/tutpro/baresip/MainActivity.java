/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tutpro.baresip;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button accountsButton = (Button)findViewById(R.id.accounts);
        accountsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,
                                         EditAccountsActivity.class));
            }
        });

        Button contactsButton = (Button)findViewById(R.id.contacts);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,
                                         EditContactsActivity.class));
            }
        });

        Button startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    new Thread(new Runnable() {
                        public void run() {
                            baresipStart();
                        }
                    }).start();
                    ((Button) v).setText("Running");
                    running = true;
                } else {
                    /* Do nothing */
                }
            }
        });

        Button quitButton = (Button)findViewById(R.id.quit);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    baresipStop();
                    running = false;
                }
/*                finish(); */
                System.exit(0);
            }
        });

        Bundle b = getIntent().getExtras();
        String action = null;
        if (b != null) {
            action = b.getString("action");
        }
        if ( (action == null) || action.equals("save")) {
            try {
                Utils.installFiles(getApplicationContext());
            } catch (java.io.IOException e) {
                Log.e("Baresip", "Failed to install files: " + e.toString());
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, MainActivity.class);
        switch (item.getItemId()) {
        case R.id.help:
            startActivity(new Intent(MainActivity.this,
                                     HelpActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public native void baresipStart();
    public native void baresipStop();

    static {
        System.loadLibrary("baresip");
    }

}
