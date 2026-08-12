import re

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'r') as f:
    content = f.read()

# Replace the block from `if (isAuthenticatingGoogle) {` to `} else if (isAlreadySignedIn) {`
# We'll use a regex that matches from `if (isAuthenticatingGoogle) {` until `} else if (isAlreadySignedIn) {`
pattern = re.compile(r'if \(isAuthenticatingGoogle\) \{.*?} else if \(isAlreadySignedIn\) \{', re.DOTALL)

replacement = """if (isAuthenticatingGoogle) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFF4285F4))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Conectando à sua Conta do Google...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        } else if (isAlreadySignedIn) {"""

new_content = pattern.sub(replacement, content)

with open('app/src/main/java/com/example/ui/components/GoogleLoginDialog.kt', 'w') as f:
    f.write(new_content)

