package com.example.messanger

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@Composable
fun FriendsListScreen(navController: NavHostController) {
    var friends by remember { mutableStateOf(listOf<User>()) }
    val context = LocalContext.current
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    fetchFriendsList(context, currentUserUid) { results ->
        friends = results
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn {
            items(friends) { user ->
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.nickname,
                        fontSize = 30.sp,
                        color = Color(0xFF810000),
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { navController.navigate("chat/${user.uid}") },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color = Color(0xFF810000))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_message),
                            contentDescription = "Message Icon",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            removeFriend(context, currentUserUid, user.uid) {
                                friends = friends.filterNot { it.uid == user.uid }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color = Color(0xFF810000))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                            contentDescription = "Remove Icon",
                            tint = Color.White
                        )
                    }
                }
            }
        }



    }

        if (friends.isEmpty()) {
            Text(
                text = "Нет господ",
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF810000),
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }

private fun fetchFriendsList(context: Context, currentUserUid: String, onResults: (List<User>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userCollection = db.collection("users")
    val userQuery = userCollection.whereEqualTo("uid", currentUserUid)

    userQuery.get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.documents.isNotEmpty()) {
                val documentSnapshot = querySnapshot.documents[0]
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null) {
                    val friends = user.friends
                    if (friends.isNotEmpty()) {
                        userCollection.whereIn("uid", friends).get()
                            .addOnSuccessListener { friendsSnapshot ->
                                val results = friendsSnapshot.documents.mapNotNull { document ->
                                    document.toObject(User::class.java)
                                }
                                onResults(results)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Не удалось загрузить список друзей. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        onResults(emptyList())
                    }
                } else {
                    Toast.makeText(context, "Ошибка при загрузке данных пользователя.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Пользователь не найден.", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Ошибка при загрузке данных. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
        }
}

private fun removeFriend(context: Context, currentUserUid: String, friendUid: String, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userCollection = db.collection("users")

    userCollection.whereEqualTo("uid", currentUserUid).get()
        .addOnSuccessListener { currentUserSnapshot ->
            if (!currentUserSnapshot.isEmpty) {
                val currentUserDocument = currentUserSnapshot.documents[0]
                val currentUser = currentUserDocument.toObject(User::class.java)
                if (currentUser != null) {
                    val updatedFriends = currentUser.friends.toMutableList().apply {
                        remove(friendUid)
                    }

                    currentUserDocument.reference.update("friends", updatedFriends)
                        .addOnSuccessListener {
                            userCollection.whereEqualTo("uid", friendUid).get()
                                .addOnSuccessListener { friendSnapshot ->
                                    if (!friendSnapshot.isEmpty) {
                                        val friendDocument = friendSnapshot.documents[0]
                                        val friend = friendDocument.toObject(User::class.java)
                                        if (friend != null) {
                                            val updatedFriendsList = friend.friends.toMutableList().apply {
                                                remove(currentUserUid)
                                            }
                                            friendDocument.reference.update("friends", updatedFriendsList)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Пользователь удален из друзей", Toast.LENGTH_SHORT).show()
                                                    onComplete()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Не удалось удалить пользователя из друзей: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Ошибка при загрузке данных друга: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Не удалось обновить список друзей: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Ошибка при загрузке данных текущего пользователя: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(navController: NavHostController, friendUid: String) {
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var friendNickname by remember { mutableStateOf("") }
    val context = LocalContext.current
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    fetchFriendNickname(context, friendUid) { nickname ->
        friendNickname = nickname
    }

    val listState = rememberLazyListState()


    fetchMessages(context, currentUserUid, friendUid) { results ->
        messages = results
    }
    LaunchedEffect(messages) {
        if(messages.isEmpty()) return@LaunchedEffect
        listState.animateScrollToItem(messages.size - 1)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = {
                deleteAllMessages(context, currentUserUid, friendUid) {
                    messages = emptyList()
                }
            },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = Color(0xFF810000))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                contentDescription = "Remove Icon",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Chat header
        Text(
            text = "Чат с $friendNickname",
            fontSize = 24.sp,
            color = Color(0xFF810000),
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif
        )

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState // Assigning listState to LazyColumn
        ) {
            items(messages.filterNot { it.isDeletedForCurrentUser }.sortedBy { it.timestamp }) { msg ->
                val isCurrentUser = msg.senderUid == currentUserUid
                var showMenu by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                Row(
                    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showMenu = true }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = if (isCurrentUser) Color(0xFF810000) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isCurrentUser) Color.White else Color.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isCurrentUser) "Вы" else friendNickname,
                            color = if (isCurrentUser) Color.White else Color.Black
                        )
                    }
                }

                if (showMenu) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            {Text("Удалить для меня")},
                            onClick = {
                                scope.launch {
                                    deleteMessageForUser(context, currentUserUid, msg.id)
                                    messages = messages.map {
                                        if (it.id == msg.id) it.copy(isDeletedForCurrentUser = true) else it
                                    }
                                    showMenu = false
                                }
                            }
                        )
                        DropdownMenuItem(
                            {Text("Удалить для всех")},
                            onClick = {
                                scope.launch {
                                    deleteMessageForAll(context, msg.id)
                                    messages = messages.filterNot { it.id == msg.id }
                                    showMenu = false
                                }
                            }
                        )
                    }
                }
            }
        }

        Row{
            TextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Введите сообщение") },
                modifier = Modifier.weight(0.8f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    sendMessage(context, currentUserUid, friendUid, message)
                    message = ""
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color = Color(0xFF810000))
                    .weight(0.1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_send_24),
                    contentDescription = "Send Icon",
                    tint = Color.White
                )
            }
        }

    }
}


private fun fetchFriendNickname(context:Context, friendUid: String, onResult: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userDocRef = db.collection("users").whereEqualTo("uid", friendUid)

    userDocRef.get()
        .addOnSuccessListener { document ->
            if (!document.isEmpty) {
                val currentUserDocument = document.documents[0]
                val user = currentUserDocument.toObject(User::class.java)
                if (user != null) {
                    onResult(user.nickname)
                } else {
                    Toast.makeText(context, "Ошибка при загрузке данных друга.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Не удалось загрузить данные друга. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
        }
}

private fun fetchMessages(context: Context, currentUserUid: String, friendUid: String, onResults: (List<Message>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val messagesCollection = db.collection("messages")
    messagesCollection
        .whereIn("senderUid", listOf(currentUserUid, friendUid))
        .whereIn("receiverUid", listOf(currentUserUid, friendUid))
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                Toast.makeText(context, "Ошибка при получении сообщений: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            querySnapshot?.let { snapshot ->
                val results = snapshot.documents.mapNotNull { document ->
                    val message = document.toObject(Message::class.java)?.copy(id = document.id)
                    if (message != null && !message.deletedFor.contains(currentUserUid)) {
                        message
                    } else {
                        null
                    }
                }
                onResults(results)
            }
        }
}

private fun sendMessage(context: Context, currentUserUid: String, friendUid: String, message: String) {
    if (message.isBlank()) return

    val db = FirebaseFirestore.getInstance()
    val messagesCollection = db.collection("messages")
    val messageData = hashMapOf(
        "text" to message,
        "senderUid" to currentUserUid,
        "receiverUid" to friendUid,
        "timestamp" to System.currentTimeMillis(),
        "deletedFor" to listOf<String>()
    )

    messagesCollection.add(messageData)
        .addOnSuccessListener {
            Toast.makeText(context, "Сообщение отправлено", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Не удалось отправить сообщение. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
        }
}

private fun deleteMessageForUser(context: Context, currentUserUid: String, messageId: String) {
    val db = FirebaseFirestore.getInstance()
    val messageRef = db.collection("messages").document(messageId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(messageRef)
        if (snapshot.exists()) {
            val message = snapshot.toObject(Message::class.java)
            if (message != null) {
                val updatedDeletedFor = message.deletedFor.toMutableList()
                updatedDeletedFor.add(currentUserUid)
                transaction.update(messageRef, "deletedFor", updatedDeletedFor)
            }
        }
    }.addOnSuccessListener {
        Toast.makeText(context, "Сообщение удалено для вас", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Не удалось удалить сообщение. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun deleteMessageForAll(context: Context, messageId: String) {
    val db = FirebaseFirestore.getInstance()
    val messageRef = db.collection("messages").document(messageId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(messageRef)
        if (snapshot.exists()) {
            transaction.delete(messageRef)
        }
    }.addOnSuccessListener {
        Toast.makeText(context, "Сообщение удалено для всех", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Не удалось удалить сообщение. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
    }
}

private fun deleteAllMessages(context: Context, currentUserUid: String, friendUid: String, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val messagesCollection = db.collection("messages")

    messagesCollection
        .whereEqualTo("senderUid", currentUserUid).whereEqualTo("receiverUid", friendUid)
        .get()
        .addOnSuccessListener { querySnapshot1 ->
            val batch = db.batch()
            querySnapshot1.documents.forEach { document ->
                batch.delete(document.reference)
            }

            messagesCollection
                .whereEqualTo("senderUid", friendUid).whereEqualTo("receiverUid", currentUserUid)
                .get()
                .addOnSuccessListener { querySnapshot2 ->
                    querySnapshot2.documents.forEach { document ->
                        batch.delete(document.reference)
                    }

                    batch.commit().addOnSuccessListener {
                        Toast.makeText(context, "Все сообщения удалены", Toast.LENGTH_SHORT).show()
                        onComplete()
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Не удалось удалить сообщения. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Не удалось удалить сообщения. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Не удалось удалить сообщения. Пожалуйста, попробуйте позже.", Toast.LENGTH_SHORT).show()
        }
}

