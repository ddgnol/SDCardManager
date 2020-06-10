package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

import java.io.File;
import java.net.URI;
import java.nio.file.FileVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    ListView listView;
    List<String> sdList;
    File file;
    String msg = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list1);
        sdList = new ArrayList<String>();


        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) { //Hoi quyen truy nhap tu user
                if (checkPermission()) {
                    String root_sd = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                    Log.v("path", root_sd);
                    file = new File(root_sd);
                    String[] list1 = file.list();
                    if (file.exists()) {
                        File[] list = file.listFiles();

                        for (int i = 0; i < list.length; i++) {
                            sdList.add(list[i].getName());
                        }
                        ArrayAdapter aa = new ArrayAdapter<String>(this,
                                android.R.layout.simple_list_item_1, sdList);
                        listView.setAdapter(aa);
                    }
                } else {
                    requestPermission();
                }
            } else {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                if (file.exists()) {
                    Log.d("path", file.toString());
                    File list[] = file.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        sdList.add(list[i].getName());
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, sdList);
                    listView.setAdapter(arrayAdapter);
                }
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    File temp_file = new File(file, sdList.get(position) + "/");
                    if (temp_file.exists() && !temp_file.isFile()) { // it is a folder
                        file = new File(file, sdList.get(position) + "/");
                        File list[] = file.listFiles();

                        sdList.clear();

                        for (int i = 0; i < list.length; i++) {
                            sdList.add(list[i].getName());
                        }
                        Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_LONG).show();
                        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, sdList);
                        listView.setAdapter(arrayAdapter);

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
        else if(id == R.id.act_newFile) {
            showNewFileDialog();
            return true;
        }
        return false;
    }

    private void showNewFileDialog() {

    }

    private void showNewFolderDialog() {
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
                            sdList.add(0,sd_txtInputData.getText().toString());
                            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
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
        String parent = file.getParent().toString();
        file = new File(parent);
        File list[] = file.listFiles();

        sdList.clear();

        for (int i = 0; i < list.length; i++) {
            sdList.add(list[i].getName());
        }
        Toast.makeText(getApplicationContext(), parent, Toast.LENGTH_LONG).show();
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, sdList);
        listView.setAdapter(arrayAdapter);
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
        menu.add(0, 1, 0, "Rename"); // groupId, itemId, order, title
        menu.add(0, 2, 0, "Delete");
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
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showConfirmDeletionDB(MainActivity mainActivity, final int position) {
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

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private void showRenameDB(final int position) {
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
            String fileExtension = path.substring(path.lastIndexOf("."));
            Log.d("path",file2open.getAbsolutePath());
            Log.d("file extension", fileExtension);
            Uri dir = Uri.fromFile(file2open);
            Intent fileIntent = new Intent(Intent.ACTION_VIEW);

            fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            fileIntent.setDataAndType(dir, "image/jpeg");
            Uri apkURI = FileProvider.getUriForFile(
                    getApplicationContext(),
                    getApplicationContext()
                            .getPackageName() + ".provider", file);
            fileIntent.setDataAndType(apkURI, "application/pdf");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getApplicationContext().startActivity(fileIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Cant Find Your File", Toast.LENGTH_LONG).show();
        }

    }

}