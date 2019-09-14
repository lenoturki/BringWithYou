package com.SWEProject.bringwithyou.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.SWEProject.bringwithyou.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.jar.Attributes;

public class Register extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static int PReqCode=1;
    static int REQUESCODE=1;
    Uri pickedImgUri ;


    private EditText userEmail,userPassword , userPassword2 , userName ,userUname ,userPhone ;
    private ProgressBar loadingProgress ;
    private Button regButton;
    private FirebaseAuth mAuth ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        //inu views
        userName=findViewById(R.id.RegName);
        userUname=findViewById(R.id.RegUName);
        userEmail=findViewById(R.id.RegEmail);
        userPassword=findViewById(R.id.RegPass);
        userPassword2=findViewById(R.id.RegPass2);
        userPhone=findViewById(R.id.RegPhone);
        loadingProgress=findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();


        loadingProgress.setVisibility(View.INVISIBLE);
        regButton =findViewById(R.id.RegButton);

        regButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

              regButton.setVisibility(View.INVISIBLE);
              loadingProgress.setVisibility(View.VISIBLE);
              final String email = userEmail.getText().toString();
              final String password = userPassword.getText().toString();
              final String password2 = userPassword2.getText().toString();
              final String name = userName.getText().toString();
              final String Phone = userPhone.getText().toString();

              if(email.isEmpty() || name.isEmpty() || password.isEmpty()  || Phone.isEmpty() || !password.equals(password2)){

                  // something gone wrong all field must be field
                  // we need to display an error message
                  showMessage("please Verify all Fields") ;
                  regButton.setVisibility(View.VISIBLE);
                  loadingProgress.setVisibility(View.INVISIBLE);


              }

              else{

                  // everthing is ok all field are filled  now we can start creating user account
                  //CreateUserAccount method will try to create the user if the email is valid


                  CreateUserAccount (email,name,password );

              }







            }
        });


        ImgUserPhoto=findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=22){
                    checkAndRequestForPermission();

                }

                else{
                    openGallery();

                }
            }
        });
    }

    private void CreateUserAccount(String email, final String name, String password) {

        // this method create user account with specific email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user account create succesfully
                            showMessage("Account Created ");
                            // after we created user account we need to update his profile picture and name
                            updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser() );
                        } else {
                            // creation failed
                            showMessage("Account Creation Failed" + task.getException().getMessage());
                            regButton.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
// update user photo and name
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {

        // first we need to upload user photo to firebase and get url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // image uploaded succefully
                // now we can get our image url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url


                        UserProfileChangeRequest profileUpdate =new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        // user info updated successful
                                        showMessage("Register Complete");
                                        updateUI();
                                    }

                                    }
                                });



                    }
                });



            }
        });


    }

    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(),Home.class);
        startActivity(homeActivity);
        finish();



    }


    // simple method to show toast message
    private void showMessage(String message) {


    Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();




    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }

    private void checkAndRequestForPermission() {
       if(ContextCompat.checkSelfPermission(Register.this , Manifest.permission.READ_EXTERNAL_STORAGE)
       != PackageManager.PERMISSION_GRANTED) {
           if(ActivityCompat.shouldShowRequestPermissionRationale(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE)){

    Toast.makeText(Register.this , "Please accept for requierd permission" ,Toast.LENGTH_SHORT).show();
           }

           else{
               ActivityCompat.requestPermissions(Register.this ,
                       new String[]{Manifest.permission.READ_EXTERNAL_STORAGE },
                       PReqCode);
           }
    }

        else openGallery();

}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode == REQUESCODE && data != null){
            // the user ha successfully picked an image
            // we need to save its refrence to a Uri variable
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }
}
