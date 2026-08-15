package com.runmate.compose.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runmate.compose.supabase.AccountState
import com.runmate.compose.supabase.SupabaseConnectionViewModel

@Composable
fun LoginScreen(state: AccountState, viewModel: SupabaseConnectionViewModel) {
    val context = LocalContext.current
    val busy = state == AccountState.Restoring || state == AccountState.Working || state == AccountState.AwaitingGoogle
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFE7F7FB), Color(0xFFF3F8FC), Color.White)),
        ).padding(28.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(76.dp).clip(CircleShape).background(Color(0xFF197C9B)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.MonitorHeart, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("WholeMate", color = Color(0xFF142A46), fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text(
                "Understand your body, one day at a time.",
                color = Color(0xFF667A91),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(42.dp))
            Button(
                onClick = {
                    viewModel.beginGoogleSignIn()?.let { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF197C9B), contentColor = Color.White),
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Continue with Google", fontWeight = FontWeight.Bold)
            }
            if (state is AccountState.SignedOut && !state.message.isNullOrBlank()) {
                Text(state.message, color = Color(0xFF8A231F), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            }
            Text(
                "Health data stays on this device until a separate sync is approved.",
                color = Color(0xFF667A91),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 26.dp),
            )
        }
    }
}
