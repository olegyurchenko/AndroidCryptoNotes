package oleg.home.ua.cryptonotes;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
  CompoundButton.OnCheckedChangeListener {

  EditText passwordEdit, decryptedEdit, encryptedEdit;
  CheckBox showPasswordBox;
  Button encryptButton, decryptButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);


    passwordEdit = (EditText)findViewById(R.id.passwordEdit);
    decryptedEdit = (EditText)findViewById(R.id.decryptedTextEdit);
    encryptedEdit = (EditText)findViewById(R.id.encryptedTextEdit);

    showPasswordBox = (CheckBox)findViewById(R.id.showPassCheck);

    encryptButton = (Button) findViewById(R.id.encryptButton);
    decryptButton = (Button) findViewById(R.id.decryptButton);

    encryptButton.setOnClickListener(this);
    decryptButton.setOnClickListener(this);
    showPasswordBox.setOnCheckedChangeListener(this);

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
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v)
  {
    String result;
    int id = v.getId();
    switch(id)
    {
      case R.id.decryptButton:
        try {
          result = OpenSslAes.decrypt(passwordEdit.getText().toString(), encryptedEdit.getText().toString());
          decryptedEdit.setText(result);
        }

        catch (Exception e) {
          Log.e("CryptoNotes", "Exception", e);
          Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
        }
        break;
      case R.id.encryptButton:
        try {
          result = OpenSslAes.encrypt(passwordEdit.getText().toString(), decryptedEdit.getText().toString());
          encryptedEdit.setText(result);
        }
        catch(Exception e) {
          Log.e("CryptoNotes", "Exception", e);
          Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
        }
        break;
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton v, boolean isChecked) {
    int id = v.getId();

    switch(id)
    {
      case R.id.showPassCheck:
        passwordEdit.setTransformationMethod(isChecked ? null : new PasswordTransformationMethod());
        break;
    }
  }

}
