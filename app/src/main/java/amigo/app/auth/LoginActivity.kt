package amigo.app.auth

import amigo.app.CarerContactsActivity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import amigo.app.R
import amigo.app.assisted.AssistedHomeActivity
import amigo.app.carer.CarerHomeActivity
import amigo.app.useractivity.MainActivity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTitle("Log in")
        setContentView(R.layout.activity_login)

        textView_register.setOnClickListener {
            finish()
        }

        button_login.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin(){
        var email = editText_email.text.toString()
        var password = editText_Password.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    var uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference()
                    var user = ref.child("users").child(uid)

                    user.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot?) {
                            if (p0 != null) {
                                var iscarer = p0.child("iscarer").getValue(Boolean::class.java) ?: true
                                enterHomeScreen(iscarer)
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {

                        }
                    })
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Fail to sign in: ${it.message}", Toast.LENGTH_LONG).show()
                }
    }

    private fun enterHomeScreen(iscarer: Boolean){
        val intent: Intent
        if (iscarer){
            intent = Intent(this, CarerContactsActivity::class.java)
        }
        else{
            intent = Intent(this, MainActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
