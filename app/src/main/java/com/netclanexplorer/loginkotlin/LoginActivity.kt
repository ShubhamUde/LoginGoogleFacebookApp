package com.netclanexplorer.loginkotlin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.Arrays


class LoginActivity : AppCompatActivity() {

    private lateinit var cardGoogle: CardView
    private lateinit var cardFacebook: CardView
    private lateinit var emailLog: EditText
    private lateinit var passwordLog: EditText
    private lateinit var forgotPass: TextView
    private lateinit var loginBtn: Button
    private lateinit var newAcc: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        cardGoogle = findViewById(R.id.googleCard)
        cardFacebook = findViewById(R.id.facebookCard)
        emailLog = findViewById(R.id.logEmail)
        passwordLog = findViewById(R.id.logPassword)
        forgotPass = findViewById(R.id.forgotPassword)
        loginBtn = findViewById(R.id.btnLogin)
        newAcc = findViewById(R.id.registerTxt)


        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                }
                override fun onCancel() {

                }
                override fun onError(exception: FacebookException) {

                }
            })

        cardFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"));
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_clint_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        cardGoogle.setOnClickListener {
            val signiInIntent = googleSignInClient.signInIntent
            launcher.launch(signiInIntent)
        }

        newAcc.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginBtn.setOnClickListener {
            val email = emailLog.editableText.toString()
            val password = passwordLog.editableText.toString()

            if (email.isEmpty() || !email.contains("@")) {
                showError(emailLog, "Enter valid email")
            } else if (password.isEmpty() || password.length < 7) {
                showError(passwordLog, "Password must be 7 digit")
            } else {
                progressDialog.setTitle("Login")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                            progressDialog.dismiss()
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this, "Authentication failed.", Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }

    private fun showError(input: EditText, stringError: String) {
        input.error = stringError
        input.requestFocus()
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>){
        if (task.isSuccessful){
            val account: GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(
                this, "Authentication failed.", Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    Toast.makeText(
                        this, "Login Successful", Toast.LENGTH_SHORT,
                    ).show()
                }else{
                    Toast.makeText(
                        this, "Authentication failed.", Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}