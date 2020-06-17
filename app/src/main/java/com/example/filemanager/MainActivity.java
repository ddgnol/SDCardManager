package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.FileVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    ListView listView;
    TextView txtEmpty;
    List<String> sdList;
    File file;
    String msg = "";
    ActionBar actionBar;
    String root_sd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list1);
        sdList = new ArrayList<String>();
        txtEmpty = findViewById(R.id.txt_empty);
        actionBar = getSupportActionBar();
        actionBar.setTitle("SD Card");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) { //Hoi quyen truy nhap tu user
                if (checkPermission()) {
                    root_sd = Environment.getExternalStorageDirectory().getAbsolutePath();
                    Log.v("path", root_sd);
                    file = new File(root_sd);
                    String[] list1 = file.list();
                    if (file.exists()) {
                        setListView(listView, sdList, file);
                        if(sdList.size()==0) txtEmpty.setText("This folder is empty");
                    }
                } else {
                    requestPermission();
                }
            } else {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                if (file.exists()) {
                    setListView(listView,sdList, file);
                    if(sdList.size()==0) txtEmpty.setText("This folder is empty");
                }
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File temp_file = new File(file, sdList.get(position) + "/");
                    if (temp_file.exists() && !temp_file.isFile()) { // it is a folder
                        file = new File(file, sdList.get(position) + "/");
                        setListView(listView, sdList,file);
                        if(sdList.size()==0) txtEmpty.setText("This folder is empty");
                    } else {  // it is a file...
                        Toast.makeText(getApplicationContext(), temp_file.getAbsolutePath().toString(), Toast.LENGTH_LONG).show();
                        Log.d("File", "File: " + temp_file.getAbsolutePath() + "\n");
                        openFile(temp_file.getAbsolutePath());
                    }
                }
            });
            registerForContextMenu(listView);
            listView.setLongClickable(true);

        }


    }
    private void setListView(ListView listView, List<String> sdList, File file){
        String s = file.getAbsolutePath();
        String folderName = "";
        if(!s.equals(root_sd)) {
            folderName = s.substring(root_sd.length()+1);
            actionBar.setTitle(folderName);
        }
        else actionBar.setTitle("SD Card");
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

    private void showNewFileDialog() {
        final Dialog customDialog = new Dialog(MainActivity.this);
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
        final Dialog customDialog = new Dialog(MainActivity.this);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED&&result2==PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)&&
                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to read  files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE)
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Log.v("TAG", "Permission Denied.");
            else
                Log.v("TAG", "Permission Granted.");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select action");
        menu.add(0, 1, 0, "Rename"); // g,roupId, itemId, order, title
        menu.add(0, 2, 0, "Delete");
        menu.add(0,3,0, "Copy to ...");
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(this, "You have chosen Rename" +
                                " context menu option for " + (int) info.id,
                        Toast.LENGTH_SHORT).show();
                showRenameDB((int)info.id);
                return true;
            case 2:
                Toast.makeText(this, "You have chosen Delete " +
                                " context menu option for " + (int) info.id,
                        Toast.LENGTH_SHORT).show();
                showConfirmDeletionDB(this, (int) info.id);
                return true;
            case 3:
                Intent intent = new Intent(MainActivity.this, CopyActivity.class);
                intent.putExtra("source", file.getAbsolutePath()+"/"+sdList.get((int) info.id));
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private void showConfirmDeletionDB(MainActivity mainActivity, final int position) { // show Dialog to confirm deleting a folder/file
        new AlertDialog.Builder(mainActivity)
                .setTitle("Confirm Deletion")
                .setMessage("1 item is going to be deleted. \n Do you want to continue?")
// set three option buttons
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
// actions serving "YES" button go here
                                //msg = "YES " + Integer.toString(whichButton);

                                File file2Delete = new File(file,sdList.get(position)+"/");
                                deleteRecursive(file2Delete);
                                sdList.remove(position);
                                ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                                Toast.makeText(MainActivity.this,"Delete Successfully",Toast.LENGTH_SHORT).show();
                            }
                        })// setPositiveButton
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
// actions serving "CANCEL" button go here
                                msg = "CANCEL " + Integer.toString(whichButton);
                                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                            }// OnClick
                        })// setNeutralButton
                .create()
                .show();
    }// showMyAlertDialog

    void deleteRecursive(File fileOrDirectory) { // detele a folder/file
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private void showRenameDB(final int position) { // Show Dialog to rename
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setTitle("Rename");
// match customDialog with custom dialog layout
        customDialog.setContentView(R.layout.rename_dialog_layout);
        final EditText sd_txtInputData = (EditText) customDialog
                .findViewById(R.id.sd_editText1);
        sd_txtInputData.setText(sdList.get(position));
        ((Button) customDialog.findViewById(R.id.btn_OK))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String newName = sd_txtInputData.getText().toString();

                        File oldfolder = new File(file,sdList.get(position));
                        File newfolder = new File(file,newName);
                        boolean check = oldfolder.renameTo(newfolder);
                        sdList.set(position, newName);
                        ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,oldfolder.toString()+ " to "+newfolder.toString()+": "+check,Toast.LENGTH_SHORT).show();
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

    public void openFile(String path) {
        try {
            File file2open = new File(path);
            String fileExtension = file2open.getAbsolutePath().substring(path.lastIndexOf("."));
            Log.d("path",file2open.getAbsolutePath());
            Log.d("path",fileExtension);
            String type = "text/plain";
            Intent fileIntent =new Intent(Intent.ACTION_VIEW);
            fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Uri apkURI = FileProvider.getUriForFile(
                    getApplicationContext(),
                    getApplicationContext()
                            .getPackageName() + ".provider", file2open);
            if(fileExtension.equalsIgnoreCase(".doc")||fileExtension.equalsIgnoreCase(".docx"))
                type = "application/msword";
            else if(fileExtension.equalsIgnoreCase(".pdf"))
                type = "application/pdf";
            else if(fileExtension.equalsIgnoreCase(".xls")||fileExtension.equalsIgnoreCase(".xlsx"))
                type = "application/vnd.ms-excel";
            else if(fileExtension.equalsIgnoreCase(".wav")||fileExtension.equalsIgnoreCase(".mp3"))
                type = "audio/x-wav";
            else if(fileExtension.equalsIgnoreCase(".jpg")||fileExtension.equalsIgnoreCase(".jpeg")||fileExtension.equalsIgnoreCase(".png"))
                type = "image/jpeg";
            else if(fileExtension.equalsIgnoreCase(".txt"))
                type = "text/plain";
            else if(fileExtension.equalsIgnoreCase(".3gp")||fileExtension.equalsIgnoreCase(".mp4")||fileExtension.equalsIgnoreCase(".avi"))
                type = "video/*";
            else type = "*/*";
            fileIntent.setDataAndType(apkURI, type);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getApplicationContext().startActivity(fileIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Cant Find Your File", Toast.LENGTH_LONG).show();
        }

    }

}
