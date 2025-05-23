package edu.unh.cs.cs619.bulletzone;

import android.content.Context;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import android.content.Intent;

import java.io.IOException;
import java.io.OutputStreamWriter;

@EActivity(R.layout.activity_authenticate)
public class AuthenticateActivity extends AppCompatActivity {
    @ViewById
    EditText username_editText;

    @ViewById
    EditText password_editText;

    @ViewById
    TextView status_message;

    @Bean
    AuthenticationController controller;

    long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Since we are using the @EActivity annotation, anything done past this point will
        //be overridden by the work AndroidAnnotations does. If you need to do more setup,
        //add to the methods under @AfterViews (for view items) or @AfterInject (for Bean items) below
    }

    @AfterViews
    protected void afterViewInjection() {
        //Put any view-setup code here (that you might normally put in onCreate)
    }

    @AfterInject
    void afterInject() {
        //Put any Bean-related setup code here (the you might normally put in onCreate)
    }

    private Boolean createNewUserFile(String username, long tankID) {
        try {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(username + ".txt", Context.MODE_PRIVATE));
            /*
                just going to subtract 1 as the return val from login is 1 but
                    for whatever reason the TID reflected in the JSON is all zeroes
             */
            outputStreamWriter.write(String.valueOf(tankID - 1));
            outputStreamWriter.close();
            Log.d("TANKID FILE", "created new file with username " + username + " and id " + tankID);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return true;
    }

    /**
     * Registers a new user and logs them in
     */
    @Click(R.id.register_button)
    @Background
    protected void onButtonRegister() {
        String username = username_editText.getText().toString();
        String password = password_editText.getText().toString();

        boolean status = controller.register(username, password);

        if (!status) {
            setStatus("User " + username + " already exists or server error.\nPlease login or try with a different username.");
        } else { //register successful
            setStatus("Registration successful.");
            //Do you want to log in automatically, or force them to do it?
            userID = controller.login(username, password);
            if (userID < 0) {
                setStatus("Registration unsuccessful--inconsistency with server.");
            } else {
                navigateToMainAppScreen(username);
            }
            //do other login things?
        }
    }

    /**
     * Logs in an existing user
     */
    @Click(R.id.login_button)
    @Background
    protected void onButtonLogin() {
        String username = username_editText.getText().toString();
        String password = password_editText.getText().toString();

        userID = controller.login(username, password);
        if (userID < 0) {
            setStatus("Invalid username and/or password.\nPlease try again.");
        } else { //register successful
            setStatus("Login successful.");
            navigateToMainAppScreen(username);
            //do other login things?
        }
    }

    private void navigateToMainAppScreen(String username) {
        Intent intent = new Intent(this, ClientActivity_.class);
        intent.putExtra("username", username);
        Boolean madeFile = createNewUserFile(username, userID);
        startActivity(intent);
        finish();  // Optional: Close the authentication screen so the user can't navigate back
    }

    @UiThread
    protected void setStatus(String message) {
        status_message.setText(message);
    }
}