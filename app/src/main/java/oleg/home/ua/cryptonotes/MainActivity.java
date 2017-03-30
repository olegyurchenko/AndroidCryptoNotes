package oleg.home.ua.cryptonotes;


  import android.os.Bundle;
  import android.support.design.widget.TabLayout;
  import android.support.v4.view.ViewPager;
  import android.support.v7.app.AppCompatActivity;
  import android.support.v7.widget.Toolbar;
  import android.text.method.PasswordTransformationMethod;
  import android.util.Log;
  import android.view.Menu;
  import android.view.MenuItem;
  import android.view.View;
  import android.widget.Button;
  import android.widget.CheckBox;
  import android.widget.CompoundButton;
  import android.widget.EditText;
  import android.widget.Toast;

  import java.io.BufferedReader;
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.InputStreamReader;
  import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
  CompoundButton.OnCheckedChangeListener,
  FileSaveFragment.Callbacks,
  FileSelectFragment.Callbacks
  {
    enum FileMode {
      Encrypt,
      Decrypt
    };


    EditText passwordEdit, decryptedEdit, encryptedEdit;
    CheckBox showPasswordBox;
    ViewPager viewPager;
    PagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
  
      TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
      tabLayout.addTab(tabLayout.newTab().setText(getResources().getText(R.string.decrypted_text)));
      tabLayout.addTab(tabLayout.newTab().setText(getResources().getText(R.string.encrypted_text)));
      tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
  
      viewPager = (ViewPager) findViewById(R.id.pager);
      adapter = new PagerAdapter
        (getSupportFragmentManager(), tabLayout.getTabCount());
      viewPager.setAdapter(adapter);
      viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
      tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
          tabSelect(tab.getPosition());
        }
    
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
      
        }
    
        @Override
        public void onTabReselected(TabLayout.Tab tab) {
      
        }
      });
  
  
      passwordEdit = (EditText)findViewById(R.id.passwordEdit);
  
      //decryptedEdit = (EditText)findViewById(R.id.decryptedTextEdit);
      decryptedEdit = (EditText)viewPager.findViewWithTag(R.id.decryptedTextEdit);
  
      //encryptedEdit = (EditText)findViewById(R.id.encryptedTextEdit);
  
      showPasswordBox = (CheckBox)findViewById(R.id.showPassCheck);
  
      Button button;
  
      button = (Button) findViewById(R.id.encryptBtn);
      button.setOnClickListener(this);
  
      button = (Button) findViewById(R.id.decryptBtn);
      button.setOnClickListener(this);
  
      button = (Button) findViewById(R.id.openBtn);
      button.setOnClickListener(this);
  
      button = (Button) findViewById(R.id.saveBtn);
      button.setOnClickListener(this);
  
      showPasswordBox.setOnCheckedChangeListener(this);
      
      enableButtons();
    }
  
    private void enableButtons()
    {
      Button button = (Button) findViewById(R.id.saveBtn);
      encryptedEdit = (EditText)viewPager.findViewById(R.id.encryptedTextEdit);
  
      button.setEnabled(
        viewPager.getCurrentItem() != 0
          && encryptedEdit != null
          && !encryptedEdit.getText().toString().isEmpty()
      );
    }
    
    private void tabSelect(int position)
    {
      viewPager.setCurrentItem(position);
      enableButtons();
    }
  
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
    }
  
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();
      if (id == R.id.action_settings) {
        return true;
      }
    
      return super.onOptionsItemSelected(item);
    }
  
    @Override
  public void onClick(View v) {
    decryptedEdit = (EditText)viewPager.findViewById(R.id.decryptedTextEdit);
    encryptedEdit = (EditText)viewPager.findViewById(R.id.encryptedTextEdit);

    String result;
    switch (v.getId())
    {
      case R.id.decryptBtn:
        try {
          result = OpenSslAes.decrypt(passwordEdit.getText().toString(), encryptedEdit.getText().toString());
          decryptedEdit.setText(result);
        }

        catch (Exception e) {
          Log.e("CryptoNotes", "Exception", e);
          Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
        }
        enableButtons();
        break;
      case R.id.encryptBtn:
        try {
          result = OpenSslAes.encrypt(passwordEdit.getText().toString(), decryptedEdit.getText().toString());
          encryptedEdit.setText(result);
        }
        catch(Exception e) {
          Log.e("CryptoNotes", "Exception", e);
          Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
        }
        enableButtons();
        break;
      case R.id.openBtn:
        open(viewPager.getCurrentItem() == 0 ? FileMode.Decrypt : FileMode.Encrypt);
        break;
      case R.id.saveBtn:
        save(viewPager.getCurrentItem() == 0 ? FileMode.Decrypt : FileMode.Encrypt);
        break;
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    switch (buttonView.getId())
    {
      case R.id.showPassCheck:
        passwordEdit.setTransformationMethod(isChecked ? null : new PasswordTransformationMethod());
        break;
    }
  }

    @Override
    public void onConfirmSelect(String absolutePath, String fileName) {

      if(absolutePath == null || fileName == null)
        return;

      decryptedEdit = (EditText) viewPager.findViewById(R.id.decryptedTextEdit);
      encryptedEdit = (EditText) viewPager.findViewById(R.id.encryptedTextEdit);

      EditText edit;

      if (viewPager.getCurrentItem() == 0)
        edit = decryptedEdit;
      else
        edit = encryptedEdit;
  
      String inputFileName = String.format("%s/%s", absolutePath, fileName);
      StringBuilder result = new StringBuilder();
      try {
        FileInputStream is = new FileInputStream(inputFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    
        String line;
        boolean flag = false;
        while ((line = reader.readLine()) != null) {
          result.append(flag ? "\n" : "").append(line);
          flag = true;
        }
      }
  
      catch(IOException e) {
        Log.e("CryptoNotes", "Read file error", e);
      }

      edit.setText(result.toString());
    }

    @Override
    public boolean isValid(String absolutePath, String fileName) {
      return !(absolutePath == null
        || fileName == null
        || fileName.isEmpty()
        || !FileSaveFragment.FileExists(absolutePath, fileName)
      );
    }

    @Override
    public boolean onCanSave(String absolutePath, String fileName) {
     if(absolutePath == null || fileName == null || fileName.isEmpty())
       return false;
    
      //FODO: FileExistsDialog FileSaveFragment.FileExists(absolutePath, fileName)
      return true;
    }

    @Override
    public void onConfirmSave(String absolutePath, String fileName) {
      if(absolutePath == null || fileName == null || fileName.isEmpty())
        return;

      if(!fileName.endsWith(".txt"))
        fileName += ".txt";
  
      String outputFileName = String.format("%s/%s", absolutePath, fileName);
      
      
      decryptedEdit = (EditText) viewPager.findViewById(R.id.decryptedTextEdit);
      encryptedEdit = (EditText) viewPager.findViewById(R.id.encryptedTextEdit);

      EditText edit;

      if (viewPager.getCurrentItem() == 0)
        edit = decryptedEdit;
      else
        edit = encryptedEdit;

      try {
        FileOutputStream out = new FileOutputStream(outputFileName);
        out.write(edit.getText().toString().getBytes());
        out.flush();
        out.close();
        Toast.makeText(this, String.format("File '%s' was saved successfully", outputFileName), Toast.LENGTH_LONG).show();
      }

      catch(Exception e) {
        Log.e("CryptoNotes", "Exception", e);
        Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
      }

    }

    void open(FileMode mode) {
      FileSelectFragment fsf = FileSelectFragment.newInstance(FileSelectFragment.Mode.FileSelector,
        android.R.string.ok,
        android.R.string.cancel,
        mode == FileMode.Decrypt ? R.string.decrypted_file_open :  R.string.encrypted_file_open,
        R.mipmap.ic_launcher,
        R.drawable.ic_dir,
        R.drawable.ic_file);

// Restrict selection to *.xml files
      ArrayList<String> allowedExtensions = new ArrayList<String>();
      allowedExtensions.add(".txt");
      fsf.setFilter(FileSelectFragment.FiletypeFilter(allowedExtensions));

      fsf.show(getFragmentManager(), "OpenFileTag");
    }

    void save(FileMode mode) {
      FileSaveFragment fsf = FileSaveFragment.newInstance(
        ".txt",
        android.R.string.ok,
        android.R.string.cancel,
        mode == FileMode.Decrypt ? R.string.decrypted_file_save_as : R.string.encrypted_file_save_as,
        mode == FileMode.Decrypt ? R.string.decrypted_hint_file_save_as : R.string.encrypted_hint_file_save_as,
        R.drawable.ic_save);

      fsf.show(getFragmentManager(), "SaveFileTag");
    }


  }

