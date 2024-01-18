package com.example.testing123

import io.ktor.client.request.forms.formData
import io.ktor.http.HttpHeaders

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream

@Serializable
data class CsvUploadResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String
)

class CsvUploadActivity : AppCompatActivity() {
    private lateinit var selectedFileUri: Uri
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csv_upload)

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedFileUri = uri

                    Toast.makeText(this, "File selected successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val selectFileButton: Button = findViewById(R.id.btnSelectFile)
        selectFileButton.setOnClickListener {
            openFilePicker()
        }

        val uploadButton: Button = findViewById(R.id.btnUploadFile)
        uploadButton.setOnClickListener {
            onUploadButtonClick()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
        }

        filePickerLauncher.launch(intent)
    }

    private fun onUploadButtonClick() {
        mainScope.launch {
            try {
                val accessToken = TokenManager.getInstance().getAccessToken()

                // Check if the selected file has a .csv MIME type
                val mimeType = contentResolver.getType(selectedFileUri)
                if (mimeType == "text/csv") {
                    val response: ApiResponse<List<Song>> = uploadFile(accessToken)
                    Log.d("UPLOAD_RESPONSE", response.toString())
                } else {
                    Toast.makeText(this@CsvUploadActivity, "Please select a .csv file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("UPLOAD_ERROR", "Error uploading file", e)
            }
        }
    }



    private suspend fun uploadFile(accessToken: String?): ApiResponse<List<Song>> {
        return withContext(Dispatchers.IO) {
            val client = HttpClient()

            try {
                val response: ApiResponse<List<Song>> = client.post("http://192.168.1.31:3000/upload") {
                    headers {
                        accessToken?.let { append(HttpHeaders.Authorization, "Bearer $it") }
                    }
                    body = MultiPartFormDataContent(
                        formData {
                            appendInput(
                                key = "file",
                                headers = Headers.build {
                                    val originalFilename = getFileNameFromUri(selectedFileUri)
                                    val mimeType = contentResolver.getType(selectedFileUri)
                                    println("Sending file with original filename: $originalFilename, MIME type: $mimeType")
                                    append(HttpHeaders.ContentDisposition, "filename=$originalFilename")
                                }
                            ) {
                                getInputStream(selectedFileUri)
                            }
                        }
                    )
                }

                return@withContext response
            } finally {
                client.close()
            }
        }
    }




    private fun getInputStream(uri: Uri): Input {
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Failed to open input stream for file: $uri")
        return inputStream.asInput()
    }
    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (displayNameIndex != -1 && it.moveToFirst()) {
                val displayName = it.getString(displayNameIndex)
                if (!displayName.isNullOrBlank()) {
                    return displayName
                }
            }
        }

        // Default to a generic filename if extraction fails
        return "file.csv"
    }


    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}

