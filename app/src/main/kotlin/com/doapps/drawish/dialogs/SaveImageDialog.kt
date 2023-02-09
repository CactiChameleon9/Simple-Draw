package com.doapps.drawish.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.doapps.drawish.R
import com.doapps.drawish.activities.SimpleActivity
import com.doapps.drawish.helpers.JPG
import com.doapps.drawish.helpers.PNG
import com.doapps.drawish.helpers.SVG
import kotlinx.android.synthetic.main.dialog_save_image.view.*
import java.io.File

class SaveImageDialog(
    val activity: SimpleActivity, val defaultPath: String, val defaultFilename: String, val defaultExtension: String,
    val hidePath: Boolean, callback: (fullPath: String, filename: String, extension: String) -> Unit
) {
    private val SIMPLE_DRAW = "Simple Draw"

    init {
        val initialFilename = getInitialFilename()
        var folder = if (defaultPath.isEmpty()) "${activity.internalStoragePath}/$SIMPLE_DRAW" else defaultPath
        val view = activity.layoutInflater.inflate(R.layout.dialog_save_image, null).apply {
            save_image_filename.setText(initialFilename)
            save_image_radio_group.check(
                when (defaultExtension) {
                    JPG -> R.id.save_image_radio_jpg
                    SVG -> R.id.save_image_radio_svg
                    else -> R.id.save_image_radio_png
                }
            )

            if (hidePath) {
                folder_hint.beGone()
            } else {
                folder_value.setText(activity.humanizePath(folder))
                folder_value.setOnClickListener {
                    FilePickerDialog(activity, folder, false, showFAB = true) {
                        folder_value.setText(activity.humanizePath(it))
                        folder = it
                    }
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.save_as) { alertDialog ->
                    alertDialog.showKeyboard(view.save_image_filename)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = view.save_image_filename.value
                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val extension = when (view.save_image_radio_group.checkedRadioButtonId) {
                            R.id.save_image_radio_png -> PNG
                            R.id.save_image_radio_svg -> SVG
                            else -> JPG
                        }

                        val newPath = "${folder.trimEnd('/')}/$filename.$extension"
                        if (!newPath.getFilenameFromPath().isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        if (!hidePath && File(newPath).exists()) {
                            val title = String.format(activity.getString(R.string.file_already_exists_overwrite), newPath.getFilenameFromPath())
                            ConfirmationDialog(activity, title) {
                                callback(newPath, filename, extension)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newPath, filename, extension)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }

    private fun getInitialFilename(): String {
        val newFilename = "image_${activity.getCurrentFormattedDateTime()}"
        return if (defaultFilename.isEmpty()) newFilename else defaultFilename
    }
}
