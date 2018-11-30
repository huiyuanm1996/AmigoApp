package amigo.app.auth

import amigo.app.CarerContactsActivity
import amigo.app.R
import amigo.app.R.drawable.user
import amigo.app.R.id.*
import amigo.app.useractivity.MainActivity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_auth_register.*

class RegisterActivity : AppCompatActivity() {

    lateinit var ref: DatabaseReference
    lateinit var user: DatabaseReference
    lateinit var listener: ValueEventListener
    lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTitle("Sign up")
        setContentView(R.layout.activity_auth_register)

        verifyUserSession()

        textView_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        button_createacc.setOnClickListener {
            performRegister()
        }

        textView_whatcarer.setOnClickListener {
            Toast.makeText(this, "A carer uses AmiGo to help others in need of assistance to navigate in their daily life", Toast.LENGTH_LONG).show()
        }
    }

    fun performRegister(){
        var firstname = editText_firstname.text.toString()
        var lastname = editText_lastname.text.toString()
        var email = editText_email.text.toString()
        var password = editText_password.text.toString()
        var iscarer = checkBox_carer.isChecked

        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter username, email and password", Toast.LENGTH_LONG).show()
            return
        }

        createUser(firstname, lastname, email, password, iscarer)
    }

    private fun createUser(firstname: String, lastname: String, email: String, password: String, iscarer: Boolean){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show()
                    saveUserToFirebaseDatabase(firstname, lastname, iscarer)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG).show()
                }

    }

    private fun saveUserToFirebaseDatabase(firstname: String, lastname: String, iscarer: Boolean){
        uid = FirebaseAuth.getInstance().uid ?: ""
        ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, firstname, lastname, iscarer)

        ref.setValue(user)
                .addOnSuccessListener {
                    enterHomeScreen(iscarer)
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

    private fun verifyUserSession(){
        if (FirebaseAuth.getInstance().uid == null){
            return
        }
        else{
            var uid = FirebaseAuth.getInstance().uid
            ref = FirebaseDatabase.getInstance().getReference()
            user = ref.child("users").child(uid)

            if (user != null) {
                user.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(p0: DataSnapshot?) {
                        if (p0 != null) {
                            var iscarer = p0.child("iscarer").getValue(Boolean::class.java)!!
                            enterHomeScreen(iscarer)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
            }

        }
    }
}
