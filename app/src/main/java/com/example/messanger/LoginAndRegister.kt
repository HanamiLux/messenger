
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.messanger.BottomNavItem
import com.example.messanger.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(navController: NavHostController) {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Регистрация",
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF810000),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000),
                        focusedTextColor = Color.White,
                    )

                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000),
                        focusedTextColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Логин") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000),
                        focusedTextColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Почта") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000),
                        focusedTextColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000),
                        focusedTextColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        registerUser(context, navController, lastName, firstName, nickname, email, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF810030), contentColor = Color.White)
                ) {
                    Text("Зарегистрироваться", fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate("login")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xDA810000), contentColor = Color.White)
                ) {
                    Text("К авторизации")
                }
            }
        }
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Авторизация",
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF810000),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Почта") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000),
                        focusedTextColor = Color.White,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0xFF810000),
                        focusedIndicatorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedContainerColor = Color(0xFF810000)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        loginUser(context, navController, email, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF810030), contentColor = Color.White)
                ) {
                    Text("Войти", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate("registration")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xDA810000), contentColor = Color.White)
                ) {
                    Text("К регистрации")
                }
            }
        }
    )
}


private fun registerUser(context:Context, navController: NavController, lastName: String, firstName: String, nickname: String, email: String, password: String) {
    if(email.isEmpty() || password.isEmpty()) return
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
        .addOnSuccessListener { authResult:AuthResult ->
            val fireBaseUser: FirebaseUser? = authResult.user
            var db:FirebaseFirestore = FirebaseFirestore.getInstance()
            var userReference:CollectionReference = db.collection("users")

            val newUser = User(fireBaseUser!!.uid, lastName, firstName, nickname, email, password)
            userReference.add(newUser)
                .addOnSuccessListener {
                    Toast.makeText(context, fireBaseUser.email + " зарегистрирован",
                        Toast.LENGTH_SHORT).show()
                    navController.navigate(BottomNavItem.Chats.route)
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }
}


private fun loginUser(context: Context, navController: NavController, email: String, password: String) {
    if(email.isEmpty() || password.isEmpty()) return
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email.trim(), password.trim())
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userReference = db.collection("users")

                    userReference.whereEqualTo("email", email).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                Toast.makeText(context, "Авторизация исполнена!", Toast.LENGTH_SHORT)
                                    .show()
                                navController.navigate(BottomNavItem.Chats.route)
                            } else {
                                Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Firestore failed: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    Toast.makeText(context, "Authentication failed (currentUser is null)", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
}
