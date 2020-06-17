package com.example.filemanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CopyActivity extends AppCompatActivity implements View.OnClickListener {
    ListView listView;
    List<String> sdList;
    File file;
    TextView txtEmpty;
    String root_sd, source;
    Button btn_Cancel, btn_Copy;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);
        source = "";
        Bundle bundle = getIntent().getExtras();
        source = bundle.getString("source");
        Log.v("source", source);
        listView = findViewById(R.id.listView_copy);
        sdList = new ArrayList<>();
        txtEmpty = findViewById(R.id.txt_empty);
        root_sd = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(root_sd);
        if (file.exists()) {
            setListView(listView,sdList, file);
            if(sdList.size()==0) txtEmpty.setText("This folder is empty");
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File temp_file = new File(file, sdList.get(position) + "/");
                if (temp_file.exists() && !temp_file.isFile()) { // it is a folder
                    file = new File(file, sdList.get(position) + "/");
                    setListView(listView, sdList,file);
                    if(sdList.size()==0) txtEmpty.setText("This folder is empty");
                }
            }
        });

        btn_Cancel = findViewById(R.id.btn_cancel);
        btn_Cancel.setOnClickListener(this);
        btn_Copy = findViewById(R.id.btn_copy);
        btn_Copy.setOnClickListener(this);

        registerForContextMenu(listView);
        listView.setLongClickable(true);

        actionBar = getSupportActionBar();
        actionBar.setTitle("SD Card");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    private void setListView(ListView listView, List<String> sdList, File file) {
        String s = file.getAbsolutePath();
        String folderName = "";
        if(!s.equals(root_sd)) {
            folderName = s.substring(root_sd.length()+1);
            actionBar.setTitle(folderName);
        }
//        else actionBar.setTitle("SD Card");
        File[] list = file.listFiles();
        sdList.clear();
        txtEmpty.setText("");
        for (int i = 0; i < list.length; i++) {
            sdList.add(list[i].getName());
        }
        ArrayAdapter aa = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, sdList);
        listView.setAdapter(aa);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id==R.id.btn_cancel){
            finish();
        }
        else {
            String destination = file.getAbsolutePath();
            String fileName = source.substring(source.lastIndexOf("/"));
            Toast.makeText(this,"destination: "+ destination+" filename: "+fileName, Toast.LENGTH_SHORT).show();
            try {
                InputStream is = new FileInputStream(new File(source));
                OutputStream os = new FileOutputStream(new File(destination+fileName));
                OutputStreamWriter writer = new OutputStreamWriter(os);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1)
                    os.write(buffer, 0, len);
                writer.close();
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; add items to the action bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// user clicked a menu-item from ActionBar
        int id = item.getItemId();
        if (id == R.id.act_newFolder) {
            showNewFolderDialog();
            return true;
        }
        if(id== android.R.id.home){
            onBackPressed();
            return true;
        }
        else if(id == R.id.act_newFile) {
            showNewFileDialog();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if(!file.getAbsolutePath().equals(root_sd)) {
            String parent = file.getParent().toString();
            file = new File(parent);
            setListView(listView, sdList, file);
            if (sdList.size() == 0) txtEmpty.setText("This folder is empty");
        }
        else finish();
    }

    private void showNewFileDialog() {
        final Dialog customDialog = new Dialog(CopyActivity.this);
        customDialog.setTitle("New File");
// match customDialog with custom dialog layout
        customDialog.setContentView(R.layout.rename_dialog_layout);
        ((TextView)customDialog.findViewById(R.id.sd_textView1)).setText("New Text File");
        final EditText sd_txtInputData = (EditText) customDialog
                .findViewById(R.id.sd_editText1);
        sd_txtInputData.setHint("");
        ((Button) customDialog.findViewById(R.id.btn_OK))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File newFile = new File(file, sd_txtInputData.getText().toString()+".txt");
//                        if(!newFolder.exists()){
////                            newFolder.mkdirs();
//////                            sdList.add(0,sd_txtInputData.getText().toString());
//////                            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
////                            setListView(new File(newFolder.getParent()));
////                        }

                        try {
                            FileWriter writer = new FileWriter(newFile,true);
                            writer.append("hello :)))");
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setListView(listView, sdList, new File(newFile.getParent()));
                        if(sdList.size()==0) txtEmpty.setText("This folder is empty");
                        customDialog.dismiss();
                    }
                });
        customDialog.findViewById(R.id.sd_btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
        customDialog.show();
    }

    private void showNewFolderDialog() { // show Dialog to create a folder
        final Dialog customDialog = new Dialog(CopyActivity.this);
        customDialog.setTitle("New Folder");
// match customDialog with custom dialog layout
        customDialog.setContentView(R.layout.rename_dialog_layout);
        ((TextView)customDialog.findViewById(R.id.sd_textView1)).setText("New Folder");
        final EditText sd_txtInputData = (EditText) customDialog
                .findViewById(R.id.sd_editText1);
        sd_txtInputData.setHint("");
        ((Button) customDialog.findViewById(R.id.btn_OK))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File newFolder = new File(file, sd_txtInputData.getText().toString()+"/");
                        if(!newFolder.exists()){
                            newFolder.mkdirs();
                            setListView(listView, sdList, new File(newFolder.getParent()));

                        }
                        customDialog.dismiss();
                    }
                });
        customDialog.findViewById(R.id.sd_btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
        customDialog.show();
    }
}
