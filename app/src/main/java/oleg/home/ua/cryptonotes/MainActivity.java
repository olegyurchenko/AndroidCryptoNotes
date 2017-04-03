package oleg.home.ua.cryptonotes;


  import android.app.AlertDialog;
  import android.app.Dialog;
  import android.content.Intent;
  import android.icu.util.Calendar;
  import android.net.Uri;
  import android.os.Bundle;
  import android.support.design.widget.TabLayout;
  import android.support.v4.content.FileProvider;
  import android.support.v4.view.ViewPager;
  import android.support.v7.app.AppCompatActivity;
  import android.support.v7.widget.Toolbar;
  import android.text.format.DateFormat;
  import android.text.method.DateTimeKeyListener;
  import android.text.method.PasswordTransformationMethod;
  import android.util.Log;
  import android.view.Menu;
  import android.view.MenuItem;
  import android.view.View;
  import android.view.Window;
  import android.view.WindowManager;
  import android.widget.Button;
  import android.widget.CheckBox;
  import android.widget.CompoundButton;
  import android.widget.EditText;
  import android.widget.LinearLayout;
  import android.widget.Toast;

  import java.io.BufferedReader;
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileOutputStream;
  import java.io.FileWriter;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.InputStreamReader;
  import java.text.SimpleDateFormat;
  import java.util.ArrayList;
  import java.util.Date;
  import java.util.Locale;


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
    Button openBtn, saveBtn, shareBtn, sendBtn, encryptBtn, decryptBtn;


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
      showPasswordBox.setOnCheckedChangeListener(this);


      encryptBtn = (Button) findViewById(R.id.encryptBtn);
      encryptBtn.setOnClickListener(this);
  
      decryptBtn = (Button) findViewById(R.id.decryptBtn);
      decryptBtn.setOnClickListener(this);
  
      openBtn = (Button) findViewById(R.id.openBtn);
      openBtn.setOnClickListener(this);
  
      saveBtn = (Button) findViewById(R.id.saveBtn);
      saveBtn.setOnClickListener(this);
  
      sendBtn = (Button) findViewById(R.id.sendBtn);
      sendBtn.setOnClickListener(this);

      shareBtn = (Button) findViewById(R.id.shareBtn);
      shareBtn.setOnClickListener(this);

      enableButtons();
    }
  
    private void enableButtons()
    {
      encryptedEdit = (EditText)viewPager.findViewById(R.id.encryptedTextEdit);

      boolean saveEnabled =
        viewPager.getCurrentItem() != 0
          && encryptedEdit != null
          && !encryptedEdit.getText().toString().isEmpty();

      saveBtn.setEnabled(saveEnabled);
      sendBtn.setEnabled(saveEnabled);
      shareBtn.setEnabled(saveEnabled);
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
      switch(item.getItemId())
      {
        case R.id.action_about:
          helpAboutDialog();
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
        save();
        break;
      case R.id.sendBtn:
        send();
        break;
      case R.id.shareBtn:
        share();
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

      if(save(new File(outputFileName), edit.getText().toString()))
        Toast.makeText(this, String.format("File '%s' was saved successfully", outputFileName), Toast.LENGTH_LONG).show();

    }

    boolean save(File f, String data) {
      boolean result = true;
      try {
        File folder = new File(f.getParent());
        if (!folder.exists()) {
          if(!folder.mkdirs())
            throw new Exception(String.format("Error create folder '%s'", folder.getName()));
        }
        FileWriter writer = new FileWriter(f);
        writer.write(data);
        writer.flush();
        writer.close();
      }
      catch (Exception e)
      {
        Log.e("CryptoNotes", "Exception", e);
        Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
        result = false;
      }
      return result;
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

    void save() {
      FileSaveFragment fsf = FileSaveFragment.newInstance(
        ".txt",
        android.R.string.ok,
        android.R.string.cancel,
        R.string.encrypted_file_save_as,
        R.string.encrypted_hint_file_save_as,
        R.drawable.ic_save);

      fsf.show(getFragmentManager(), "SaveFileTag");
    }

    static final String FOLDER_NAME = "docs";
    static final String FILE_NAME = "data.txt";
  
    void send() {
  
      try {
    
        encryptedEdit = (EditText) viewPager.findViewById(R.id.encryptedTextEdit);
/*
        //File folder = new File(getCacheDir(), FOLDER_NAME);
        File folder = new File(getFilesDir(), FOLDER_NAME);
        File f = new File(folder, "1.txt");
        if(!save(f, encryptedEdit.getText().toString()))
          return;
    
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), AUTHORITIES_NAME, f);
*/
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        //sendIntent.setData(contentUri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, encryptedEdit.getText());
        //sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        //sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
      }

      catch (Exception e) {
        Log.e("CryptoNotes", "Exception", e);
        Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
      }
    }
  
    static final String AUTHORITIES_NAME = "oleg.home.ua.cryptonotes.fileprovider";
    
    String getFileNameFromTimeStamp() {
      Date d = new Date();
      return DateFormat.format("yyyyMMddhhmmss", d.getTime()).toString();
    }

    void share() {
 
      try {
    
        encryptedEdit = (EditText) viewPager.findViewById(R.id.encryptedTextEdit);
        
  
        File folder = new File(getCacheDir(), FOLDER_NAME);
        File f = new File(folder, String.format("%s.txt", getFileNameFromTimeStamp()));
        if(!save(f, encryptedEdit.getText().toString()))
          return;
    
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), AUTHORITIES_NAME, f);
          //Uri.parse("file://" + f); //FileProvider.getUriForFile(getApplicationContext(), AUTHORITIES_NAME, f);
    
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        //sendIntent.putExtra(Intent.EXTRA_TEXT, encryptedEdit.getText());
        sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
      }
  
      catch (Exception e) {
        Log.e("CryptoNotes", "Exception", e);
        Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
      }
    }



    private void share(File fileSharing) {
      try {
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), AUTHORITIES_NAME, fileSharing);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(contentUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
      }
      catch (Exception e) {
        Log.e("CryptoNotes", "Exception", e);
        Toast.makeText(this, String.format("Error:%s", e.getMessage()), Toast.LENGTH_LONG).show();
      }
    }

    public String getAppTimeStamp()
    {
      String timeStamp = "";
      try {
        long buildDate = BuildConfig.TIMESTAMP;
        java.text.DateFormat formatter = java.text.DateFormat.getDateTimeInstance();
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timeStamp = formatter.format(buildDate);
      }
      catch (Exception e) {
        //Ignore
      }

      return timeStamp;
    }

    private String aboutMessage() {
      String versionName = BuildConfig.VERSION_NAME;
      int versionCode = BuildConfig.VERSION_CODE;

      return String.format(Locale.getDefault(), getString(R.string.about_text),
        versionName,
        versionCode,
        getAppTimeStamp());
    }

    private Dialog createAboutDialog () {
      AlertDialog.Builder adb = new AlertDialog.Builder(this);
      adb.setTitle(R.string.app_name);
      adb.setIcon(R.mipmap.ic_launcher);
      adb.setMessage(aboutMessage());

      adb.setPositiveButton(android.R.string.ok, null);

      return adb.create();
    }

    private void helpAboutDialog () {
      Dialog dialog = createAboutDialog();


      dialog.show();
    }

  }

