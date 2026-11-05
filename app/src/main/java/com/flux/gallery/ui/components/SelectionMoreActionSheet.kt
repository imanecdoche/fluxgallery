package com.flux.gallery.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.alexzhirkevich.cupertino.CupertinoActionSheet
import io.github.alexzhirkevich.cupertino.cancel
import io.github.alexzhirkevich.cupertino.default

@Composable
fun SelectionMoreActionSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedCount: Int,
    onActionSelected: (String) -> Unit
) {
    CupertinoActionSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(text = "$selectedCount item dipilih")
        },
        buttons = {
            default(
                onClick = { onActionSelected("Salin ke clipboard"); onDismiss() }
            ) {
                Text("Salin ke clipboard")
            }
            default(
                onClick = { onActionSelected("Salin ke album"); onDismiss() }
            ) {
                Text("Salin ke album")
            }
            default(
                onClick = { onActionSelected("Pindahkan ke album"); onDismiss() }
            ) {
                Text("Pindahkan ke album")
            }
            default(
                onClick = { onActionSelected("Tambahkan ke favorit"); onDismiss() }
            ) {
                Text("Tambahkan ke favorit")
            }
            default(
                onClick = { onActionSelected("Tambahkan tag"); onDismiss() }
            ) {
                Text("Tambahkan tag")
            }
            default(
                onClick = { onActionSelected("Ubah tanggal dan waktu"); onDismiss() }
            ) {
                Text("Ubah tanggal dan waktu")
            }
            default(
                onClick = { onActionSelected("Ubah lokasi"); onDismiss() }
            ) {
                Text("Ubah lokasi")
            }
            default(
                onClick = { onActionSelected("Mulai tayangan slide"); onDismiss() }
            ) {
                Text("Mulai tayangan slide")
            }
            default(
                onClick = { onActionSelected("Atur sebagai wallpaper"); onDismiss() }
            ) {
                Text("Atur sebagai wallpaper")
            }
            cancel(
                onClick = onDismiss
            ) {
                Text("Batal")
            }
        }
    )
}
