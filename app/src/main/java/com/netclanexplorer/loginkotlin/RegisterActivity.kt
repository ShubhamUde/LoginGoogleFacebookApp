package com.netclanexplorer.loginkotlin

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailReg: EditText
    private lateinit var passwordReg: EditText
    private lateinit var conPasswordReg: EditText
    private lateinit var registerBtn: Button
    private lateinit var alreadyAcc: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailReg = findViewById(R.id.rgEmail)
        passwordReg = findViewById(R.id.rgPassword)
        conPasswordReg = findViewById(R.id.rgConPassword)
        registerBtn = findViewById(R.id.btnRegister)
        alreadyAcc = findViewById(R.id.loginTxt)

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)

        alreadyAcc.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerBtn.setOnClickListener {
            val email = emailReg.editableText.toString()
            val password = passwordReg.editableText.toString()
            val confirmPassword = conPasswordReg.editableText.toString()

            if (email.isEmpty() || !email.contains("@gmail.com")) {
                showError(emailReg, "Enter valid email")
            } else if (password.isEmpty() || password.length < 7) {
                showError(passwordReg, "Password must be 7 digit")
            } else if (confirmPassword.isEmpty() || confirmPassword != password) {
                showError(conPasswordReg, "Password not match")
            } else {
                progressDialog.setTitle("Registration")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
    }
    private fun showError(input: EditText, stringError: String) {
        input.error = stringError
        input.requestFocus()
    }
}