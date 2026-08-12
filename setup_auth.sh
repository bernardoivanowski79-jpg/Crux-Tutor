sed -i '1i\
import androidx.credentials.CredentialManager\
import androidx.credentials.GetCredentialRequest\
import androidx.credentials.exceptions.GetCredentialException\
import androidx.credentials.exceptions.NoCredentialException\
import com.google.android.libraries.identity.googleid.GetGoogleIdOption\
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential\
import com.google.firebase.auth.FirebaseAuth\
import com.google.firebase.auth.GoogleAuthProvider\
import androidx.compose.ui.platform.LocalContext\
import androidx.compose.runtime.rememberCoroutineScope\
import kotlinx.coroutines.launch\
import kotlinx.coroutines.tasks.await\
' app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt
