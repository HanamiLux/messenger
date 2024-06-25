package com.example.messanger

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var nicknames by remember { mutableStateOf(listOf<User>()) }
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                searchUsers(context, query, currentUserUid) { results ->
                    nicknames = results
                }
            },
            label = { Text("Поиск по логину") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp)),
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF810000),
                focusedTextColor = Color.White,
                unfocusedContainerColor = Color(0xFF810000),
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(nicknames) { user ->
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = user.nickname,
                            fontSize = 18.sp,
                            color = Color(0xFF810000),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif)
                    Button(
                        onClick = { sendFriendRequest(context, user.uid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF810000), contentColor = Color.White)
                    ) {
                        Text("Добавить в приближённые")
                    }
                }
            }
        }
    }
}

private fun searchUsers(context: Context, query: String, currentUserUid: String, onResults: (List<User>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userReference = db.collection("users")

    if (query.isNotEmpty()) {
        val lowercaseQuery = query.lowercase(Locale.getDefault())

        userReference.get()
            .addOnSuccessListener { querySnapshot ->
                val results = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(User::class.java)
                }.filter { user ->
                    user.uid != currentUserUid && (
                            user.nickname.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                                    user.lastName.lowercase(Locale.getDefault()).contains(lowercaseQuery) ||
                                    user.firstName.lowercase(Locale.getDefault()).contains(lowercaseQuery)
                            )
                }
                onResults(results)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка поиска: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    } else {
        onResults(emptyList())
    }
}

private fun sendFriendRequest(context: Context, uid: String) {
    val db = FirebaseFirestore.getInstance()
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val userCollection = db.collection("users")
    val userQuery = userCollection.whereEqualTo("uid", uid)
    val currentUserQuery = userCollection.whereEqualTo("uid", currentUserUid)

    currentUserQuery.get()
        .addOnSuccessListener { currentUserSnapshot ->
            if (!currentUserSnapshot.isEmpty) {
                val currentUserDocument = currentUserSnapshot.documents[0]
                val currentUser = currentUserDocument.toObject(User::class.java)
                if (currentUser != null) {
                    if (currentUser.friends.contains(uid)) {
                        Toast.makeText(context, "Этот господин уже ваш приближённый", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    userQuery.get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val documentSnapshot = querySnapshot.documents[0]
                                val user = documentSnapshot.toObject(User::class.java)
                                if (user != null) {
                                    if (!user.friendRequests.contains(currentUserUid)) {
                                        val updatedFriendRequests = user.friendRequests + currentUserUid
                                        documentSnapshot.reference.update("friendRequests", updatedFriendRequests)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Весть отправлена господину", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Ошибка запроса: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Весть уже была отправлена", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Ошибка получения господина", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Господина не найден", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Ошибка отправки весточки: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Ошибка получения текущего господина", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Текущий господин не найден", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Ошибка проверки текущего господина: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

