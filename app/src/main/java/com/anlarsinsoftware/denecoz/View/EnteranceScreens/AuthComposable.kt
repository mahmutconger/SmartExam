package com.anlarsinsoftware.denecoz.View.EnteranceScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anlarsinsoftware.denecoz.R

@Composable
fun AuthScreen(
    title: String,
    emailValue: String,
    onEmailChange: (String) -> Unit,
    passwordValue: String,
    onPasswordChange: (String) -> Unit,
    buttonText: String,
    bottomText: String,
    bottomActionText: String,
    onBottomActionClick: () -> Unit,
    onContinueClick: () -> Unit,
    showForgotPassword: Boolean = false,
    onForgotPasswordClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.google_ico),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.continue_google))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_phone),
                    contentDescription = "Phone",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.continue_phone))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = stringResource(id = R.string.or),
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Gray
            )
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = emailValue,
            onValueChange = { onEmailChange },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.username_or_email)) },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = passwordValue,
            onValueChange = { onPasswordChange },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(id = R.string.password)) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (showForgotPassword) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(id = R.string.forgot_password),
                    color = Color.Blue,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0000FF)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(text = buttonText, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(text = bottomText)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = bottomActionText,
                color = Color.Blue,
                modifier = Modifier.clickable { onBottomActionClick() }
            )
        }
    }
}

