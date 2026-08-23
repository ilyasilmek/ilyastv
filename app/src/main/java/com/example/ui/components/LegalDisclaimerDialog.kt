package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.StreamFlowAccentOrange
import com.example.ui.theme.StreamFlowLiveRed

@Composable
fun LegalDisclaimerDialog(
    isFirstLaunch: Boolean = true,
    onAccept: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    var isChecked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!isFirstLaunch) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isFirstLaunch,
            dismissOnClickOutside = !isFirstLaunch,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("legal_disclaimer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = StreamFlowAccentOrange.copy(alpha = 0.2f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = StreamFlowAccentOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Yasal Uyarı & Sorumluluk Reddi",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "İlyasTV Medya Oynatıcı Kullanım Şartları",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Legal Text Area
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LegalClauseItem(
                            icon = Icons.Default.Security,
                            title = "1. Yalnızca Medya Oynatıcı Yazılımıdır",
                            content = "İlyasTV, kullanıcıların kendi sağladıkları M3U/M3U8 bağlantılarını ve Xtream Codes API oynatma listelerini oynatabilmeleri için geliştirilmiş bağımsız bir medya oynatıcı arayüzüdür. İlyasTV kendi sunucularında veya bünyesinde hiçbir video, canlı TV yayını, film, dizi veya telifli medya içeriği barındırmaz, depolamaz veya dağıtmaz."
                        )

                        LegalClauseItem(
                            icon = Icons.Default.WarningAmber,
                            title = "2. Telif Hakları ve Yasal Sorumluluk",
                            content = "Uygulamaya eklenen tüm oynatma listeleri, sunucu adresleri ve medya akışlarının yasal sorumluluğu münhasıran kullanıcıya aittir. Telif hakkı sahibinin açık izni ve lisansı olmaksızın korsan / izinsiz IPTV yayınlarını izlemek ve yaymak yasalara aykırıdır ve ilgili kanunlar kapsamında hukuki ve cezai yaptırımlar doğurabilir."
                        )

                        LegalClauseItem(
                            icon = Icons.Default.Gavel,
                            title = "3. Üçüncü Taraf Sağlayıcılar",
                            content = "İlyasTV geliştiricileri; üçüncü şahıs veya sağlayıcılar tarafından sunulan IPTV yayınlarının içeriği, sürekliliği, kalitesi, yasal statüsü veya sunucu güvenliğinden hiçbir koşulda sorumlu tutulamaz."
                        )

                        LegalClauseItem(
                            icon = Icons.Default.Check,
                            title = "4. Kullanıcı Kabul ve Beyanı",
                            content = "Bu uygulamayı kullanarak yalnızca yasal, kamuya açık veya lisanslı yayın kaynaklarını ekleyeceğinizi; doğabilecek tüm hukuki ve idari sorumlulukların şahsınıza ait olduğunu peşinen kabul ve taahhüt etmiş olursunuz."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isFirstLaunch) {
                    // Checkbox for acceptance
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Yukarıdaki yasal uyarıyı ve sorumluluk reddi şartlarını okudum, anladım ve kabul ediyorum.",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (isChecked) {
                                onAccept()
                            }
                        },
                        enabled = isChecked,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("accept_disclaimer_button")
                    ) {
                        Text(
                            text = "Okudum ve Kabul Ediyorum",
                            color = if (isChecked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Read-only review mode from menu
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Kapat", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalClauseItem(
    icon: ImageVector,
    title: String,
    content: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StreamFlowAccentOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}
