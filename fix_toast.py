import re

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.background", "import android.widget.Toast\nimport androidx.compose.foundation.background")

replacement = """onError = { error ->
                                            isAuthenticatingGoogle = false
                                            Toast.makeText(context, "Erro: $error", Toast.LENGTH_LONG).show()
                                        }"""

content = content.replace("""onError = { error ->
                                            isAuthenticatingGoogle = false
                                            // Ideally show a toast or error message here
                                        }""", replacement)

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'w') as f:
    f.write(content)

