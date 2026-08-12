import re

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'r') as f:
    content = f.read()

replacement = """fun GoogleLoginDialog(
    onDismiss: () -> Unit,
    onOpenAvatarPicker: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
"""

content = content.replace("""fun GoogleLoginDialog(
    onDismiss: () -> Unit,
    onOpenAvatarPicker: () -> Unit
) {""", replacement)

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'w') as f:
    f.write(content)
