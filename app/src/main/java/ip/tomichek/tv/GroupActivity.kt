package ip.tomichek.tv

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class GroupActivity : AppCompatActivity() {

    private var remainingAttempts = 3
    private val PIN_CODE_PREF_KEY = "pin_code_entered"
    private lateinit var pinCodes: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_group)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val l1: ConstraintLayout = findViewById(R.id.l1)
        val l2: ConstraintLayout = findViewById(R.id.l2)
        val l3: ConstraintLayout = findViewById(R.id.l3)
        val l4: ConstraintLayout = findViewById(R.id.l4)
        val l5: ConstraintLayout = findViewById(R.id.l5)
        val l6: ConstraintLayout = findViewById(R.id.l6)
        val l7: ConstraintLayout = findViewById(R.id.l7)
        val l8: ConstraintLayout = findViewById(R.id.l8)
        val l9: ConstraintLayout = findViewById(R.id.l9)
        val l10: ConstraintLayout = findViewById(R.id.l10)
        val l11: ConstraintLayout = findViewById(R.id.l11)


        l1.setOnClickListener {

            val intent = Intent(this, TelechannelsHDFHD::class.java)
            startActivity(intent)
        }
        l2.setOnClickListener {

            val intent = Intent(this, TV_Premium::class.java)
            startActivity(intent)
        }
        l3.setOnClickListener {

            val intent = Intent(this, Films_HD_FHD::class.java)
            startActivity(intent)
        }
        l4.setOnClickListener {

            val intent = Intent(this, MultFilms_HD_FHD::class.java)
            startActivity(intent)
        }
        l5.setOnClickListener {

            val intent = Intent(this, Serials_FHD::class.java)
            startActivity(intent)
        }
        l6.setOnClickListener {

            val intent = Intent(this, UFC_FHD::class.java)
            startActivity(intent)
        }
        l7.setOnClickListener {

            val intent = Intent(this, FORSAZH::class.java)
            startActivity(intent)
        }
        l8.setOnClickListener {

            val intent = Intent(this, AMERICANPIE::class.java)
            startActivity(intent)
        }
        l9.setOnClickListener {

            val intent = Intent(this, FOUR_K::class.java)
            startActivity(intent)
        }
        l10.setOnClickListener {

            val intent = Intent(this, TajikistanTV::class.java)
            startActivity(intent)
        }
        l11.setOnClickListener {

            val intent = Intent(this, IranTV::class.java)
            startActivity(intent)
        }
        FirebaseApp.initializeApp(this)
        readPinCodesFromFirebase()

        val pinCodeEntered = getPinCodeEnteredStatus()
        if (pinCodeEntered) {
            // Пользователь уже вводил пин-код
        } else {
            showPinCodeDialog()
        }

    }

    private fun readPinCodesFromFirebase() {
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReferenceFromUrl("gs://tomichek-tv.appspot.com/pin_codes.txt")

        storageReference.getBytes(1024 * 1024) // Максимальный размер файла (1 MB)
            .addOnSuccessListener { bytes ->
                val pinCodesString = String(bytes, Charsets.UTF_8)
                pinCodes = pinCodesString.lines()
            }
            .addOnFailureListener { e ->
                Log.e("YourActivity", "Error reading pin codes from Firebase Storage", e)
            }
    }

    private fun removePinCodeFromFirebase(enteredPinCode: String) {
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReferenceFromUrl("gs://tomichek-tv.appspot.com/pin_codes.txt")

        // Читаем текущие пин-коды из Firebase Storage
        storageReference.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                val pinCodesString = String(bytes, Charsets.UTF_8)

                // Удаляем введенный пин-код
                val updatedPinCodes = pinCodesString.lines().filter { it != enteredPinCode }

                // Загружаем обновленные пин-коды обратно в Firebase Storage
                storageReference.putBytes(updatedPinCodes.joinToString("\n").toByteArray())
                    .addOnSuccessListener {
                        Log.d("YourActivity", "Pin code removed successfully from Firebase Storage.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("YourActivity", "Error removing pin code from Firebase Storage", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("YourActivity", "Error reading pin codes from Firebase Storage", e)
            }
    }

    private fun showPinCodeDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.pin_code_dialog, null)

        builder.setView(dialogView)
        builder.setTitle("Введите пин код")

        val inputEditText: EditText = dialogView.findViewById(R.id.pinCodeEditText)

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        builder.setPositiveButton("OK") { _, _ ->
            val enteredPinCode = inputEditText.text.toString()
            if (pinCodes.contains(enteredPinCode)) {
                // Пин-код верен, сохраняем статус введенного пин-кода
                setPinCodeEnteredStatus(true)
                removePinCodeFromFirebase(enteredPinCode)
                showToast("Добро Пожаловать")
            }  else {
                // Пин-код неверен

                remainingAttempts--
                showToast("Неверный пин код. Осталось попыток: $remainingAttempts")
                if (remainingAttempts <= 0) {
                    // Если попыток больше нет, завершите приложение
                    showToast("Попытки закончились. Выход из приложения.")
                    finish()
                } else {
                    showPinCodeDialog()
                }
            }
        }

            // Обработайте отмену ввода пин-кода, например, выход из приложения
            builder.setCancelable(false);

        builder.show()
    }

    private fun getPinCodeEnteredStatus(): Boolean {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(PIN_CODE_PREF_KEY, false)
    }

    private fun setPinCodeEnteredStatus(entered: Boolean) {
        if (entered) {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(PIN_CODE_PREF_KEY, entered).apply()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}